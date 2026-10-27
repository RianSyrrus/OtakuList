import os
import sys
import json
import socket
import logging
from http.server import HTTPServer, ThreadingHTTPServer, BaseHTTPRequestHandler
from urllib.parse import urlparse

# Ensure UTF-8 output on Windows terminal
if sys.platform.startswith("win"):
    try:
        sys.stdout.reconfigure(encoding="utf-8")
    except Exception:
        pass

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger("SyncServer")

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA_DIR = os.path.join(BASE_DIR, "data")
PATCH_DIR = os.path.join(DATA_DIR, "patch")

MANIFEST_PATH = os.path.join(PATCH_DIR, "sync_manifest.json")
PATCH_PATH = os.path.join(PATCH_DIR, "catalog_patch.json")
COVERS_ZIP_PATH = os.path.join(PATCH_DIR, "covers_patch.zip")


def get_local_ip() -> str:
    """Mendeteksi IP LAN lokal perangkat ini."""
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
        s.close()
        return ip
    except Exception:
        try:
            return socket.gethostbyname(socket.gethostname())
        except Exception:
            return "127.0.0.1"


def print_qr_code(text: str):
    """Mencetak QR Code ke konsol terminal jika pustaka qrcode tersedia."""
    try:
        import qrcode
        qr = qrcode.QRCode(border=1)
        qr.add_data(text)
        qr.make(fit=True)
        print("\n--- PINDAI / SALIN URL SINKRONISASI ---")
        qr.print_ascii(invert=True)
        print("--------------------------------------\n")
    except Exception as e:
        logger.debug(f"Pustaka qrcode tidak digunakan untuk render terminal: {e}")


class SyncRequestHandler(BaseHTTPRequestHandler):
    def send_cors_headers(self):
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type, Accept")

    def do_OPTIONS(self):
        self.send_response(204)
        self.send_cors_headers()
        self.end_headers()

    def do_GET(self):
        parsed = urlparse(self.path)
        path = parsed.path.rstrip("/")

        if path in ("/status", "/manifest"):
            self.handle_status()
        elif path == "/patch":
            self.handle_patch()
        elif path == "/covers":
            self.handle_covers()
        elif path == "" or path == "/":
            self.handle_index()
        else:
            self.send_error_json(404, f"Endpoint '{self.path}' tidak ditemukan.")

    def handle_status(self):
        if not os.path.exists(MANIFEST_PATH):
            self.send_error_json(404, "sync_manifest.json belum dibuat. Jalankan export_delta_patch.py terlebih dahulu.")
            return

        try:
            with open(MANIFEST_PATH, "rb") as f:
                content = f.read()
            self.send_response(200)
            self.send_header("Content-Type", "application/json; charset=utf-8")
            self.send_header("Content-Length", str(len(content)))
            self.send_cors_headers()
            self.end_headers()
            self.wfile.write(content)
        except Exception as e:
            self.send_error_json(500, f"Gagal membaca manifest: {e}")

    def handle_patch(self):
        if not os.path.exists(PATCH_PATH):
            self.send_error_json(404, "catalog_patch.json belum dibuat. Jalankan export_delta_patch.py terlebih dahulu.")
            return

        try:
            with open(PATCH_PATH, "rb") as f:
                content = f.read()
            self.send_response(200)
            self.send_header("Content-Type", "application/json; charset=utf-8")
            self.send_header("Content-Length", str(len(content)))
            self.send_cors_headers()
            self.end_headers()
            self.wfile.write(content)
        except Exception as e:
            self.send_error_json(500, f"Gagal membaca catalog patch: {e}")

    def handle_covers(self):
        if not os.path.exists(COVERS_ZIP_PATH):
            self.send_error_json(404, "covers_patch.zip belum dibuat. Jalankan export_delta_patch.py terlebih dahulu.")
            return

        try:
            file_size = os.path.getsize(COVERS_ZIP_PATH)
            self.send_response(200)
            self.send_header("Content-Type", "application/zip")
            self.send_header("Content-Length", str(file_size))
            self.send_header("Content-Disposition", 'attachment; filename="covers_patch.zip"')
            self.send_cors_headers()
            self.end_headers()

            with open(COVERS_ZIP_PATH, "rb") as f:
                while chunk := f.read(65536):
                    self.wfile.write(chunk)
        except Exception as e:
            logger.error(f"Gagal mengalirkan covers_patch.zip: {e}")

    def handle_index(self):
        manifest_info = {}
        if os.path.exists(MANIFEST_PATH):
            try:
                with open(MANIFEST_PATH, "r", encoding="utf-8") as f:
                    manifest_info = json.load(f)
            except Exception:
                pass

        html = f"""<!DOCTYPE html>
<html lang="id">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>OtakuList Sync Server</title>
    <style>
        body {{ font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #0f172a; color: #f8fafc; padding: 2rem; }}
        .card {{ background: #1e293b; border-radius: 12px; padding: 1.5rem; max-width: 600px; margin: 0 auto; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); border: 1px solid #334155; }}
        h1 {{ color: #38bdf8; font-size: 1.5rem; margin-top: 0; }}
        .badge {{ background: #0284c7; color: white; padding: 4px 8px; border-radius: 6px; font-size: 0.85rem; font-weight: bold; }}
        .row {{ display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #334155; }}
        .row:last-child {{ border-bottom: none; }}
        .label {{ color: #94a3b8; }}
        .val {{ font-weight: 600; }}
        .endpoints {{ margin-top: 1.5rem; background: #0f172a; border-radius: 8px; padding: 1rem; }}
        code {{ color: #38bdf8; }}
    </style>
</head>
<body>
    <div class="card">
        <h1>⚡ OtakuList Local WiFi Sync Server</h1>
        <p>Server lokal siap melayani pembaruan katalog ke aplikasi Android <strong>OtakuList</strong>.</p>
        
        <div class="row">
            <span class="label">Status Server</span>
            <span class="badge">AKTIF</span>
        </div>
        <div class="row">
            <span class="label">Versi Patch Katalog</span>
            <span class="val">{manifest_info.get('catalog_version', 'N/A')}</span>
        </div>
        <div class="row">
            <span class="label">Anime Reguler Baru</span>
            <span class="val">{manifest_info.get('new_regular_count', 0)} judul</span>
        </div>
        <div class="row">
            <span class="label">Anime Dewasa Baru</span>
            <span class="val">{manifest_info.get('new_adult_count', 0)} judul</span>
        </div>
        <div class="row">
            <span class="label">Arsip Cover WebP</span>
            <span class="val">{manifest_info.get('covers_count', 0)} berkas ({manifest_info.get('covers_size_bytes', 0) / 1024:.1f} KB)</span>
        </div>
        <div class="row">
            <span class="label">Waktu Dibuat</span>
            <span class="val">{manifest_info.get('generated_at', 'N/A')}</span>
        </div>

        <div class="endpoints">
            <strong>Endpoint API:</strong>
            <ul>
                <li><code>GET /status</code> - Cek info manifest patch</li>
                <li><code>GET /patch</code> - Unduh data anime baru (JSON)</li>
                <li><code>GET /covers</code> - Unduh bundle cover poster (.zip)</li>
            </ul>
        </div>
    </div>
</body>
</html>
"""
        encoded = html.encode("utf-8")
        self.send_response(200)
        self.send_header("Content-Type", "text/html; charset=utf-8")
        self.send_header("Content-Length", str(len(encoded)))
        self.send_cors_headers()
        self.end_headers()
        self.wfile.write(encoded)

    def send_error_json(self, status_code: int, message: str):
        payload = json.dumps({"error": message, "status": status_code}).encode("utf-8")
        self.send_response(status_code)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(payload)))
        self.send_cors_headers()
        self.end_headers()
        self.wfile.write(payload)

    def log_message(self, format, *args):
        logger.info(f"{self.client_address[0]} - {format % args}")


def run_server(port: int = 8080):
    ip = get_local_ip()
    server_address = ("0.0.0.0", port)
    
    server = ThreadingHTTPServer(server_address, SyncRequestHandler)

    sync_url = f"http://{ip}:{port}"
    sync_host = f"{ip}:{port}"

    print("\n" + "=" * 60)
    print("      OTAKULIST LOCAL WIFI SYNC SERVER (LAN)")
    print("=" * 60)
    print(f"  [+] Host Lokal       : {sync_host}")
    print(f"  [+] Web Dashboard    : {sync_url}/")
    print(f"  [+] Status Endpoint  : {sync_url}/status")
    print(f"  [+] Patch Endpoint   : {sync_url}/patch")
    print(f"  [+] Covers Endpoint  : {sync_url}/covers")
    print("-" * 60)

    # Info status patch saat ini
    if os.path.exists(MANIFEST_PATH):
        try:
            with open(MANIFEST_PATH, "r", encoding="utf-8") as f:
                m = json.load(f)
            print(f"  [i] Versi Katalog   : {m.get('catalog_version')}")
            print(f"  [i] Muatan Anime    : {m.get('new_regular_count', 0)} Reguler, {m.get('new_adult_count', 0)} Dewasa")
            print(f"  [i] Ukuran Cover    : {m.get('covers_size_bytes', 0) / 1024:.1f} KB ({m.get('covers_count', 0)} gambar)")
        except Exception:
            pass
    else:
        print("  [!] PERINGATAN: Paket delta belum ditemukan di folder data/patch/")
        print("      Jalankan: python export_delta_patch.py")

    print("=" * 60)
    print("  CARA PENGGUNAAN DI HP:")
    print("  1. Pastikan HP dan PC berada di satu jaringan WiFi / Hotspot yang sama.")
    print("  2. Buka Aplikasi OtakuList -> Pengaturan -> Sinkronisasi WiFi Lokal.")
    print(f"  3. Masukkan Host IP : {sync_host}")
    print("  4. Tekan tombol 'Periksa Pembaruan', lalu 'Mulai Sinkronisasi'.")
    print("=" * 60)

    print_qr_code(sync_host)
    print("Server sedang berjalan... Tekan Ctrl+C untuk menghentikan.\n")

    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\nMenghentikan server...")
    finally:
        server.server_close()
        logger.info("Server dihentikan.")


if __name__ == "__main__":
    p = 8080
    if len(sys.argv) > 1 and sys.argv[1].isdigit():
        p = int(sys.argv[1])
    run_server(port=p)
