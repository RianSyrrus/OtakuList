@echo off
title "Download Katalog Khusus (MAL + Gemini AI) - OtakuList"
cd /d "%~dp0\.."
echo ========================================================
echo   MENGUNDUH KATALOG KHUSUS DARI MAL + TERJEMAHAN AI
echo ========================================================
echo Target Database : data\anime_catalog.db (Tabel adult_anime)
echo Target Folder   : ..\android\app\src\main\assets\covers_adult\
echo Target Android  : ..\android\app\src\main\assets\databases\
echo ========================================================
python pipelines\fetch_mal_adult.py
pause
