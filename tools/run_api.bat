@echo off
title OtakOtaku Scraping API Server
cd /d "%~dp0"
echo ======================================================
echo    Menjalankan OtakOtaku Scraping API Server
echo ======================================================
echo Server URL : http://localhost:8009
echo Swagger UI : http://localhost:8009/docs
echo ======================================================
cd ..
python -m uvicorn api:app --host 0.0.0.0 --port 8009 --reload
pause
