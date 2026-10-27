@echo off
title OtakuList Local WiFi Sync Server
cd /d "%~dp0"
echo ======================================================
echo       MENJALANKAN LOCAL WIFI SYNC SERVER (PORT 8080)
echo ======================================================
python sync\sync_server.py %*
pause
