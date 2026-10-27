import os
import sqlite3
import shutil
import logging

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger("ExportAndroidDB")

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SOURCE_DB = os.path.join(BASE_DIR, "data", "anime_catalog.db")
TARGET_DB = os.path.join(os.path.dirname(BASE_DIR), "android", "app", "src", "main", "assets", "databases", "anime_catalog.db")
ROOM_IDENTITY_HASH = "e629f1b50af63fa8510bd04b7dbab401"

def export_clean_room_db():
    if not os.path.exists(SOURCE_DB):
        logger.error(f"Source database tidak ditemukan: {SOURCE_DB}")
        return

    logger.info("Membangun dan memformat database Android Room yang 100% valid...")
    temp_db = os.path.join(BASE_DIR, "data", "temp_room.db")
    if os.path.exists(temp_db):
        os.remove(temp_db)

    src_conn = sqlite3.connect(SOURCE_DB)
    src_cur = src_conn.cursor()

    dest_conn = sqlite3.connect(temp_db)
    dest_cur = dest_conn.cursor()

    # 1. Tabel anime
    dest_cur.execute("""
    CREATE TABLE anime (
        id INTEGER NOT NULL,
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
        created_at TEXT,
        related_anime TEXT,
        PRIMARY KEY(id)
    );
    """)

    src_cur.execute("""
    SELECT id, title, japanese_title, english_title, slug, type, status, season,
           episodes, duration, rating, score, genres, studios, synopsis,
           cover_url, cover_file, otak_url, CAST(created_at AS TEXT), related_anime
    FROM anime
    """)
    anime_rows = src_cur.fetchall()
    dest_cur.executemany("""
    INSERT INTO anime VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """, anime_rows)
    logger.info(f"Tabel anime: {len(anime_rows)} data berhasil dimuat.")

    # 2. Tabel adult_anime
    dest_cur.execute("""
    CREATE TABLE adult_anime (
        id INTEGER NOT NULL,
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
        created_at TEXT,
        related_anime TEXT,
        PRIMARY KEY(id)
    );
    """)

    src_cur.execute("""
    SELECT id, title, japanese_title, english_title, slug, type, status, season,
           episodes, duration, rating, score, genres, studios, synopsis,
           cover_url, cover_file, otak_url, CAST(created_at AS TEXT), related_anime
    FROM adult_anime
    """)
    adult_rows = src_cur.fetchall()
    dest_cur.executemany("""
    INSERT INTO adult_anime VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """, adult_rows)
    logger.info(f"Tabel adult_anime: {len(adult_rows)} data berhasil dimuat.")

    # 3. Tabel user_watchlist (Wajib ada untuk Room)
    dest_cur.execute("""
    CREATE TABLE user_watchlist (
        anime_id INTEGER NOT NULL,
        status TEXT NOT NULL,
        current_episode INTEGER NOT NULL DEFAULT 0,
        personal_score INTEGER NOT NULL DEFAULT 0,
        personal_note TEXT NOT NULL DEFAULT '',
        updated_at INTEGER NOT NULL DEFAULT 0,
        PRIMARY KEY(anime_id),
        FOREIGN KEY(anime_id) REFERENCES anime(id) ON UPDATE NO ACTION ON DELETE CASCADE
    );
    """)

    # 4. Tabel room_master_table (Wajib ada dengan hash identik Room)
    dest_cur.execute("""
    CREATE TABLE room_master_table (
        id INTEGER PRIMARY KEY,
        identity_hash TEXT
    );
    """)
    dest_cur.execute("INSERT INTO room_master_table (id, identity_hash) VALUES (42, ?)", (ROOM_IDENTITY_HASH,))

    dest_conn.commit()
    dest_cur.execute("VACUUM")

    dest_conn.close()
    src_conn.close()

    if os.path.exists(os.path.dirname(TARGET_DB)):
        shutil.copy2(temp_db, TARGET_DB)
        logger.info(f"Database Room Android berhasil diperbarui:\n  -> {TARGET_DB}")

    shutil.copy2(temp_db, SOURCE_DB)
    os.remove(temp_db)
    logger.info("Database lokal OtakOtaku/data/anime_catalog.db juga telah disinkronkan.")

if __name__ == "__main__":
    export_clean_room_db()
