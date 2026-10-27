@echo off
title Perbaikan Terjemahan Sinopsis Bahasa Inggris
cd /d "%~dp0\.."
python tools\translate_english_synopses.py
pause
