import os
import sys
import json
import sqlite3
import zipfile
import hashlib
import logging
from datetime import datetime, timezone

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger("ExportDeltaPatch")

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA_DIR = os.path.join(BASE_DIR, "data")
PATCH_DIR = os.path.join(DATA_DIR, "patch")
DB_PATH = os.path.join(DATA_DIR, "anime_catalog.db")
COVERS_DIR = os.path.join(DATA_DIR, "covers")
COVERS_ADULT_DIR = os.path.join(DATA_DIR, "covers_adult")
DELTA_RECORD_FILE = os.path.join(PATCH_DIR, "pending_delta.json")


def row_to_dict(row, cursor):
    return {cursor.description[i][0]: row[i] for i in range(len(row))}


def compute_sha256(file_path: str) -> str:
    if not os.path.exists(file_path):
        return ""
    hasher = hashlib.sha256()
    with open(file_path, "rb") as f:
        while chunk := f.read(65536):
            hasher.update(chunk)
    return hasher.hexdigest()


def resolve_version(arg_version: Optional[str] = None) -> tuple[str, str]:
    """Returns (current_version, next_version)."""
    cur_ver = "1.1.0"
    manifest_file = os.path.join(PATCH_DIR, "sync_manifest.json")
    if os.path.exists(manifest_file):
        try:
            with open(manifest_file, "r", encoding="utf-8") as f:
                data = json.load(f)
                cur_ver = data.get("catalog_version", "1.1.0")
        except Exception:
            pass

    if arg_version and arg_version.strip():
        return cur_ver, arg_version.strip()

    parts = cur_ver.split(".")
    if len(parts) == 3 and all(p.isdigit() for p in parts):
        next_ver = f"{parts[0]}.{int(parts[1]) + 1}.0"
    else:
        next_ver = "1.2.0"
    return cur_ver, next_ver


def generate_delta_package(version: Optional[str] = None, fallback_recent_count: int = 20):
    os.makedirs(PATCH_DIR, exist_ok=True)
    conn = sqlite3.connect(DB_PATH)
    cur = conn.cursor()

    cur_ver, next_ver = resolve_version(version)

    regular_ids = []
    adult_ids = []

    # Cek pending_delta.json jika ada
    if os.path.exists(DELTA_RECORD_FILE):
        try:
            with open(DELTA_RECORD_FILE, "r", encoding="utf-8") as f:
                saved = json.load(f)
                regular_ids = saved.get("new_regular_ids", [])
                adult_ids = saved.get("new_adult_ids", [])
                logger.info(f"Memuat {len(regular_ids)} reguler dan {len(adult_ids)} dewasa dari pending_delta.json")
        except Exception as e:
            logger.warning(f"Gagal membaca pending_delta.json: {e}")

    # Jika tidak ada data baru yang ditemukan dari scraper
    if not regular_ids and not adult_ids:
        manifest_file = os.path.join(PATCH_DIR, "sync_manifest.json")
        zip_path = os.path.join(PATCH_DIR, "covers_patch.zip")
        patch_file = os.path.join(PATCH_DIR, "catalog_patch.json")

        existing_manifest = {}
        if os.path.exists(manifest_file):
            try:
                with open(manifest_file, "r", encoding="utf-8") as f:
                    existing_manifest = json.load(f)
            except Exception:
                pass

        # Jika paket patch sebelumnya sudah ada dan berisi item aktif, JANGAN DIHAPUS!
        if os.path.exists(zip_path) and os.path.exists(patch_file) and (
            existing_manifest.get("covers_count", 0) > 0 or existing_manifest.get("new_regular_count", 0) > 0
        ):
            conn.close()
            print("\n=======================================================")
            print("             STATUS PAKET SINKRONISASI")
            print("=======================================================")
            print(f"  [i] Paket patch Versi {existing_manifest.get('catalog_version')} TETAP AKTIF & SIAP.")
            print(f"  - Reguler : {existing_manifest.get('new_regular_count')} judul")
            print(f"  - Dewasa  : {existing_manifest.get('new_adult_count')} judul")
            print(f"  - Cover   : {existing_manifest.get('covers_count')} berkas ({existing_manifest.get('covers_size_bytes', 0) / 1024:.1f} KB)")
            print("  - Tidak ada data baru tambahan dari pemindaian web.")
            print("  - Server siap dijalankan (run_sync_server.bat).")
            print("=======================================================\n")
            return

        logger.info("Pemeriksaan selesai: Tidak ada anime baru atau pembaruan yang ditemukan dari web.")
        manifest = {
            "catalog_version": cur_ver,
            "generated_at": datetime.now(timezone.utc).isoformat(),
            "new_regular_count": 0,
            "new_adult_count": 0,
            "covers_count": 0,
            "covers_size_bytes": 0,
            "checksum_sha256": ""
        }
        with open(manifest_file, "w", encoding="utf-8") as f:
            json.dump(manifest, f, indent=2)

        patch_payload = {
            "version": cur_ver,
            "regular_anime": [],
            "adult_anime": []
        }
        with open(patch_file, "w", encoding="utf-8") as f:
            json.dump(patch_payload, f, indent=2)

        if os.path.exists(zip_path):
            os.remove(zip_path)

        conn.close()
        print("\n=======================================================")
        print("           HASIL PEMINDAIAN SINKRONISASI")
        print("=======================================================")
        print("  [i] TIDAK ADA DATA BARU DITEMUKAN DI WEB.")
        print("  - Seluruh rilisan anime di web sudah ada di database.")
        print("  - Tidak ada data baru (0 reguler, 0 dewasa).")
        print("  - Di HP Anda statusnya akan muncul: 'Sudah Paling Mutakhir'.")
        print("=======================================================\n")
        return

    # 1. Ambil data anime reguler
    regular_items = []
    if regular_ids:
        placeholders = ",".join(["?"] * len(regular_ids))
        cur.execute(f"SELECT * FROM anime WHERE id IN ({placeholders})", regular_ids)
        for row in cur.fetchall():
            regular_items.append(row_to_dict(row, cur))

    # 2. Ambil data anime dewasa
    adult_items = []
    if adult_ids:
        placeholders = ",".join(["?"] * len(adult_ids))
        cur.execute(f"SELECT * FROM adult_anime WHERE id IN ({placeholders})", adult_ids)
        for row in cur.fetchall():
            adult_items.append(row_to_dict(row, cur))

    conn.close()

    # 3. Tulis catalog_patch.json
    patch_payload = {
        "version": next_ver,
        "regular_anime": regular_items,
        "adult_anime": adult_items
    }
    patch_file = os.path.join(PATCH_DIR, "catalog_patch.json")
    with open(patch_file, "w", encoding="utf-8") as f:
        json.dump(patch_payload, f, ensure_ascii=False, indent=2)
    logger.info(f"Berkas patch JSON tersimpan: {patch_file} ({len(regular_items)} reguler, {len(adult_items)} dewasa)")

    # 4. Kumpulkan & Kemas Cover ke covers_patch.zip
    zip_path = os.path.join(PATCH_DIR, "covers_patch.zip")
    covers_packed = 0

    with zipfile.ZipFile(zip_path, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=6) as zipf:
        # Cover reguler
        for item in regular_items:
            c_file = item.get("cover_file")
            if c_file:
                c_path = os.path.join(COVERS_DIR, c_file)
                if os.path.exists(c_path):
                    zipf.write(c_path, arcname=f"covers/{c_file}")
                    covers_packed += 1

        # Cover dewasa
        for item in adult_items:
            c_file = item.get("cover_file")
            if c_file:
                c_path = os.path.join(COVERS_ADULT_DIR, c_file)
                if os.path.exists(c_path):
                    zipf.write(c_path, arcname=f"covers_adult/{c_file}")
                    covers_packed += 1

    zip_size = os.path.getsize(zip_path)
    logger.info(f"Berkas arsip cover tersimpan: {zip_path} ({covers_packed} cover, {zip_size / 1024:.1f} KB)")

    # 5. Tulis sync_manifest.json
    manifest = {
        "catalog_version": next_ver,
        "generated_at": datetime.now(timezone.utc).isoformat(),
        "new_regular_count": len(regular_items),
        "new_adult_count": len(adult_items),
        "covers_count": covers_packed,
        "covers_size_bytes": zip_size,
        "checksum_sha256": compute_sha256(zip_path)
    }
    manifest_file = os.path.join(PATCH_DIR, "sync_manifest.json")
    with open(manifest_file, "w", encoding="utf-8") as f:
        json.dump(manifest, f, indent=2)
    logger.info(f"Manifest sinkronisasi tersimpan: {manifest_file}")

    print("\n=======================================================")
    print(f"  PAKET DELTA PATCH BERHASIL DIBUAT (Versi: {next_ver})")
    print(f"  - Anime Reguler : {len(regular_items)} judul")
    print(f"  - Anime Dewasa  : {len(adult_items)} judul")
    print(f"  - Gambar Cover  : {covers_packed} berkas ({zip_size / 1024:.1f} KB)")
    print("=======================================================\n")


if __name__ == "__main__":
    ver = sys.argv[1] if len(sys.argv) > 1 else None
    generate_delta_package(version=ver)
