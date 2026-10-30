#!/bin/bash
# ?????????Ubuntu/Debian?
echo "=== 1. Install JDK 17 ==="
sudo apt update && sudo apt install -y openjdk-17-jdk

echo "=== 2. Install MySQL 8 ==="
sudo apt install -y mysql-server
sudo systemctl enable mysql

echo "=== 3. Create DB ==="
read -s -p "Enter MySQL root password: " MPASS
echo "CREATE DATABASE IF NOT EXISTS db_book DEFAULT CHARSET utf8mb4;" | sudo mysql -uroot
echo "db_book DB created."

echo "=== 4. Import SQL ==="
sudo mysql -uroot db_book < db_book.sql
echo "Import done."

echo "=== 5. Open ports ==="
sudo ufw allow 8080/tcp
echo "Open port 8080."

echo "=== DONE ==="
echo "Now run: ./start.sh"
