@echo off
cd /d "%~dp0\.."
echo ========================================================
echo    KOMPRESI COVER POSTER JPG KE WEBP
echo ========================================================
python tools\compress_covers_webp.py
pause
