import os
import sys
import re
import json
import sqlite3
import asyncio
import logging
import random
import shutil
from urllib.parse import urlparse
import httpx
from bs4 import BeautifulSoup
from PIL import Image
import dotenv

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PROJECT_ROOT = os.path.dirname(BASE_DIR)
DATA_DIR = os.path.join(BASE_DIR, "data")
COVERS_ADULT_DIR = os.path.join(DATA_DIR, "covers_adult")
DB_PATH = os.path.join(DATA_DIR, "anime_catalog.db")

ANDROID_ROOT = os.path.join(os.path.dirname(BASE_DIR), "android")
ANDROID_COVERS_ADULT = os.path.join(ANDROID_ROOT, r"app\src\main\assets\covers_adult")
ANDROID_DB_PATH = os.path.join(ANDROID_ROOT, r"app\src\main\assets\databases\anime_catalog.db")

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    handlers=[logging.StreamHandler(sys.stdout)],
)
logger = logging.getLogger("MALAdultSync")

dotenv.load_dotenv(os.path.join(PROJECT_ROOT, ".env"))
GEMINI_KEY = os.getenv("GEMINI_API_KEY", "").strip()
raw_proxies = os.getenv("WEBSHARE_PROXIES", "").split(",")
proxy_pool = [f"http://{p.strip()}" for p in raw_proxies if p.strip()]


def init_database(conn: sqlite3.Connection):
    """Create adult_anime table with identical schema."""
    os.makedirs(COVERS_ADULT_DIR, exist_ok=True)
    if os.path.exists(ANDROID_ROOT):
        os.makedirs(ANDROID_COVERS_ADULT, exist_ok=True)

    cursor = conn.cursor()
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS adult_anime (
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
        rating TEXT DEFAULT 'Dewasa',
        score TEXT,
        genres TEXT,
        studios TEXT,
        synopsis TEXT,
        cover_url TEXT,
        cover_file TEXT,
        otak_url TEXT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        related_anime TEXT DEFAULT '[]'
    );
    """)

    cursor.execute("""
    CREATE TABLE IF NOT EXISTS adult_queue (
        id INTEGER PRIMARY KEY,
        title TEXT,
        synopsis_en TEXT,
        type TEXT,
        anime_status TEXT,
        season TEXT,
        episodes TEXT,
        duration TEXT,
        score TEXT,
        genres TEXT,
        studios TEXT,
        cover_url TEXT,
        queue_status TEXT DEFAULT 'pending'
    );
    """)
    conn.commit()


async def scrape_all_genre_pages(conn: sqlite3.Connection):
    """Scrape all 17 pages of MAL genre 12 (Hentai) into adult_queue with retries and proxy rotation."""
    cursor = conn.cursor()
    cursor.execute("SELECT COUNT(*) FROM adult_queue")
    existing = cursor.fetchone()[0]
    if existing >= 1500:
        logger.info(f"Katalog MAL sudah terindeks ({existing} judul). Melompati langkah indexing.")
        return

    logger.info("Mengindeks seluruh katalog genre dari MyAnimeList (17 Halaman)...")

    for page in range(1, 18):
        url = f"https://myanimelist.net/anime/genre/12/Hentai?page={page}"
        page_saved = False

        for attempt in range(4):
            proxy = random.choice(proxy_pool) if proxy_pool else None
            try:
                async with httpx.AsyncClient(
                    headers={"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/124.0.0.0"},
                    proxy=proxy,
                    timeout=15.0,
                    follow_redirects=True,
                ) as client:
                    resp = await client.get(url)
                    if resp.status_code == 200:
                        soup = BeautifulSoup(resp.text, "html.parser")
                        cards = soup.select(".seasonal-anime")
                        added = 0
                        for card in cards:
                            t_link = card.select_one("a.link-title, .title a, a[href*='/anime/']")
                            if not t_link:
                                continue
                            href = t_link.get("href", "")
                            match = re.search(r"/anime/(\d+)", href)
                            if not match:
                                continue
                            anime_id = int(match.group(1))
                            title = t_link.get_text(strip=True)

                            syn_el = card.select_one(".synopsis, .preline")
                            synopsis = syn_el.get_text(strip=True) if syn_el else ""

                            score_el = card.select_one(".score")
                            score = score_el.get_text(strip=True) if score_el else ""

                            img_el = card.select_one("img")
                            img_url = img_el.get("data-src") or img_el.get("src") or "" if img_el else ""

                            info_el = card.select_one(".info")
                            info_spans = [s.get_text(strip=True) for s in info_el.select(".item")] if info_el else []
                            first_span = info_spans[0] if info_spans else ""

                            anime_type = "OVA"
                            if "ONA" in first_span:
                                anime_type = "ONA"
                            elif "Movie" in first_span:
                                anime_type = "Movie"
                            elif "Special" in first_span:
                                anime_type = "Special"
                            elif "TV" in first_span:
                                anime_type = "TV"

                            year_match = re.search(r"\b(19\d\d|20\d\d)\b", first_span)
                            season = year_match.group(1) if year_match else "-"

                            full_info = " ".join(info_spans).lower()
                            anime_status = "Selesai Tayang"
                            if "not yet" in full_info:
                                anime_status = "Akan Tayang"
                            elif "air" in full_info and "finish" not in full_info:
                                anime_status = "Sedang Tayang"
                            elif "finish" in full_info:
                                anime_status = "Selesai Tayang"

                            eps_match = re.search(r"(\d+)\s*ep", full_info)
                            episodes = eps_match.group(1) if eps_match else "?"

                            dur_match = re.search(r"(\d+)\s*min", full_info)
                            duration = f"{dur_match.group(1)} Menit" if dur_match else "-"

                            genres = [g.get_text(strip=True) for g in card.select(".genre a")]
                            genres_str = ", ".join(genres) if genres else "Hentai"

                            studio_el = card.select_one(".property")
                            studios = ""
                            if studio_el:
                                st_links = [s.get_text(strip=True) for s in studio_el.select("a")]
                                studios = ", ".join(st_links)

                            cursor.execute("""
                            INSERT OR IGNORE INTO adult_queue (
                                id, title, synopsis_en, type, anime_status, season, episodes, duration, score, genres, studios, cover_url, queue_status
                            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'pending')
                            """, (anime_id, title, synopsis, anime_type, anime_status, season, episodes, duration, score, genres_str, studios, img_url))
                            added += 1

                        conn.commit()
                        logger.info(f"Halaman {page}/17 terindeks ({added} anime).")
                        page_saved = True
                        break
                    else:
                        logger.warning(f"Halaman {page} return status {resp.status_code}, mencoba lagi...")
            except Exception as e:
                logger.warning(f"Error pada halaman {page} (Percobaan {attempt+1}/4): {e}")
                await asyncio.sleep(2.0)

        if not page_saved:
            logger.error(f"Gagal mengindeks halaman {page} setelah 4 kali percobaan.")
        await asyncio.sleep(random.uniform(1.0, 1.8))

    cursor.execute("SELECT COUNT(*) FROM adult_queue")
    total = cursor.fetchone()[0]
    logger.info(f"Indexing selesai. Total {total} anime masuk antrean.")


async def translate_with_gemini(client: httpx.AsyncClient, text_list: list[str]) -> list[str]:
    """Translate batch of synopses using Gemini Flash Lite with character name protection, safety filter bypass, and rate-limit backoff."""
    if not GEMINI_KEY:
        return text_list

    prompt = (
        "Terjemahkan sinopsis anime berikut dari Bahasa Inggris ke Bahasa Indonesia secara alami, baku, dan mengalir.\n"
        "ATURAN MUTLAK:\n"
        "1. DILARANG KERAS menerjemahkan nama karakter (seperti Yuki, Kuro, Asagi, Misaki, Sakura), nama tempat, nama organisasi, atau istilah anime/Jepang (seperti Taimanin, Ninja, OVA, Sensei, Senpai).\n"
        "2. Pertahankan urutan teks. Pisahkan setiap hasil terjemahan dengan pembatas persis: [===SELESAI===]\n\n"
        "Teks yang harus diterjemahkan:\n"
    )

    for i, t in enumerate(text_list):
        clean_t = t.replace("\n", " ").strip()
        prompt += f"[{i+1}] {clean_t}\n"

    url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-lite-latest:generateContent?key={GEMINI_KEY}"
    payload = {
        "contents": [{"parts": [{"text": prompt}]}],
        "safetySettings": [
            {"category": "HARM_CATEGORY_SEXUALLY_EXPLICIT", "threshold": "BLOCK_NONE"},
            {"category": "HARM_CATEGORY_HATE_SPEECH", "threshold": "BLOCK_NONE"},
            {"category": "HARM_CATEGORY_HARASSMENT", "threshold": "BLOCK_NONE"},
            {"category": "HARM_CATEGORY_DANGEROUS_CONTENT", "threshold": "BLOCK_NONE"},
        ],
    }

    for attempt in range(3):
        try:
            resp = await client.post(url, json=payload, timeout=25.0)
            if resp.status_code == 200:
                data = resp.json()
                candidates = data.get("candidates", [])
                if candidates:
                    first_cand = candidates[0]
                    content = first_cand.get("content", {})
                    parts_list = content.get("parts", [])
                    if parts_list and "text" in parts_list[0]:
                        result_text = parts_list[0]["text"].strip()
                        parts = [p.strip() for p in result_text.split("[===SELESAI===]") if p.strip()]
                        if len(parts) == len(text_list):
                            cleaned = []
                            for p in parts:
                                cleaned.append(re.sub(r"^\[\d+\]\s*", "", p).strip())
                            return cleaned
            elif resp.status_code == 429:
                logger.warning(f"Rate limit Gemini (429). Menunggu 15 detik sebelum mencoba lagi (Percobaan {attempt+1}/3)...")
                await asyncio.sleep(15.0)
                continue
            else:
                logger.warning(f"Gemini response code {resp.status_code}: {resp.text[:100]}")
        except Exception as e:
            logger.warning(f"Gemini translation attempt {attempt+1} error: {e}")
            await asyncio.sleep(3.0)

    # Fallback to original text if API error
    return text_list


async def download_and_compress_cover(client: httpx.AsyncClient, img_url: str, anime_id: int) -> str:
    """Download cover image and save directly as optimized WebP (250x375)."""
    if not img_url:
        return ""

    file_name = f"{anime_id}.webp"
    local_path = os.path.join(COVERS_ADULT_DIR, file_name)

    if os.path.exists(local_path) and os.path.getsize(local_path) > 500:
        return file_name

    try:
        resp = await client.get(img_url, timeout=10.0)
        if resp.status_code == 200:
            import io
            img_bytes = resp.content
            with Image.open(io.BytesIO(img_bytes)) as img:
                if img.mode in ("RGBA", "P"):
                    img = img.convert("RGB")
                img.thumbnail((250, 375), Image.Resampling.LANCZOS)
                img.save(local_path, format="WEBP", quality=75, method=4)

            # Sync to Android assets
            if os.path.exists(ANDROID_COVERS_ADULT):
                shutil.copy2(local_path, os.path.join(ANDROID_COVERS_ADULT, file_name))

            return file_name
    except Exception as e:
        logger.warning(f"Gagal download cover {anime_id}: {e}")
    return ""


async def process_translation_and_storage(conn: sqlite3.Connection):
    """Process queue in batches, translate via Gemini, save covers and DB."""
    cursor = conn.cursor()
    cursor.execute("""
    SELECT id, title, synopsis_en, type, anime_status, season, episodes, duration, score, genres, studios, cover_url 
    FROM adult_queue 
    WHERE queue_status = 'pending'
    ORDER BY id ASC
    """)
    pending_rows = cursor.fetchall()

    cursor.execute("SELECT COUNT(*) FROM adult_queue")
    total_items = cursor.fetchone()[0]

    cursor.execute("SELECT COUNT(*) FROM adult_anime")
    already_done = cursor.fetchone()[0]

    logger.info(f"Status Pemrosesan: Total={total_items} | Sudah Selesai={already_done} | Menunggu={len(pending_rows)}")

    if not pending_rows:
        logger.info("Seluruh anime dewasa sudah selesai diproses!")
        return

    # Dedicated clients
    gemini_client = httpx.AsyncClient(timeout=25.0)
    img_client = httpx.AsyncClient(
        headers={"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/124.0.0.0"},
        timeout=12.0,
    )

    BATCH_SIZE = 8
    counter = already_done

    try:
        for i in range(0, len(pending_rows), BATCH_SIZE):
            batch = pending_rows[i : i + BATCH_SIZE]
            synopses = [row[2] for row in batch]

            # Translate batch with Gemini
            translations = await translate_with_gemini(gemini_client, synopses)

            for j, row in enumerate(batch):
                anime_id, title, _, anime_type, anime_status, season, episodes, duration, score, genres, studios, cover_url = row
                synopsis_id = translations[j] if j < len(translations) else row[2]

                # Download & compress cover
                cover_file = await download_and_compress_cover(img_client, cover_url, anime_id)

                mal_url = f"https://myanimelist.net/anime/{anime_id}"
                cursor.execute("""
                INSERT OR REPLACE INTO adult_anime (
                    id, title, japanese_title, english_title, slug, type, status, season,
                    episodes, duration, rating, score, genres, studios, synopsis,
                    cover_url, cover_file, otak_url, related_anime
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Dewasa', ?, ?, ?, ?, ?, ?, ?, '[]')
                """, (
                    anime_id,
                    title,
                    title,
                    title,
                    re.sub(r"[^a-zA-Z0-9]+", "-", title.lower()).strip("-"),
                    anime_type,
                    anime_status,
                    season,
                    episodes,
                    duration,
                    score,
                    genres,
                    studios,
                    synopsis_id,
                    cover_url,
                    cover_file,
                    mal_url,
                ))

                cursor.execute("UPDATE adult_queue SET queue_status = 'completed' WHERE id = ?", (anime_id,))
                counter += 1

            conn.commit()
            if counter % 24 == 0 or counter == total_items:
                pct = (counter / total_items) * 100
                logger.info(f"[Progress: {counter}/{total_items} ({pct:.1f}%)] Terakhir: {batch[-1][1]}")

            # Jeda sopan agar hemat dan patuh pada limit 15 RPM Gemini Free Tier
            await asyncio.sleep(random.uniform(2.5, 4.0))

    finally:
        await gemini_client.aclose()
        await img_client.aclose()

    # Sync updated database directly to Android Studio assets
    if os.path.exists(os.path.dirname(ANDROID_DB_PATH)):
        shutil.copy2(DB_PATH, ANDROID_DB_PATH)
        logger.info(f"Database berhasil diperbarui dan disinkronkan ke Android Studio:\n{ANDROID_DB_PATH}")


async def main():
    conn = sqlite3.connect(DB_PATH)
    init_database(conn)

    # Langkah 1: Indeks seluruh halaman MAL
    await scrape_all_genre_pages(conn)

    # Langkah 2: Terjemahkan dengan Gemini, download cover WebP, simpan ke SQLite
    await process_translation_and_storage(conn)

    conn.close()
    logger.info("=== PROSES SINKRONISASI SELESAI SEMPURNA ===")


if __name__ == "__main__":
    asyncio.run(main())
