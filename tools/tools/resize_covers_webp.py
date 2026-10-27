import os
import sys
import shutil
from concurrent.futures import ThreadPoolExecutor
from PIL import Image

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA_COVERS_DIR = os.path.join(BASE_DIR, "data", "covers")

ANDROID_ROOT = os.path.join(os.path.dirname(BASE_DIR), "android")
ANDROID_COVERS_DIR = os.path.join(ANDROID_ROOT, r"app\src\main\assets\covers")

# Resolusi optimal berdasarkan analisis UI Android OtakuList:
# - DetailScreen (poster terbesar): 130dp x 195dp
# - Resolusi render 240x360 s.d 260x390px sangat tajam di layar FHD (High DPI)
TARGET_WIDTH = 250
TARGET_HEIGHT = 375
QUALITY = 75


def resize_single_cover(filename: str) -> tuple[int, int]:
    if not filename.endswith(".webp"):
        return 0, 0

    src_path = os.path.join(DATA_COVERS_DIR, filename)
    try:
        orig_size = os.path.getsize(src_path)
        with Image.open(src_path) as img:
            # Resize proporsional dengan Lanczos
            img.thumbnail((TARGET_WIDTH, TARGET_HEIGHT), Image.Resampling.LANCZOS)
            img.save(src_path, format="WEBP", quality=QUALITY, method=4)

        new_size = os.path.getsize(src_path)

        # Sync ke Android assets jika ada
        if os.path.exists(ANDROID_COVERS_DIR):
            dst_android = os.path.join(ANDROID_COVERS_DIR, filename)
            shutil.copy2(src_path, dst_android)

        return orig_size, new_size
    except Exception as e:
        print(f"Error pada {filename}: {e}")
        return 0, 0


def main():
    print("========================================================")
    print("   RESIZE & OPTIMASI POSTER WEBP UNTUK ANDROID UI      ")
    print(f"   Target Dimensi : Maksimal {TARGET_WIDTH}x{TARGET_HEIGHT} px (Kualitas: {QUALITY}%)")
    print("========================================================")

    files = [f for f in os.listdir(DATA_COVERS_DIR) if f.endswith(".webp")]
    total_files = len(files)

    if total_files == 0:
        print("Tidak ditemukan file .webp di folder data/covers/!")
        return

    print(f"Memproses {total_files} file poster...")

    total_orig = 0
    total_new = 0

    with ThreadPoolExecutor(max_workers=8) as executor:
        results = list(executor.map(resize_single_cover, files))

    for orig_s, new_s in results:
        total_orig += orig_s
        total_new += new_s

    orig_mb = total_orig / (1024 * 1024)
    new_mb = total_new / (1024 * 1024)
    saved_pct = ((total_orig - total_new) / total_orig) * 100 if total_orig > 0 else 0

    print("--------------------------------------------------------")
    print(f"Ukuran Sebelum : {orig_mb:.2f} MB")
    print(f"Ukuran Sesudah : {new_mb:.2f} MB")
    print(f"Penghematan    : {saved_pct:.1f}% LEBIH RINGAN")
    print("--------------------------------------------------------")
    print(f"Folder OtakOtaku : {DATA_COVERS_DIR} ({new_mb:.2f} MB)")
    if os.path.exists(ANDROID_COVERS_DIR):
        print(f"Folder Android   : {ANDROID_COVERS_DIR} (Tersinkron)")
    print("\n[SELESAI] Database tidak perlu diubah karena nama file tetap {id}.webp.")


if __name__ == "__main__":
    main()
