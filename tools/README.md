# OtakOtaku Scraping API & Data Engine

REST API Scraper dan Mesin Data untuk situs ensiklopedia anime Indonesia [otakotaku.com](https://otakotaku.com). Layanan ini menyediakan API kutipan anime (*quotes*), basis data anime lengkap, artikel berita, profil karakter & seiyu, pencarian terpadu, serta mesin sinkronisasi WiFi lokal (*Over-The-Air*) untuk aplikasi Android **OtakuList**.

---

## 🚀 Launcher Utama (Root)

Folder root menyediakan launcher 1-klik untuk seluruh operasi utama:

| Launcher | Deskripsi | Perintah Alternatif |
| :--- | :--- | :--- |
| `run_sync_server.bat` | Menjalankan **Server Sinkronisasi WiFi Lokal** (port 8080) untuk pembaruan katalog ke HP Android via LAN/Hotspot | `python sync_server.py` |
| `run_sync_delta.bat` | Menjalankan **Scraper Delta Cepat** (halaman 1-3 & MAL) dan langsung mengemas paket patch (`.zip` & `.json`) | `python sync/sync_delta_scraper.py` |
| `run_api.bat` | Menjalankan **FastAPI Web Server** di `http://localhost:8009` dengan dokumentasi interaktif Swagger UI | `python -m uvicorn OtakOtaku.api:app --reload` |

---

## 📂 Struktur Direktori

```text
D:\Android Project API\Scraping API\OtakOtaku/
├── data/                         # Berkas data, DB SQLite, poster cover, dan paket patch
│   ├── anime_catalog.db          # Basis data master SQLite
│   ├── anime_catalog.json        # Ekspor JSON
│   ├── covers/                   # Cover anime reguler (WebP)
│   ├── covers_adult/             # Cover anime khusus 18+ (WebP)
│   └── patch/                    # Paket delta sinkronisasi (manifest, json patch, zip cover)
│
├── sync/                         # [Fitur 006] Mesin Sinkronisasi WiFi Lokal
│   ├── sync_server.py            # Server HTTP LAN (status, patch, covers, web UI & QR code)
│   ├── sync_delta_scraper.py     # Scraper inkremental cepat dengan early-stop
│   └── export_delta_patch.py     # Generator paket delta patch & kompresi zip
│
├── pipelines/                    # Pipeline Pengunduhan Data Skala Penuh
│   ├── dump_full_catalog.py      # Pengunduh seluruh katalog OtakOtaku ke DB & JSON
│   ├── fetch_mal_adult.py        # Pengunduh katalog dewasa MAL + terjemahan Gemini AI
│   ├── sync_related_anime.py     # Sinkronisasi anime terkait (prequel, sequel, spin-off)
│   └── export_android_db.py      # Builder Room DB khusus untuk aset Android
│
├── tools/                        # Skrip Pemeliharaan, Konversi Media & Migrasi
│   ├── compress_covers_webp.py   # Kompresi poster JPG ke format WebP
│   ├── resize_covers_webp.py     # Penyesuaian dimensi poster WebP ke resolusi optimal
│   ├── fix_episodes.py           # Perbaikan penomoran episode anime
│   ├── translate_english_synopses.py # Terjemahan sinopsis Inggris via Google Translate
│   ├── apply_option2.py          # Skrip migrasi offset ID anime dewasa (+100.000)
│   ├── audit_data.py             # Skrip audit konsistensi database & berkas cover
│   └── user_watchlist_raw.txt    # Data mentah riwayat tontonan
│
├── scripts/                      # Batch launcher untuk operasi pipeline & maintenance
│   ├── run_full_dump.bat         # Jalankan dump_full_catalog.py
│   ├── run_fetch_adult.bat       # Jalankan fetch_mal_adult.py
│   ├── run_sync_related.bat      # Jalankan sync_related_anime.py
│   ├── compress_covers_webp.bat  # Jalankan compress_covers_webp.py
│   ├── run_resize_covers.bat     # Jalankan resize_covers_webp.py
│   └── run_fix_translations.bat # Jalankan translate_english_synopses.py
│
├── scrapers/                     # Modul scraper individual untuk FastAPI
│   ├── anime_scraper.py          # Scraper detail, feed, genre, season anime
│   ├── article_scraper.py        # Scraper berita & artikel
│   ├── character_scraper.py      # Scraper karakter anime & seiyu
│   ├── quote_scraper.py          # Scraper kutipan anime
│   └── search_scraper.py         # Scraper pencarian global
│
├── tests/                        # Pengujian unit pytest
│
├── __init__.py                   # Package marker
├── api.py                        # Endpoint REST API FastAPI
├── client.py                     # SafeHttpClient (proteksi WAF & stealth headers)
├── cache.py                      # TTLCache in-memory
├── scraper.py                    # Base scraper class
├── sync_server.py                # Wrapper root untuk sync/sync_server.py
├── run_api.bat                   # Launcher API
├── run_sync_server.bat           # Launcher WiFi Sync Server
├── run_sync_delta.bat            # Launcher Fast Delta Scraper
└── README.md                     # Panduan ini
```

---

## ⚡ Fitur Utama & Keamanan
- **Async Non-blocking**: Ditenagai oleh `FastAPI` dan `httpx.AsyncClient`.
- **In-Memory TTL Caching**: Cache internal berdurasi 5 menit per request untuk mereduksi beban network ke server target dan mencegah throttling / deteksi bot.
- **Bypass Cache**: Parameter `?refresh=true` tersedia di seluruh endpoint GET untuk mengambil data real-time terbaru.
- **Proteksi SSRF & Sanitasi Input**: Seluruh target request di-whitelist strictly ke `https://otakotaku.com`.
- **CORS Support**: `Access-Control-Allow-Origin: *` aktif untuk integrasi langsung ke aplikasi Android atau frontend client.
- **Sinkronisasi WiFi Lokal**: 0 MB internet egress, transaksi atomik, dan proteksi Zip Slip.

---

## 📋 Daftar Endpoint API REST (Port 8009)

### 1. Quotes Anime
| Method | Endpoint | Keterangan |
| :--- | :--- | :--- |
| `GET` | `/api/quote/feed?page=1` | Daftar feed quote anime terbaru beserta karakter dan judul anime. |
| `GET` | `/api/quote/detail/{quote_id}/{slug}` | Detail kutipan lengkap, media gambar/audio, dan anime asal. |
| `GET` | `/api/quote/search?q={keyword}&page=1` | Pencarian kutipan berdasarkan kata kunci atau karakter. |

### 2. Database & Musim Anime
| Method | Endpoint | Keterangan |
| :--- | :--- | :--- |
| `GET` | `/api/anime/feed?page=1` | Daftar rilis anime terbaru beserta tipe, episode, dan sinopsis singkat. |
| `GET` | `/api/anime/detail/{anime_id}/{slug}` | Detail lengkap anime (sinopsis, skor, studio, status, karakter, OP/ED, trailer). |
| `GET` | `/api/anime/popular` | Daftar anime terpopuler season ini. |
| `GET` | `/api/anime/season/{season_slug}` | Daftar anime per musim (contoh: `summer-2024`, `fall-2026`, `later`). |
| `GET` | `/api/anime/genres` | Master list seluruh kategori genre anime yang tersedia. |
| `GET` | `/api/anime/genre/{genre_slug}?page=1` | Daftar anime berdasarkan genre tertentu. |

### 3. Artikel & Berita Anime
| Method | Endpoint | Keterangan |
| :--- | :--- | :--- |
| `GET` | `/api/article/feed?page=1` | Feed artikel dan berita anime/manga terkini. |
| `GET` | `/api/article/category/{category_slug}?page=1` | Filter artikel berdasarkan kategori (`anime`, `manga`, `seiyu`). |
| `GET` | `/api/article/detail/{post_id}/{slug}` | Konten artikel lengkap beserta paragraf teks, tanggal, dan penulis. |

### 4. Karakter & Seiyu
| Method | Endpoint | Keterangan |
| :--- | :--- | :--- |
| `GET` | `/api/character/detail/{char_id}/{slug}` | Profil lengkap karakter (biodata, jenis kelamin, seiyu pengisi suara, daftar anime). |

### 5. Pencarian Global
| Method | Endpoint | Keterangan |
| :--- | :--- | :--- |
| `GET` | `/api/search?q={query}` | Pencarian global multi-kategori (anime, karakter, seiyu, artikel, quotes, anisong). |

---

## 📦 Format Respon Standar

```json
{
  "status": "success",
  "cached": false,
  "data": { ... }
}
```
Jika terjadi error:
```json
{
  "status": "error",
  "message": "Content not found at https://otakotaku.com/quote/view/999999"
}
```
