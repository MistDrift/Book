@echo off
chcp 65001 > nul
set JAVA_CP=classes;lib/*
set JAVA_MAIN=com.example.bookstore.JettyServer
cd /d "%~dp0"
echo Starting BookStore on port 8080 (webapp=webapp, classes=classes)...
echo Jetty log will follow below; press Ctrl+C to stop.
java -cp "%JAVA_CP%" "%JAVA_MAIN%" "webapp" "classes" "8080"
pause
