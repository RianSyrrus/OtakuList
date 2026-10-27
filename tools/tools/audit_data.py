import sqlite3
import os
import json

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DB_PATH = os.path.join(BASE_DIR, "data", "anime_catalog.db")
JSON_PATH = os.path.join(BASE_DIR, "data", "anime_catalog.json")
COVERS_DIR = os.path.join(BASE_DIR, "data", "covers")

conn = sqlite3.connect(DB_PATH)
cursor = conn.cursor()

# 1. Check Queue
cursor.execute("SELECT status, COUNT(*) FROM anime_queue GROUP BY status")
queue_stats = dict(cursor.fetchall())

# 2. Check Table Rows
cursor.execute("SELECT COUNT(*) FROM anime")
total_anime = cursor.fetchone()[0]

# 3. Check for NULL or Empty Titles
cursor.execute("SELECT COUNT(*) FROM anime WHERE title IS NULL OR title = ''")
empty_titles = cursor.fetchone()[0]

# 4. Check Cover Files Matching
cursor.execute("SELECT id, cover_file FROM anime")
db_records = cursor.fetchall()

missing_covers = []
zero_byte_covers = []
for anime_id, cover_file in db_records:
    if not cover_file:
        missing_covers.append(anime_id)
        continue
    fpath = os.path.join(COVERS_DIR, cover_file)
    if not os.path.exists(fpath):
        missing_covers.append(anime_id)
    elif os.path.getsize(fpath) < 100:
        zero_byte_covers.append(anime_id)

# 5. Check Disk Files
disk_files = [f for f in os.listdir(COVERS_DIR) if f.endswith(".jpg")]

# 6. Check JSON
with open(JSON_PATH, "r", encoding="utf-8") as f:
    json_data = json.load(f)

# Sample check first and last anime
cursor.execute("SELECT id, title, type, episodes, genres, cover_file FROM anime ORDER BY id ASC LIMIT 1")
first_anime = cursor.fetchone()

cursor.execute("SELECT id, title, type, episodes, genres, cover_file FROM anime ORDER BY id DESC LIMIT 1")
last_anime = cursor.fetchone()

print("=== HASIL AUDIT INTEGRITAS DATA ===")
print("1. Status Antrean (Queue)     :", queue_stats)
print("2. Total Baris Anime (SQLite) :", total_anime)
print("3. Judul Kosong (Null/Empty)  :", empty_titles)
print("4. File Poster di Disk (JPG)  :", len(disk_files))
print("5. Poster Hilang / Missing    :", len(missing_covers))
print("6. Poster Korup (< 100 byte)  :", len(zero_byte_covers))
print("7. Total Entri JSON           :", len(json_data))
print("-----------------------------------")
print("Anime Pertama di DB           :", first_anime)
print("Anime Terakhir di DB          :", last_anime)
