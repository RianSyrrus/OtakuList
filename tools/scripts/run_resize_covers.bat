@echo off
cd /d "%~dp0\.."
echo ========================================================
echo    RESIZE COVER POSTER WEBP
echo ========================================================
python tools\resize_covers_webp.py
pause
