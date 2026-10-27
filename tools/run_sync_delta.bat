@echo off
title OtakuList Fast Delta Scraper
cd /d "%~dp0"
echo ======================================================
echo      MENJALANKAN SCRAPER PEMBARUAN DELTA CEPAT
echo ======================================================
python sync\sync_delta_scraper.py %*
echo.
echo Menghasilkan paket delta...
python sync\export_delta_patch.py
pause
