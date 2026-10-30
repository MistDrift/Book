@echo off
rem 前端静态服务（前后端分离）：优先用 Python，未安装则用 JDK 自带 jwebserver
cd /d "%~dp0webapp"
echo 前端服务启动于 http://localhost:5500
start http://localhost:5500/index.html
rem 仅当 python 能真正执行时才用它（Windows 商店占位程序会误判）
python -c "import sys" >nul 2>nul
if %errorlevel%==0 (
    python -m http.server 5500
) else (
    if exist "C:\Users\Sariel\.jdks\openjdk-ea-25\bin\jwebserver.exe" (
        "C:\Users\Sariel\.jdks\openjdk-ea-25\bin\jwebserver.exe" -p 5500 -b 127.0.0.1 -d "%~dp0webapp"
    ) else (
        echo 未找到 python 或 jwebserver，请用任意静态服务器托管本目录
    )
)
pause
