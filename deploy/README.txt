==================== 图书管理系统 部署包 ====================

目录结构:
  classes/  — 后端编译产物（.class）
  lib/      — 依赖 jar 包（12 个）
  webapp/   — 前端页面（20 html + 21 js + 231 封面图）
  db_book.sql — 数据库初始化脚本（12 张表 + 9 用户 + 229 图书 + 示例数据）
  start.bat   — Windows 启动脚本
  start.sh    — Linux 启动脚本
  setup-server.sh — Linux 一键初始化脚本

=======================================================
第一步：必做安全改动（上线不能不改！）
=======================================================
1) 改数据库用户名/密码
   当前默认 root/root@root（可改）：在 classes 目录旁放 db.properties，无需重新编译：
   db.url=jdbc:mysql://localhost:3306/db_book?useSSL=false&serverTimezone=UTC&characterEncoding=utf8&allowPublicKeyRetrieval=true
   db.username=bookstore
   db.password=你的强密码
   建账号：
   GRANT ALL ON db_book.* TO 'bookstore'@'localhost' IDENTIFIED BY '你的强密码';
   FLUSH PRIVILEGES;

2) 改前端 API_BASE
   deploy/webapp/js/config.js 第一行：
   const API_BASE = window.location.protocol + '//' + window.location.hostname + ':8080';
   如果服务器上后端不在 8080 端口，改成真实地址。

3) 可选：删掉 admin123 等测试账号的默认密码
   UPDATE tb_user SET password = '强密码' WHERE username = 'admin';

=======================================================
第二步：服务器部署
=======================================================

=== Linux（推荐） ===
  sudo apt install openjdk-17-jdk mysql-server -y
  sudo mysql_secure_installation    # 设新 root 密码
  mysql -uroot -p -e "CREATE DATABASE db_book DEFAULT CHARSET utf8mb4;"
  mysql -uroot -p db_book < db_book.sql
  cd deploy/ && chmod +x start.sh && ./start.sh
  # 后台守护: sudo apt install -y screen && screen -dmS bookstore bash start.sh
  # 或 systemd 服务（见下文）

=== Windows ===
  1) 装 JDK 17 + MySQL 8
  2) mysql -uroot -p db_book < db_book.sql
  3) 防火墙开放 8080：New-NetFirewallRule -DisplayName "Jetty" -Direction Inbound -Protocol TCP -LocalPort 8080 -Action Allow
  4) 双击 start.bat 或在 cmd 里运行

=== 内网穿透（最快演示方案，不需买服务器） ===
  1) 本机启动 start.bat
  2) 下载 cpolar: https://www.cpolar.com
  3) cpolar http 5500   # 前端
     cpolar http 8080   # 后端
  4) 改 config.js 里 API_BASE 为 cpolar 给的后端地址
  5) 把前端公网地址发给别人即可访问

=======================================================
第三步：systemd 服务（Linux 持久运行）
=======================================================
# /etc/systemd/system/bookstore.service
[Unit]
Description=BookStore Jetty Server
After=network.target mysql.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/bookstore
ExecStart=/usr/bin/java -cp "classes:lib/*" com.example.bookstore.JettyServer
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target

启用: sudo systemctl daemon-reload
     sudo systemctl enable bookstore
     sudo systemctl start bookstore
日志: sudo journalctl -u bookstore -f

=======================================================
第四步：Nginx 反代 + HTTPS（可选，更高质量）
=======================================================
server {
    listen 80;
    server_name bookstore.yourdomain.com;

    # 后端 API 反代
    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
    # 图书封面图片反代
    location /upload/ {
        proxy_pass http://127.0.0.1:8080/upload/;
    }
    # 静态前端
    location / {
        root /opt/bookstore/webapp;
        try_files $uri /index.html;
    }
}
# HTTPS: certbot certonly --nginx -d bookstore.yourdomain.com

=======================================================
验证清单
=======================================================
✅ 1. 本机 http://localhost:8080/api/login 返回 JSON
✅ 2. 本机 http://localhost:5500/index.html 打开
✅ 3. 管理员 admin/admin123 能登录 home.html
✅ 4. 教师 teacher1/teacher123 能登录 home.html（选"用户登录→教师"）
✅ 5. 新注册账号能登录（公开注册接口 /api/register）
✅ 6. 用 curl http://公网IP:8080/api/login 外部能访问
✅ 7. 换台电脑用内网 IP 能访问 http://你的电脑IP:5500/index.html
✅ 8. 换个 MySQL 实例导入 db_book.sql 全部功能正常
