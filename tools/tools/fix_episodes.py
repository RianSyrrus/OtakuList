import os
import sys
import sqlite3
import asyncio
import logging
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
logger = logging.getLogger("EpisodeFixer")

dotenv.load_dotenv(os.path.join(PROJECT_ROOT, ".env"))
raw_proxies = os.getenv("WEBSHARE_PROXIES", "").split(",")
proxy_pool = [f"http://{p.strip()}" for p in raw_proxies if p.strip()]


async def fetch_page_episodes(client: httpx.AsyncClient, page: int) -> list[tuple[int, str]]:
    url = f"https://otakotaku.com/anime/feed/{page}" if page > 1 else "https://otakotaku.com/anime/feed"
    results = []
    try:
        resp = await client.get(url, timeout=12.0)
        if resp.status_code == 200:
            soup = BeautifulSoup(resp.text, "html.parser")
            for row in soup.select(".row"):
                title_a = row.select_one(".anime-title a")
                table = row.select_one(".col-md-3 table")
                if title_a and table:
                    href = title_a.get("href", "")
                    if "/anime/view/" in href:
                        parts = href.split("/anime/view/")[-1].strip("/").split("/")
                        if parts and parts[0].isdigit():
                            anime_id = int(parts[0])
                            eps = ""
                            for tr in table.find_all("tr"):
                                tds = [td.get_text(strip=True) for td in tr.find_all(["th", "td"])]
                                if len(tds) >= 2 and "eps" in tds[0].lower():
                                    eps = tds[1]
                                    break
                            results.append((anime_id, eps))
    except Exception as e:
        logger.warning(f"Error fetching page {page}: {e}")
    return results


async def main():
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()

    logger.info("Starting episode count synchronization from 168 feed pages...")
    num_workers = 4
    queue = asyncio.Queue()
    for p in range(1, 169):
        await queue.put(p)

    total_updated = [0]
    lock = asyncio.Lock()

    async def worker(w_id: int):
        proxy = proxy_pool[w_id % len(proxy_pool)] if proxy_pool else None
        async with httpx.AsyncClient(
            headers={"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/124.0.0.0"},
            proxy=proxy,
            timeout=15.0,
        ) as client:
            while not queue.empty():
                try:
                    page = await queue.get()
                except asyncio.QueueEmpty:
                    break

                items = await fetch_page_episodes(client, page)
                if items:
                    async with lock:
                        for anime_id, eps in items:
                            cursor.execute("UPDATE anime SET episodes = ? WHERE id = ?", (eps, anime_id))
                        conn.commit()
                        total_updated[0] += len(items)
                        if page % 25 == 0 or page == 168:
                            logger.info(f"Page {page}/168 processed. Total anime updated: {total_updated[0]}")
                queue.task_done()
                await asyncio.sleep(0.4)

    workers = [asyncio.create_task(worker(i)) for i in range(num_workers)]
    await asyncio.gather(*workers)
    conn.close()

    # Re-export JSON
    import json
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    rows = [dict(r) for r in conn.cursor().execute("SELECT * FROM anime ORDER BY id ASC").fetchall()]
    with open(os.path.join(DATA_DIR, "anime_catalog.json"), "w", encoding="utf-8") as f:
        json.dump(rows, f, ensure_ascii=False, indent=2)
    conn.close()

    # Copy updated DB to Android project
    import shutil
    shutil.copy2(DB_PATH, ANDROID_DB_PATH)
    logger.info(f"Successfully copied updated database to {ANDROID_DB_PATH}")


if __name__ == "__main__":
    asyncio.run(main())
