import os
import sys
import json
import sqlite3
import asyncio
import logging
import random
from urllib.parse import urljoin
from typing import Optional

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PROJECT_ROOT = os.path.dirname(BASE_DIR)
if BASE_DIR not in sys.path:
    sys.path.insert(0, BASE_DIR)

from bs4 import BeautifulSoup
import httpx

from client import SafeHttpClient, UpstreamNotFoundError, UpstreamError
from scrapers.anime_scraper import AnimeScraper, extract_id_and_slug

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    handlers=[logging.StreamHandler(sys.stdout)],
)
logger = logging.getLogger("OtakOtakuDumper")

DATA_DIR = os.path.join(BASE_DIR, "data")
COVERS_DIR = os.path.join(DATA_DIR, "covers")
DB_PATH = os.path.join(DATA_DIR, "anime_catalog.db")
JSON_PATH = os.path.join(DATA_DIR, "anime_catalog.json")


def init_database() -> sqlite3.Connection:
    """Initialize SQLite tables for full catalog and queue tracking."""
    os.makedirs(COVERS_DIR, exist_ok=True)
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()

    # Main catalog table
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS anime (
        id INTEGER PRIMARY KEY,
        title TEXT NOT NULL,
        japanese_title TEXT,
        english_title TEXT,
        slug TEXT,
        type TEXT,
        status TEXT,
        season TEXT,
        episodes TEXT,
        duration TEXT,
        rating TEXT,
        score TEXT,
        genres TEXT,
        studios TEXT,
        synopsis TEXT,
        cover_url TEXT,
        cover_file TEXT,
        otak_url TEXT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );
    """)

    # Queue table to store discovered list before detail scraping
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS anime_queue (
        id INTEGER PRIMARY KEY,
        slug TEXT,
        url TEXT UNIQUE,
        status TEXT DEFAULT 'pending'
    );
    """)

    conn.commit()
    return conn


async def collect_all_anime_urls(client: SafeHttpClient, conn: sqlite3.Connection, limit: Optional[int] = None) -> int:
    """Fetch feed pages and populate anime_queue."""
    cursor = conn.cursor()
    cursor.execute("SELECT COUNT(*) FROM anime_queue")
    existing_count = cursor.fetchone()[0]

    if existing_count > 0 and limit:
        return existing_count

    if existing_count > 3000:
        logger.info(f"Anime queue already populated with {existing_count} items. Skipping index step.")
        return existing_count

    max_pages = 2 if limit else 168
    logger.info(f"Collecting anime list from feed pages (1 to {max_pages})...")
    total_added = 0

    for page in range(1, max_pages + 1):
        target_path = f"/anime/feed/{page}" if page > 1 else "/anime/feed"
        try:
            html = await client.fetch_html(target_path)
            soup = BeautifulSoup(html, "html.parser")

            for a in soup.select(".anime-title a, .anime-img a"):
                href = a.get("href", "")
                if "/anime/view/" in href:
                    anime_id, slug = extract_id_and_slug(href, "anime/view")
                    if anime_id:
                        cursor.execute(
                            "INSERT OR IGNORE INTO anime_queue (id, slug, url, status) VALUES (?, ?, ?, 'pending')",
                            (anime_id, slug, href),
                        )
            conn.commit()
            if page % 20 == 0 or page == 168:
                cursor.execute("SELECT COUNT(*) FROM anime_queue")
                cnt = cursor.fetchone()[0]
                logger.info(f"Feed indexing progress: Page {page}/168 | Total queued: {cnt}")
            await asyncio.sleep(random.uniform(0.8, 1.3))
        except Exception as e:
            logger.error(f"Error indexing page {page}: {e}")
            await asyncio.sleep(2.0)

    cursor.execute("SELECT COUNT(*) FROM anime_queue")
    final_count = cursor.fetchone()[0]
    logger.info(f"Index collection completed. Total anime in queue: {final_count}")
    return final_count


async def download_cover(
    http_client: httpx.AsyncClient, cover_url: str, anime_id: int
) -> Optional[str]:
    """Download cover image and save locally."""
    if not cover_url or "spin-black" in cover_url:
        return ""

    file_name = f"{anime_id}.jpg"
    file_path = os.path.join(COVERS_DIR, file_name)

    if os.path.exists(file_path) and os.path.getsize(file_path) > 500:
        return file_name

    try:
        resp = await http_client.get(cover_url, timeout=10.0)
        if resp.status_code == 200:
            with open(file_path, "wb") as f:
                f.write(resp.content)
            return file_name
    except Exception as e:
        logger.warning(f"Could not download cover for {anime_id}: {e}")
    return ""


async def worker_scrape_details(
    worker_id: int,
    queue: asyncio.Queue,
    anime_scraper: AnimeScraper,
    img_client: httpx.AsyncClient,
    conn: sqlite3.Connection,
    total_items: int,
    progress_counter: list[int],
    lock: asyncio.Lock,
):
    """Worker task to process anime details and covers."""
    while not queue.empty():
        try:
            anime_id, slug, url = await queue.get()
        except asyncio.QueueEmpty:
            break

        try:
            detail = await anime_scraper.get_anime_detail(anime_id, slug)

            # Download cover
            cover_file = ""
            if detail.get("cover"):
                cover_file = await download_cover(img_client, detail["cover"], anime_id)

            genres_str = ", ".join([g["name"] for g in detail.get("genres", [])])
            studios_str = ", ".join([s["name"] for s in detail.get("studios", [])])

            async with lock:
                cursor = conn.cursor()
                cursor.execute("""
                INSERT OR REPLACE INTO anime (
                    id, title, japanese_title, english_title, slug,
                    type, status, season, episodes, duration, rating, score,
                    genres, studios, synopsis, cover_url, cover_file, otak_url
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, (
                    anime_id,
                    detail.get("title", ""),
                    detail.get("japanese_title", ""),
                    detail.get("english_title", ""),
                    slug,
                    detail.get("type", ""),
                    detail.get("status", ""),
                    detail.get("season", ""),
                    detail.get("episodes", ""),
                    detail.get("duration", ""),
                    detail.get("rating", ""),
                    detail.get("score", ""),
                    genres_str,
                    studios_str,
                    detail.get("synopsis", ""),
                    detail.get("cover", ""),
                    cover_file,
                    url,
                ))

                cursor.execute("UPDATE anime_queue SET status = 'completed' WHERE id = ?", (anime_id,))
                conn.commit()

                progress_counter[0] += 1
                curr = progress_counter[0]
                if curr % 25 == 0 or curr == total_items:
                    pct = (curr / total_items) * 100
                    logger.info(f"[Progress: {curr}/{total_items} ({pct:.1f}%)] Scraped: {detail.get('title')}")

            # Polite jitter delay between requests (per worker)
            await asyncio.sleep(random.uniform(0.9, 1.6))

        except UpstreamNotFoundError:
            async with lock:
                cursor = conn.cursor()
                cursor.execute("UPDATE anime_queue SET status = 'not_found' WHERE id = ?", (anime_id,))
                conn.commit()
                progress_counter[0] += 1
        except Exception as e:
            logger.warning(f"Worker {worker_id} connection issue on {anime_id} ({slug}): {e}. Re-queuing...")
            await queue.put((anime_id, slug, url))
            await asyncio.sleep(1.5)
        finally:
            queue.task_done()


def export_to_json(conn: sqlite3.Connection):
    """Export the entire SQLite anime table to a standalone JSON file."""
    conn.row_factory = sqlite3.Row
    cursor = conn.cursor()
    cursor.execute("SELECT * FROM anime ORDER BY id ASC")
    rows = [dict(row) for row in cursor.fetchall()]

    with open(JSON_PATH, "w", encoding="utf-8") as f:
        json.dump(rows, f, ensure_ascii=False, indent=2)

    logger.info(f"Exported {len(rows)} anime to {JSON_PATH}")


async def filter_working_proxies(proxies: list[str]) -> list[str]:
    """Test proxies concurrently and return only responsive ones."""
    working = []

    async def check(p):
        try:
            async with httpx.AsyncClient(proxy=p, timeout=4.0) as c:
                r = await c.get("https://ipv4.webshare.io/")
                if r.status_code == 200:
                    working.append(p)
        except Exception:
            pass

    await asyncio.gather(*[check(p) for p in proxies])
    return working


async def main(limit: Optional[int] = None):
    import dotenv
    dotenv.load_dotenv(os.path.join(PROJECT_ROOT, ".env"))
    raw_proxies = os.getenv("WEBSHARE_PROXIES", "").split(",")
    initial_pool = [f"http://{p.strip()}" for p in raw_proxies if p.strip()]

    logger.info("Performing quick health check on Webshare proxies...")
    proxy_pool = await filter_working_proxies(initial_pool) if initial_pool else []
    logger.info(f"Active healthy proxies: {len(proxy_pool)}/{len(initial_pool)}")

    conn = init_database()

    # Indexing client uses first active proxy
    index_proxy = proxy_pool[0] if proxy_pool else None
    safe_client = SafeHttpClient(proxy=index_proxy)

    worker_clients = []

    try:
        # Step 1: Ensure URLs are indexed
        await collect_all_anime_urls(safe_client, conn, limit=limit)

        # Step 2: Fetch pending items
        cursor = conn.cursor()
        if limit:
            cursor.execute("SELECT id, slug, url FROM anime_queue WHERE status = 'pending' LIMIT ?", (limit,))
        else:
            cursor.execute("SELECT id, slug, url FROM anime_queue WHERE status = 'pending'")
        pending_rows = cursor.fetchall()

        cursor.execute("SELECT COUNT(*) FROM anime_queue")
        total_queue = cursor.fetchone()[0]

        cursor.execute("SELECT COUNT(*) FROM anime")
        already_done = cursor.fetchone()[0]

        logger.info(f"Queue Status: Total={total_queue} | Already Completed={already_done} | Pending This Run={len(pending_rows)}")

        if not pending_rows:
            logger.info("All anime in queue are already processed!")
            export_to_json(conn)
            return

        # Queue setup
        queue = asyncio.Queue()
        for row in pending_rows:
            await queue.put(row)

        progress_counter = [already_done]
        lock = asyncio.Lock()
        num_workers = 3  # safe concurrent worker limit

        workers = []
        for i in range(num_workers):
            p = proxy_pool[i % len(proxy_pool)] if proxy_pool else None
            w_safe_client = SafeHttpClient(proxy=p)
            w_anime_scraper = AnimeScraper(w_safe_client)
            w_img_client = httpx.AsyncClient(
                headers={"User-Agent": SafeHttpClient.DEFAULT_HEADERS["User-Agent"]},
                timeout=15.0,
                proxy=p,
                limits=httpx.Limits(max_keepalive_connections=10, max_connections=15),
            )
            worker_clients.append((w_safe_client, w_img_client))
            workers.append(
                asyncio.create_task(
                    worker_scrape_details(
                        i + 1,
                        queue,
                        w_anime_scraper,
                        w_img_client,
                        conn,
                        total_queue,
                        progress_counter,
                        lock,
                    )
                )
            )

        await asyncio.gather(*workers)
        logger.info("Scraping batch completed successfully.")

        export_to_json(conn)

    finally:
        await safe_client.close()
        for s_c, i_c in worker_clients:
            await s_c.close()
            await i_c.aclose()
        conn.close()


if __name__ == "__main__":
    test_limit = None
    if len(sys.argv) > 1 and sys.argv[1].isdigit():
        test_limit = int(sys.argv[1])
        logger.info(f"Running in test mode with limit = {test_limit}")
    asyncio.run(main(limit=test_limit))
