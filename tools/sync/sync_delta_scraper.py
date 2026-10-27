import os
import sys
import json
import shutil
import sqlite3
import asyncio
import logging
import random
import re
from typing import Optional
from bs4 import BeautifulSoup
import httpx
from PIL import Image

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PROJECT_ROOT = os.path.dirname(BASE_DIR)
if BASE_DIR not in sys.path:
    sys.path.insert(0, BASE_DIR)

from client import SafeHttpClient
from scrapers.anime_scraper import AnimeScraper, extract_id_and_slug, parse_feed_anime_row

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    handlers=[logging.StreamHandler(sys.stdout)],
)
logger = logging.getLogger("DeltaScraper")

DATA_DIR = os.path.join(BASE_DIR, "data")
COVERS_DIR = os.path.join(DATA_DIR, "covers")
COVERS_ADULT_DIR = os.path.join(DATA_DIR, "covers_adult")
PATCH_DIR = os.path.join(DATA_DIR, "patch")
DB_PATH = os.path.join(DATA_DIR, "anime_catalog.db")
DELTA_RECORD_FILE = os.path.join(PATCH_DIR, "pending_delta.json")

ANDROID_ROOT = os.path.join(os.path.dirname(BASE_DIR), "android")
ANDROID_COVERS_DIR = os.path.join(ANDROID_ROOT, r"app\src\main\assets\covers")
ANDROID_COVERS_ADULT_DIR = os.path.join(ANDROID_ROOT, r"app\src\main\assets\covers_adult")

ID_OFFSET = 100000


def ensure_directories():
    os.makedirs(COVERS_DIR, exist_ok=True)
    os.makedirs(COVERS_ADULT_DIR, exist_ok=True)
    os.makedirs(PATCH_DIR, exist_ok=True)
    if not os.path.exists(DB_PATH):
        android_db = os.path.join(ANDROID_ROOT, 'app', 'src', 'main', 'assets', 'databases', 'anime_catalog.db')
        if os.path.exists(android_db):
            shutil.copy2(android_db, DB_PATH)
            logger.info(f'Initialized local catalog database from Android assets: {DB_PATH}')


async def translate_text_google(client: httpx.AsyncClient, text: str) -> str:
    if not text or not text.strip():
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
    for _ in range(3):
        try:
            resp = await client.get(url, params=params, timeout=10.0)
            if resp.status_code == 200:
                data = resp.json()
                if data and len(data) > 0 and data[0]:
                    return "".join([seg[0] for seg in data[0] if seg and len(seg) > 0 and seg[0]]).strip()
            elif resp.status_code == 429:
                await asyncio.sleep(2.0)
        except Exception:
            await asyncio.sleep(1.0)
    return clean_text


def save_webp_cover(image_bytes: bytes, dest_path: str, max_size=(225, 320)):
    try:
        from io import BytesIO
        img = Image.open(BytesIO(image_bytes))
        if img.mode in ("RGBA", "P"):
            img = img.convert("RGB")
        img.thumbnail(max_size, Image.Resampling.LANCZOS)
        img.save(dest_path, "WEBP", quality=80, method=6)
        return True
    except Exception as e:
        logger.error(f"Gagal mengonversi cover {dest_path}: {e}")
        return False


async def download_and_save_cover(
    client: httpx.AsyncClient,
    cover_url: str,
    dest_path: str,
    asset_backup_path: Optional[str] = None,
    max_retries: int = 3
) -> bool:
    if not cover_url or not cover_url.strip():
        return False

    cover_url = cover_url.strip()
    if cover_url.startswith("//"):
        cover_url = "https:" + cover_url
    elif cover_url.startswith("/"):
        cover_url = f"https://otakotaku.com{cover_url}"

    # Jika file valid sudah ada di disk, langsung gunakan
    if os.path.exists(dest_path) and os.path.getsize(dest_path) > 1000:
        if asset_backup_path and os.path.exists(os.path.dirname(asset_backup_path)):
            if not os.path.exists(asset_backup_path) or os.path.getsize(asset_backup_path) == 0:
                try:
                    shutil.copy2(dest_path, asset_backup_path)
                except Exception:
                    pass
        return True

    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36",
        "Referer": "https://otakotaku.com/" if "otakotaku" in cover_url else "https://myanimelist.net/"
    }

    for attempt in range(1, max_retries + 1):
        try:
            resp = await client.get(cover_url, headers=headers, timeout=20.0, follow_redirects=True)
            if resp.status_code == 200 and len(resp.content) > 500:
                success = save_webp_cover(resp.content, dest_path)
                if success:
                    if asset_backup_path and os.path.exists(os.path.dirname(asset_backup_path)):
                        try:
                            shutil.copy2(dest_path, asset_backup_path)
                        except Exception:
                            pass
                    return True
            logger.warning(f"Percobaan {attempt}/{max_retries} unduh cover status {resp.status_code}: {cover_url}")
        except Exception as e:
            logger.warning(f"Percobaan {attempt}/{max_retries} unduh cover gagal ({e}): {cover_url}")
        await asyncio.sleep(1.0 * attempt)

    return False


async def run_delta_sync():
    ensure_directories()
    conn = sqlite3.connect(DB_PATH)
    cur = conn.cursor()

    cur.execute("SELECT id, status, episodes FROM anime")
    existing_regular = {row[0]: (row[1], row[2]) for row in cur.fetchall()}

    cur.execute("SELECT id, status, episodes FROM adult_anime")
    existing_adult = {row[0]: (row[1], row[2]) for row in cur.fetchall()}

    logger.info(f"Database saat ini memuat {len(existing_regular)} anime reguler dan {len(existing_adult)} dewasa.")

    safe_client = SafeHttpClient()
    anime_scraper = AnimeScraper(safe_client)
    http_client = await safe_client.get_client()

    new_regular_ids = []
    updated_regular_ids = []
    new_adult_ids = []
    seen_feed_ids = set()

    # =========================================================================
    # 1. Pindai Feed OtakOtaku (Halaman 1-4)
    # =========================================================================
    logger.info("=== 1. Memindai Feed Rilis Otak Otaku ===")
    consecutive_seen = 0
    max_consecutive_stop = 15

    for page in range(1, 5):
        if consecutive_seen >= max_consecutive_stop:
            logger.info(f"Early stop tercapai ({consecutive_seen} judul lama berturut-turut). Selesai memindai feed.")
            break

        path_desc = f"/anime/feed/{page}" if page > 1 else "/anime/feed"
        logger.info(f"Membaca {path_desc}...")
        try:
            feed_data = await anime_scraper.get_anime_feed(page=page)
            anime_items = feed_data.get("anime", [])
            if not anime_items:
                logger.info(f"Tidak ada anime lagi pada feed halaman {page}.")
                break

            for item in anime_items:
                anime_id = item.get("id")
                if not anime_id or anime_id in seen_feed_ids:
                    continue
                seen_feed_ids.add(anime_id)

                slug = item.get("slug", "")

                if anime_id in existing_regular:
                    old_status, old_eps = existing_regular[anime_id]
                    cur_status = item.get("status")
                    cur_eps = item.get("episodes")

                    if (cur_status and cur_status != old_status) or (cur_eps and cur_eps != old_eps):
                        cur.execute(
                            "UPDATE anime SET status = COALESCE(?, status), episodes = COALESCE(?, episodes) WHERE id = ?",
                            (cur_status, cur_eps, anime_id)
                        )
                        updated_regular_ids.append(anime_id)
                        logger.info(f"[Update Status] ID {anime_id}: {item.get('title')} -> {cur_status} ({cur_eps} eps)")
                        consecutive_seen = 0
                    else:
                        consecutive_seen += 1
                        if consecutive_seen >= max_consecutive_stop:
                            break
                else:
                    # Anime baru ditemukan!
                    consecutive_seen = 0
                    logger.info(f"[Anime Baru Ditemukan] ID {anime_id}: {item.get('title')}")
                    await asyncio.sleep(random.uniform(0.5, 1.0))

                    try:
                        detail = await anime_scraper.get_anime_detail(anime_id, slug)
                    except Exception as ex:
                        logger.error(f"Gagal mengambil detail ID {anime_id}: {ex}")
                        detail = {}

                    cover_file = f"{anime_id}.webp"
                    dest_cover = os.path.join(COVERS_DIR, cover_file)
                    asset_cover = os.path.join(ANDROID_COVERS_DIR, cover_file) if os.path.exists(ANDROID_COVERS_DIR) else None
                    cover_url = detail.get("cover") or item.get("image") or ""

                    if cover_url:
                        await download_and_save_cover(
                            client=http_client,
                            cover_url=cover_url,
                            dest_path=dest_cover,
                            asset_backup_path=asset_cover
                        )

                    # Format genres
                    genres_val = detail.get("genres", "")
                    if isinstance(genres_val, list):
                        genres_str = ", ".join([g.get("name", "") if isinstance(g, dict) else str(g) for g in genres_val if g])
                    else:
                        genres_str = str(genres_val)

                    # Format studios
                    studios_val = detail.get("studios", "")
                    if isinstance(studios_val, list):
                        studios_str = ", ".join([s.get("name", "") if isinstance(s, dict) else str(s) for s in studios_val if s])
                    else:
                        studios_str = str(studios_val)

                    synopsis_text = detail.get("synopsis") or item.get("synopsis") or ""

                    cur.execute("""
                    INSERT OR REPLACE INTO anime (
                        id, title, japanese_title, english_title, slug, type, status, season,
                        episodes, duration, rating, score, genres, studios, synopsis,
                        cover_url, cover_file, otak_url, created_at, related_anime
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)
                    """, (
                        anime_id,
                        (detail.get("title") or item.get("title")) or "Judul Tidak Diketahui",
                        detail.get("japanese_title", ""),
                        detail.get("english_title", ""),
                        slug,
                        detail.get("type") or item.get("type") or "",
                        detail.get("status") or item.get("status") or "",
                        detail.get("season") or item.get("season") or "",
                        detail.get("total_episodes") or item.get("episodes") or "",
                        detail.get("duration", ""),
                        detail.get("rating", ""),
                        detail.get("score") or item.get("score") or "",
                        genres_str,
                        studios_str,
                        synopsis_text,
                        cover_url,
                        cover_file,
                        f"https://otakotaku.com/anime/view/{anime_id}/{slug}",
                        json.dumps(detail.get("related_anime", []))
                    ))
                    new_regular_ids.append(anime_id)
                    existing_regular[anime_id] = (item.get("status"), item.get("episodes"))
                    conn.commit()

        except Exception as e:
            logger.error(f"Error memindai feed page {page}: {e}")

    # =========================================================================
    # 2. Pindai Seluruh Katalog Rilis MAL Dewasa (Genre 12 Hentai)
    # =========================================================================
    logger.info("=== 2. Memindai Seluruh Katalog Rilis MAL Dewasa (Genre 12) ===")
    try:
        mal_headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36",
            "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
            "Accept-Language": "en-US,en;q=0.9",
        }
        async with httpx.AsyncClient(headers=mal_headers, timeout=20.0, follow_redirects=True) as mal_client:
            for page in range(1, 25):
                mal_url = f"https://myanimelist.net/anime/genre/12/Hentai?page={page}"
                try:
                    resp = await mal_client.get(mal_url)
                    if resp.status_code != 200:
                        logger.warning(f"MAL page {page} status code: {resp.status_code}")
                        break

                    soup = BeautifulSoup(resp.text, "html.parser")
                    cards = soup.select(".seasonal-anime")
                    if not cards:
                        logger.info(f"Akhir halaman katalog MAL tercapai di halaman {page - 1}.")
                        break

                    for card in cards:
                        title_el = card.select_one("a.link-title, .title a, a[href*='/anime/']")
                        if not title_el:
                            continue
                        href = title_el.get("href", "")
                        mal_id_match = re.search(r"/anime/(\d+)", href)
                        if not mal_id_match:
                            continue

                        raw_id = int(mal_id_match.group(1))
                        adult_id = raw_id + ID_OFFSET

                        if adult_id in existing_adult:
                            continue

                        title = title_el.get_text(strip=True)
                        logger.info(f"[Adult Anime Baru Ditemukan] ID {adult_id} (MAL {raw_id}): {title}")

                        # Ekstraksi metadata dari kartu anime
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
                        studios = ", ".join([s.get_text(strip=True) for s in studio_el.select("a")]) if studio_el else ""

                        score_el = card.select_one(".score")
                        score = score_el.get_text(strip=True) if score_el else ""

                        synopsis_el = card.select_one("p.preline, .preline") or card.select_one(".synopsis")
                        raw_syn = synopsis_el.get_text(strip=True) if synopsis_el else ""
                        translated_syn = await translate_text_google(mal_client, raw_syn) if raw_syn else ""

                        cover_el = card.select_one(".image img, img")
                        cover_url = ""
                        if cover_el:
                            cover_url = cover_el.get("data-src") or cover_el.get("src") or ""

                        cover_file = f"{adult_id}.webp"
                        dest_cover = os.path.join(COVERS_ADULT_DIR, cover_file)
                        asset_adult_cover = os.path.join(ANDROID_COVERS_ADULT_DIR, cover_file) if os.path.exists(ANDROID_COVERS_ADULT_DIR) else None

                        if cover_url:
                            await download_and_save_cover(
                                client=mal_client,
                                cover_url=cover_url,
                                dest_path=dest_cover,
                                asset_backup_path=asset_adult_cover
                            )

                        cur.execute("""
                        INSERT OR REPLACE INTO adult_anime (
                            id, title, japanese_title, english_title, slug, type, status, season,
                            episodes, duration, rating, score, genres, studios, synopsis,
                            cover_url, cover_file, otak_url, created_at, related_anime
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)
                        """, (
                            adult_id,
                            title,
                            "",
                            "",
                            f"mal-{raw_id}",
                            anime_type,
                            anime_status,
                            season,
                            episodes,
                            duration,
                            "Dewasa",
                            score,
                            genres_str,
                            studios,
                            translated_syn,
                            cover_url,
                            cover_file,
                            href,
                            "[]"
                        ))
                        new_adult_ids.append(adult_id)
                        existing_adult[adult_id] = (anime_status, episodes)
                        conn.commit()
                        await asyncio.sleep(random.uniform(0.3, 0.6))

                except Exception as pe:
                    logger.warning(f"Error memindai MAL halaman {page}: {pe}")
                    break
    except Exception as me:
        logger.error(f"Error memindai MAL: {me}")

    conn.commit()

    # =========================================================================
    # 3. Simpan delta record & Tampilkan Rekap
    # =========================================================================
    total_new = len(new_regular_ids) + len(updated_regular_ids) + len(new_adult_ids)
    if total_new > 0:
        delta_data = {
            "new_regular_ids": list(set(new_regular_ids + updated_regular_ids)),
            "new_adult_ids": new_adult_ids,
            "total_items": total_new
        }
        with open(DELTA_RECORD_FILE, "w", encoding="utf-8") as f:
            json.dump(delta_data, f, indent=2)
    else:
        logger.info("Tidak ada data baru pada pemindaian ini. Berkas pending_delta dipertahankan.")

    print("\n" + "=" * 60)
    print("           HASIL PEMINDAIAN DATA DELTA")
    print("=" * 60)
    if total_new > 0:
        print(f"  [+] DITEMUKAN {total_new} DATA BARU / DIPERBARUI!")
        if new_regular_ids:
            print(f"\n  -- Anime Reguler Baru ({len(new_regular_ids)} judul) --")
            cur.execute(f"SELECT id, title FROM anime WHERE id IN ({','.join(['?']*len(new_regular_ids))})", new_regular_ids)
            for r in cur.fetchall():
                print(f"    * [ID {r[0]}] {r[1]}")
        if updated_regular_ids:
            print(f"\n  -- Anime Reguler Diperbarui ({len(updated_regular_ids)} judul) --")
            cur.execute(f"SELECT id, title, status, episodes FROM anime WHERE id IN ({','.join(['?']*len(updated_regular_ids))})", updated_regular_ids)
            for r in cur.fetchall():
                print(f"    * [ID {r[0]}] {r[1]} -> {r[2]} ({r[3]} eps)")
        if new_adult_ids:
            print(f"\n  -- Anime Dewasa Baru ({len(new_adult_ids)} judul) --")
            cur.execute(f"SELECT id, title FROM adult_anime WHERE id IN ({','.join(['?']*len(new_adult_ids))})", new_adult_ids)
            for r in cur.fetchall():
                print(f"    * [ID {r[0]}] {r[1]}")
    else:
        print("  [i] STATUS: 100% SINKRON & MUTAKHIR!")
        print("  - Seluruh anime di OtakOtaku dan MyAnimeList sudah ada di database.")
        print("  - Tidak ada data baru yang perlu diunduh.")
    print("=" * 60 + "\n")

    conn.close()


if __name__ == "__main__":
    asyncio.run(run_delta_sync())
