# 图书管理系统（Java Web + 原生前端）

面向四类角色（管理员 / 教师 / 线上学员 / 社会人员）的图书借阅管理系统，后端为原生 Servlet + Jetty + MySQL 8，前端为纯 HTML/CSS/JavaScript（fetch 调用 REST API），前后端分离部署。

## 在线演示（GitHub Pages）

仓库根目录的 `docs/` 是**纯前端演示版**：`docs/js/demo-mock.js` 在浏览器内拦截全部 `/api/*` 请求并返回内置假数据，因此不需要后端和数据库即可完整操作界面。

- 演示地址：<https://mistdrift.github.io/Book/>（需在仓库 Settings → Pages 中将 Source 设为 `Deploy from a branch`、分支 `master`、目录 `/docs`）
- 演示账号：`admin/admin123`、`student1/student123`、`teacher1/teacher123`、`user1/user123`
- 演示数据每次刷新重置；写操作（借阅/归还/续借/缴费/标记已读等）会在内存中真实生效
- `前端/`、`后端/`、`数据库/` 为真实可运行版本，与演示版互不影响

## 功能模块

| 模块 | 说明 |
| --- | --- |
| 统一登录 | 账号密码登录 + 登录身份预校验（身份与实际 role 不匹配时拒绝登录） |
| 图书查询 | 按关键词 / 分类检索，卡片式展示封面、作者、库存、简介 |
| 图书管理 | 管理员图书增删改查、封面上传（`/api/upload`） |
| 借阅管理 | 借阅登记、续借、归还；管理员可代读者借阅；支持导出当前筛选结果 |
| 我的借阅历史 | 读者本人借阅记录独立页面：封面、借阅/应还/归还日期、逾期天数、罚金、筛选、分页、CSV 导出 |
| 借阅申请 / 预约 | 预约队列，队首自动通知 |
| 罚金管理 | 逾期自动计费、缴纳、与信用分联动 |
| 遗失理赔 | 遗失申报与理赔处理 |
| 馆藏盘点 | 管理员盘点任务与明细核对 |
| 图书荐购 | 读者荐购与管理员处理 |
| 消息中心 | 到期提醒、预约到书等站内消息（后台每小时扫描） |
| 信用管理 | 信用分变动记录，低于 60 分限制借阅 |
| 用户 / 等级 / 权限 | 用户管理、读者分级（借阅上限与借期）、位掩码权限体系 |
| 操作日志 | 全链路审计（访问拦截、登录、业务操作） |
| 数据看板 | 管理员统计视图 |

### 角色权限

| 角色 | role 值 | 主要能力 |
| --- | --- | --- |
| 管理员 | 1 | 全部模块，含图书管理、用户/等级/权限、盘点、审计日志、数据看板 |
| 教师 | 3 | 图书查询、借阅、预约、罚金、消息、荐购 |
| 线上学员 | 2 | 教师能力 + 信用管理 + 借阅历史 |
| 社会人员 | 0 | 图书查询、借阅、预约、罚金、消息 |

页面级权限守卫集中在 `前端/webapp/js/config.js` 的 `PAGE_ROLES`，越权访问会被拦截并写入审计日志。

## 目录结构

```
├─ 后端/java/com/example/bookstore   # Servlet / Service / DAO / Entity / Filter / Util
├─ 前端/webapp                       # 独立前端（index.html + 各功能页 + js/ + css/）
├─ 数据库/db_init.sql                # 建库建表 + 初始数据
├─ 数据库/db_migration_v2.sql        # V2 扩展表（预约/罚金/理赔/盘点/荐购/公告/信用/日志等）
├─ 其他/src/main/webapp/upload       # 图书封面图片（后端 Jetty 提供 /upload/* 静态访问）
├─ 其他/start.bat                    # 后端编译 + 启动（Windows）
├─ deploy/                           # 部署包结构（webapp + classes + lib）
```

## 本地运行

### 1. 数据库

```sql
mysql -uroot -p < 数据库/db_init.sql
mysql -uroot -p < 数据库/db_migration_v2.sql
```

数据库连接默认 `jdbc:mysql://localhost:3306/db_book`，账号 `root` / `root@root`。
如需修改，在 classpath 下放置 `db.properties`：

```properties
db.url=jdbc:mysql://localhost:3306/db_book?useSSL=false&serverTimezone=UTC&characterEncoding=utf8&allowPublicKeyRetrieval=true
db.username=root
db.password=root@root
```

### 2. 后端（端口 8080）

```bat
其他\start.bat
```

脚本会自动用 `javac` 编译 `后端/java` 到 `其他/target/classes`，同步到 `其他/src/main/webapp/WEB-INF/classes`，再启动内嵌 Jetty。依赖 jar 从本机 Maven 仓库 `~/.m2` 收集。

### 3. 前端（端口 5500）

```bat
前端\启动前端.bat
```

打开 <http://localhost:5500/index.html>。后端地址在 `前端/webapp/js/config.js` 的 `API_BASE` 中配置，默认 `http://localhost:8080`。

### 演示账号

| 账号 | 密码 | 身份 |
| --- | --- | --- |
| admin | admin123 | 管理员 |
| teacher1 | teacher123 | 教师 |
| student1 / student2 / student3 | student123 | 线上学员 |
| user1 / user2 / user3 | user123 | 社会人员 |

## 接口约定

- 认证：`POST /api/login` 返回 `token`，后续请求带 `Authorization: Bearer <token>`
- 统一响应：`{ "success": true, "message": "..." }`，列表接口直接返回数组
- CORS：`CorsFilter` 放行 `*` 与 `Authorization` 头，支持 `OPTIONS` 预检
- 审计：`AuditFilter` 拦截 `/api/*` 写操作，日志写入 `tb_log`

## 安全与部署说明

- 当前数据库密码为明文存储，正式部署请改为 BCrypt 哈希并启用 HTTPS
- Token 为内存存储，重启后端后需重新登录
- 部署到服务器时请修改 `API_BASE` 为实际域名，并替换 `db.properties` 中的数据库账号
