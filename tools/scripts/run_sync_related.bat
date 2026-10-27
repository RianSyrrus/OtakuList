@echo off
title "Sinkronisasi Anime Terkait - OtakuList"
cd /d "%~dp0\.."
echo ========================================================
echo    MENSINKRONKAN DATA ANIME TERKAIT (SEMUA KATEGORI)
echo   TV, Movie, TV Special, OVA, ONA, Music, Side-Story
echo ========================================================
echo Output Database : data\anime_catalog.db
echo Target Android  : ..\android\app\src\main\assets\databases\
echo ========================================================
python pipelines\sync_related_anime.py
pause
