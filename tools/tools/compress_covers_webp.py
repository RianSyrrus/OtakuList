import os
import sys
import sqlite3
import shutil
from concurrent.futures import ThreadPoolExecutor
from PIL import Image

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA_DIR = os.path.join(BASE_DIR, "data")
COVERS_DIR = os.path.join(DATA_DIR, "covers")
DB_PATH = os.path.join(DATA_DIR, "anime_catalog.db")

ANDROID_ROOT = os.path.join(os.path.dirname(BASE_DIR), "android")
ANDROID_COVERS_DIR = os.path.join(ANDROID_ROOT, r"app\src\main\assets\covers")
ANDROID_DB_PATH = os.path.join(ANDROID_ROOT, r"app\src\main\assets\databases\anime_catalog.db")


def convert_image(file_name: str) -> tuple[int, int]:
    """Convert a single JPG file to WebP and delete original JPG."""
    if not file_name.endswith(".jpg"):
        return 0, 0

    jpg_path = os.path.join(COVERS_DIR, file_name)
    base_name = os.path.splitext(file_name)[0]
    webp_path = os.path.join(COVERS_DIR, f"{base_name}.webp")

    try:
        orig_size = os.path.getsize(jpg_path)
        with Image.open(jpg_path) as img:
            # Convert RGBA/P to RGB if needed
            if img.mode in ("RGBA", "P"):
                img = img.convert("RGB")
            img.save(webp_path, format="WEBP", quality=80, method=4)

        new_size = os.path.getsize(webp_path)
        os.remove(jpg_path)
        return orig_size, new_size
    except Exception as e:
        print(f"Error converting {file_name}: {e}")
        return 0, 0


def update_database_extension(db_path: str):
    """Update cover_file in SQLite to use .webp extension."""
    if not os.path.exists(db_path):
        return
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    cursor.execute("""
    UPDATE anime 
    SET cover_file = REPLACE(cover_file, '.jpg', '.webp')
    WHERE cover_file LIKE '%.jpg'
    """)
    conn.commit()
    conn.close()
    print(f"Database cover_file updated: {db_path}")


def main():
    print("========================================================")
    print("      KOMPRESI POSTER ANIME KE FORMAT WEBP (80%)        ")
    print("========================================================")

    jpg_files = [f for f in os.listdir(COVERS_DIR) if f.endswith(".jpg")]
    total_files = len(jpg_files)

    if total_files == 0:
        print("Tidak ada file .jpg yang perlu dikompres (sudah format .webp).")
    else:
        print(f"Memulai konversi {total_files} file JPG ke WebP...")

        total_orig = 0
        total_new = 0

        with ThreadPoolExecutor(max_workers=8) as executor:
            results = list(executor.map(convert_image, jpg_files))

        for orig_s, new_s in results:
            total_orig += orig_s
            total_new += new_s

        orig_mb = total_orig / (1024 * 1024)
        new_mb = total_new / (1024 * 1024)
        saved_pct = ((total_orig - total_new) / total_orig) * 100 if total_orig > 0 else 0

        print("--------------------------------------------------------")
        print(f"Ukuran Awal (JPG)  : {orig_mb:.2f} MB")
        print(f"Ukuran Baru (WebP) : {new_mb:.2f} MB")
        print(f"Penghematan Ruang  : {saved_pct:.1f}% LEBIH HEMAT")
        print("--------------------------------------------------------")

    # Update database local & Android
    print("\nMemperbarui referensi nama file di SQLite...")
    update_database_extension(DB_PATH)

    # Sync to Android Studio assets
    if os.path.exists(ANDROID_COVERS_DIR):
        print(f"\nMenyinkronkan file WebP ke Android Studio:\n{ANDROID_COVERS_DIR}")

        # Hapus file jpg lama di Android assets
        for old_f in os.listdir(ANDROID_COVERS_DIR):
            if old_f.endswith(".jpg"):
                os.remove(os.path.join(ANDROID_COVERS_DIR, old_f))

        # Copy webp baru ke Android assets
        webp_files = [f for f in os.listdir(COVERS_DIR) if f.endswith(".webp")]
        for wf in webp_files:
            shutil.copy2(os.path.join(COVERS_DIR, wf), os.path.join(ANDROID_COVERS_DIR, wf))

        print(f"Berhasil menyalin {len(webp_files)} file .webp ke folder assets Android!")

    # Copy updated DB to Android
    if os.path.exists(os.path.dirname(ANDROID_DB_PATH)):
        shutil.copy2(DB_PATH, ANDROID_DB_PATH)
        print(f"Berhasil memperbarui {ANDROID_DB_PATH}")

    print("\n[SELESAI] Kompresi selesai sempurna. APK Android Anda sekarang jauh lebih ringan!")


if __name__ == "__main__":
    main()
