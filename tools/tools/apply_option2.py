import os
import sqlite3
import shutil
import logging

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger("ApplyOption2")

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LOCAL_DB = os.path.join(BASE_DIR, "data", "anime_catalog.db")
ANDROID_DB = os.path.join(os.path.dirname(BASE_DIR), "android", "app", "src", "main", "assets", "databases", "anime_catalog.db")
LOCAL_COVERS_ADULT = os.path.join(BASE_DIR, "data", "covers_adult")
ANDROID_COVERS_ADULT = os.path.join(os.path.dirname(BASE_DIR), "android", "app", "src", "main", "assets", "covers_adult")

ID_OFFSET = 100000
ROOM_IDENTITY_HASH = "e629f1b50af63fa8510bd04b7dbab401"

def apply_offset_and_watchlist():
    # 1. Rename files in LOCAL_COVERS_ADULT
    logger.info("1. Mengganti nama file cover dewasa di folder lokal (offset +100.000)...")
    if os.path.exists(LOCAL_COVERS_ADULT):
        for f in os.listdir(LOCAL_COVERS_ADULT):
            if f.endswith(".webp") and f[:-5].isdigit():
                old_id = int(f[:-5])
                if old_id < ID_OFFSET:
                    new_id = old_id + ID_OFFSET
                    old_path = os.path.join(LOCAL_COVERS_ADULT, f)
                    new_path = os.path.join(LOCAL_COVERS_ADULT, f"{new_id}.webp")
                    os.rename(old_path, new_path)

    # 2. Rename files in ANDROID_COVERS_ADULT
    logger.info("2. Mengganti nama file cover dewasa di folder Android assets (offset +100.000)...")
    if os.path.exists(ANDROID_COVERS_ADULT):
        for f in os.listdir(ANDROID_COVERS_ADULT):
            if f.endswith(".webp") and f[:-5].isdigit():
                old_id = int(f[:-5])
                if old_id < ID_OFFSET:
                    new_id = old_id + ID_OFFSET
                    old_path = os.path.join(ANDROID_COVERS_ADULT, f)
                    new_path = os.path.join(ANDROID_COVERS_ADULT, f"{new_id}.webp")
                    os.rename(old_path, new_path)

    # 3. Update database
    logger.info("3. Membangun skema database baru dengan offset ID dan tabel user_watchlist_adult...")
    temp_db = os.path.join(BASE_DIR, "data", "temp_option2.db")
    if os.path.exists(temp_db):
        os.remove(temp_db)

    src_conn = sqlite3.connect(LOCAL_DB)
    src_cur = src_conn.cursor()

    dest_conn = sqlite3.connect(temp_db)
    dest_cur = dest_conn.cursor()

    # A. Tabel anime (reguler)
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

    # B. Tabel adult_anime (dengan ID + 100.000 dan cover_file baru)
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
    raw_adult_rows = src_cur.fetchall()
    offset_adult_rows = []
    for r in raw_adult_rows:
        old_id = r[0]
        new_id = old_id + ID_OFFSET if old_id < ID_OFFSET else old_id
        new_cover_file = f"{new_id}.webp"
        offset_adult_rows.append((
            new_id, r[1], r[2], r[3], r[4], r[5], r[6], r[7],
            r[8], r[9], r[10], r[11], r[12], r[13], r[14],
            r[15], new_cover_file, r[17], r[18], r[19]
        ))

    dest_cur.executemany("""
    INSERT INTO adult_anime VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """, offset_adult_rows)
    logger.info(f"Tabel adult_anime: {len(offset_adult_rows)} data berhasil dimuat (ID range {ID_OFFSET+188}..).")

    # C. Tabel user_watchlist (reguler)
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

    # D. Tabel user_watchlist_adult (khusus anime dewasa, FOREIGN KEY ke adult_anime)
    dest_cur.execute("""
    CREATE TABLE user_watchlist_adult (
        anime_id INTEGER NOT NULL,
        status TEXT NOT NULL,
        current_episode INTEGER NOT NULL DEFAULT 0,
        personal_score INTEGER NOT NULL DEFAULT 0,
        personal_note TEXT NOT NULL DEFAULT '',
        updated_at INTEGER NOT NULL DEFAULT 0,
        PRIMARY KEY(anime_id),
        FOREIGN KEY(anime_id) REFERENCES adult_anime(id) ON UPDATE NO ACTION ON DELETE CASCADE
    );
    """)
    logger.info("Tabel user_watchlist_adult berhasil dibuat dengan FOREIGN KEY ke adult_anime(id).")

    # E. Tabel room_master_table
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

    # Simpan ke Android & Local
    if os.path.exists(os.path.dirname(ANDROID_DB)):
        shutil.copy2(temp_db, ANDROID_DB)
        logger.info(f"Database Android Assets berhasil diperbarui:\n  -> {ANDROID_DB}")

    shutil.copy2(temp_db, LOCAL_DB)
    os.remove(temp_db)
    logger.info("Database lokal OtakOtaku/data/anime_catalog.db juga telah diperbarui.")

if __name__ == "__main__":
    apply_offset_and_watchlist()
