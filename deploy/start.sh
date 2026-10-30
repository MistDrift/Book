#!/bin/bash
# ???? cd ???????
SCRIPT_DIR="$(dirname "$0")"
cd "$SCRIPT_DIR"
echo "=== BookStore ?? ==="
echo "????: $(pwd)"
echo "webapp: $(pwd)/webapp"
echo "classes: $(pwd)/classes"
echo "========================"
export JAVA_CP="classes:lib/*"
nohup java -cp "$JAVA_CP" com.example.bookstore.JettyServer "webapp" "classes" "8080" > app.log 2>&1 &
echo "PID=$!"
echo "???... ? 5 ?"
sleep 5
echo "????: tail -f app.log"
echo "????: http://???IP:8080/index.html"
