@echo off
rem BookStore backend starter - delegates to PowerShell script
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0start_backend.ps1"
pause
