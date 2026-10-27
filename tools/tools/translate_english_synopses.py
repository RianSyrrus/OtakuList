import os
import re
import sqlite3
import asyncio
import logging
import random
import httpx

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger("GoogleTranslateFixer")

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LOCAL_DB = os.path.join(BASE_DIR, "data", "anime_catalog.db")
ANDROID_DB = os.path.join(os.path.dirname(BASE_DIR), "android", "app", "src", "main", "assets", "databases", "anime_catalog.db")

ENG_STOP_WORDS = {
    "the", "and", "of", "to", "a", "in", "is", "that", "for", "with",
    "on", "his", "her", "was", "after", "he", "she", "an", "at", "by", "from"
}

def is_english_synopsis(text: str) -> bool:
    if not text:
        return False
    words = set(re.findall(r"\b[a-z]+\b", text.lower()))
    matches = words.intersection(ENG_STOP_WORDS)
    return len(matches) >= 3 or "[written by mal rewrite]" in text.lower()

async def translate_text_google(client: httpx.AsyncClient, text: str) -> str:
    if not text.strip():
        return ""
    
    clean_text = re.sub(r"\[Written by MAL Rewrite\].*", "", text, flags=re.IGNORECASE).strip()
    if not clean_text:
        return ""

    url = "https://translate.googleapis.com/translate_a/single"
    params = {
        "client": "gtx",
        "sl": "en",
        "tl": "id",
        "dt": "t",
        "q": clean_text,
    }

    for attempt in range(3):
        try:
            resp = await client.get(url, params=params, timeout=12.0)
            if resp.status_code == 200:
                data = resp.json()
                if data and len(data) > 0 and data[0]:
                    result = "".join([seg[0] for seg in data[0] if seg and len(seg) > 0 and seg[0]])
                    return result.strip()
            elif resp.status_code == 429:
                await asyncio.sleep(2.0)
        except Exception:
            await asyncio.sleep(1.0)

    return clean_text

async def fix_all_translations():
    conn = sqlite3.connect(LOCAL_DB)
    c = conn.cursor()
    c.execute("SELECT id, title, synopsis FROM adult_anime ORDER BY id ASC")
    rows = c.fetchall()

    target_items = []
    for r in rows:
        anime_id, title, syn = r
        if is_english_synopsis(syn):
            target_items.append((anime_id, title, syn))

    total = len(target_items)
    logger.info(f"Ditemukan {total} anime dengan sinopsis Bahasa Inggris yang perlu diterjemahkan.")

    if total == 0:
        logger.info("Seluruh sinopsis anime dewasa sudah dalam Bahasa Indonesia!")
        conn.close()
        return

    logger.info("Memulai penerjemahan super cepat menggunakan Google Translate NMT Engine...")
    async with httpx.AsyncClient(
        headers={"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/124.0.0.0"},
        timeout=15.0
    ) as client:
        counter = 0
        for anime_id, title, raw_syn in target_items:
            translated = await translate_text_google(client, raw_syn)
            c.execute("UPDATE adult_anime SET synopsis = ? WHERE id = ?", (translated, anime_id))
            counter += 1

            if counter % 10 == 0 or counter == total:
                conn.commit()
                pct = (counter / total) * 100
                logger.info(f"[Progress: {counter}/{total} ({pct:.1f}%)] Terakhir: {title}")

            await asyncio.sleep(random.uniform(0.12, 0.25))

    conn.commit()
    conn.close()

    # Re-export to Android DB
    try:
        from tools.apply_option2 import apply_offset_and_watchlist
    except ImportError:
        from apply_option2 import apply_offset_and_watchlist
    logger.info("Menyinkronkan hasil database ke aset Android...")
    apply_offset_and_watchlist()
    logger.info("=== PROSES PENERJEMAHAN SELESAI SEMPURNA ===")

if __name__ == "__main__":
    asyncio.run(fix_all_translations())
