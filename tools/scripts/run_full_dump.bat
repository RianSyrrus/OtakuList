@echo off
title OtakOtaku Full Catalog Dumper
cd /d "%~dp0\.."
echo ========================================================
echo    MENGUNDUH SELURUH DATABASE ANIME & POSTER OTAKOTAKU
echo ========================================================
echo Database Output: data\anime_catalog.db
echo JSON Output    : data\anime_catalog.json
echo Covers Folder  : data\covers\
echo ========================================================
python pipelines\dump_full_catalog.py
pause
