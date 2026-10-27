import os
import sys
import json
import sqlite3
import asyncio
import logging
import random
import shutil
import httpx
from bs4 import BeautifulSoup
import dotenv

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PROJECT_ROOT = os.path.dirname(BASE_DIR)
DATA_DIR = os.path.join(BASE_DIR, "data")
DB_PATH = os.path.join(DATA_DIR, "anime_catalog.db")
ANDROID_DB_PATH = os.path.join(os.path.dirname(BASE_DIR), "android", "app", "src", "main", "assets", "databases", "anime_catalog.db")

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    handlers=[logging.StreamHandler(sys.stdout)],
)
logger = logging.getLogger("RelatedAnimeSync")

dotenv.load_dotenv(os.path.join(PROJECT_ROOT, ".env"))
raw_proxies = os.getenv("WEBSHARE_PROXIES", "").split(",")
proxy_pool = [f"http://{p.strip()}" for p in raw_proxies if p.strip()]


def init_column(conn: sqlite3.Connection):
    """Add related_anime column if not present."""
    cursor = conn.cursor()
    cursor.execute("PRAGMA table_info(anime)")
    cols = [row[1] for row in cursor.fetchall()]
    if "related_anime" not in cols:
        logger.info("Adding column 'related_anime' to anime table...")
        cursor.execute("ALTER TABLE anime ADD COLUMN related_anime TEXT")
        conn.commit()


def parse_related_from_html(html: str) -> list[dict]:
    """Parse related anime links and types across ALL categories (TV, Movie, OVA, Special, etc.)."""
    soup = BeautifulSoup(html, "html.parser")
    related_list = []

    for h4 in soup.find_all("h4", class_="content-header"):
        if "anime terkait" in h4.get_text().lower():
            curr = h4.find_next_sibling()
            while curr and curr.name != "h4":
                if curr.find(class_="anime-related"):
                    type_span = curr.find("span", class_="anime-related")
                    current_type = type_span.get_text(strip=True) if type_span else "Anime"

                    for a in curr.find_all("a", class_="anime-related"):
                        # Skip self-active link
                        if "active" in a.get("class", []):
                            continue
                        href = a.get("href", "")
                        title = a.get_text(strip=True)
                        parts = href.split("/anime/view/")[-1].strip("/").split("/")
                        if parts and parts[0].isdigit():
                            related_list.append({
                                "id": int(parts[0]),
                                "title": title,
                                "type": current_type,
                            })
                curr = curr.find_next_sibling()
            break
    return related_list


async def worker_fetch(
    worker_id: int,
    queue: asyncio.Queue,
    conn: sqlite3.Connection,
    total_items: int,
    counter: list[int],
    lock: asyncio.Lock,
):
    proxy = proxy_pool[worker_id % len(proxy_pool)] if proxy_pool else None
    async with httpx.AsyncClient(
        headers={"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/124.0.0.0"},
        proxy=proxy,
        timeout=15.0,
        follow_redirects=True,
    ) as client:
        while not queue.empty():
            try:
                anime_id, slug, otak_url = await queue.get()
            except asyncio.QueueEmpty:
                break

            # Special bypass for known infinite redirect on OtakOtaku
            if anime_id == 1382:
                async with lock:
                    cursor = conn.cursor()
                    cursor.execute("UPDATE anime SET related_anime = '[]' WHERE id = 1382")
                    conn.commit()
                    counter[0] += 1
                queue.task_done()
                continue

            target_url = otak_url if otak_url else f"https://otakotaku.com/anime/view/{anime_id}"

            try:
                resp = await client.get(target_url)
                related_data = []
                if resp.status_code == 200:
                    related_data = parse_related_from_html(resp.text)

                json_str = json.dumps(related_data, ensure_ascii=False)

                async with lock:
                    cursor = conn.cursor()
                    cursor.execute("UPDATE anime SET related_anime = ? WHERE id = ?", (json_str, anime_id))
                    conn.commit()
                    counter[0] += 1
                    curr = counter[0]
                    if curr % 50 == 0 or curr == total_items:
                        pct = (curr / total_items) * 100
                        logger.info(f"[Progress: {curr}/{total_items} ({pct:.1f}%)] Anime ID {anime_id} synced.")

                await asyncio.sleep(random.uniform(0.6, 1.2))

            except Exception as e:
                logger.warning(f"Worker {worker_id} error on ID {anime_id}: {e}. Retrying later...")
                await queue.put((anime_id, slug, otak_url))
                await asyncio.sleep(2.0)
            finally:
                queue.task_done()


async def main():
    conn = sqlite3.connect(DB_PATH)
    init_column(conn)

    cursor = conn.cursor()
    cursor.execute("SELECT COUNT(*) FROM anime")
    total_catalog = cursor.fetchone()[0]

    cursor.execute("SELECT id, slug, otak_url FROM anime ORDER BY id ASC")
    pending = cursor.fetchall()

    logger.info(f"Full Relational Sync: Total {len(pending)} anime to process across all categories (TV, Movie, OVA, Special).")

    queue = asyncio.Queue()
    for row in pending:
        await queue.put(row)

    counter = [0]
    lock = asyncio.Lock()
    num_workers = 5

    workers = [
        asyncio.create_task(worker_fetch(i, queue, conn, total_catalog, counter, lock))
        for i in range(num_workers)
    ]

    await asyncio.gather(*workers)
    logger.info("All related anime data synchronized successfully.")

    conn.close()

    # Re-export JSON
    logger.info("Updating JSON backup...")
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    rows = [dict(r) for r in conn.cursor().execute("SELECT * FROM anime ORDER BY id ASC").fetchall()]
    with open(os.path.join(DATA_DIR, "anime_catalog.json"), "w", encoding="utf-8") as f:
        json.dump(rows, f, ensure_ascii=False, indent=2)
    conn.close()

    # Copy updated DB directly to Android Studio assets
    if os.path.exists(os.path.dirname(ANDROID_DB_PATH)):
        shutil.copy2(DB_PATH, ANDROID_DB_PATH)
        logger.info(f"Updated anime_catalog.db successfully copied to Android Studio:\n{ANDROID_DB_PATH}")


if __name__ == "__main__":
    asyncio.run(main())
