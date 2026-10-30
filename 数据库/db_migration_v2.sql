-- ============================================================
-- 图书管理系统 V2 升级脚本
-- 执行前备份 db_book
-- ============================================================

USE db_book;

-- ============================================================
-- 一、扩展现有表
-- ============================================================

ALTER TABLE tb_user
  ADD COLUMN tier_id INT DEFAULT 1 COMMENT '等级ID',
  ADD COLUMN credit INT NOT NULL DEFAULT 100 COMMENT '信用分',
  ADD COLUMN perms BIGINT NOT NULL DEFAULT 0 COMMENT '管理员权限位',
  ADD COLUMN status TINYINT NOT NULL DEFAULT 1 COMMENT '1正常 0禁用',
  ADD INDEX idx_user_tier (tier_id),
  ADD INDEX idx_user_credit (credit);

ALTER TABLE tb_borrow
  ADD COLUMN due_date DATE COMMENT '应还日期',
  ADD COLUMN fine DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '罚金',
  ADD COLUMN paid TINYINT NOT NULL DEFAULT 0 COMMENT '罚金0未付1已付',
  ADD COLUMN overdue INT NOT NULL DEFAULT 0 COMMENT '逾期天数',
  ADD COLUMN renewed TINYINT NOT NULL DEFAULT 0 COMMENT '是否续借',
  ADD INDEX idx_borrow_due (due_date);

ALTER TABLE tb_book
  ADD COLUMN location VARCHAR(100) COMMENT '馆藏位置',
  ADD COLUMN coll_status TINYINT NOT NULL DEFAULT 1 COMMENT '1在馆2借出3遗失4维修',
  ADD INDEX idx_book_coll (coll_status);

-- ============================================================
-- 二、新表
-- ============================================================

-- 2.1 读者等级
CREATE TABLE IF NOT EXISTS tb_tier (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '等级名：普通/银卡/金卡/钻石',
    min_score INT NOT NULL COMMENT '最低信用要求',
    max_borrow INT NOT NULL COMMENT '可借本数',
    days INT NOT NULL COMMENT '借阅天数',
    renew_max INT NOT NULL DEFAULT 2 COMMENT '最多续借次数',
    renew_days INT NOT NULL DEFAULT 15 COMMENT '每次续借天数',
    priority INT NOT NULL DEFAULT 0 COMMENT '预约优先级',
    rate DECIMAL(5,3) NOT NULL DEFAULT 0.05 COMMENT '逾期费率 元/天',
    note VARCHAR(255),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO tb_tier (name, min_score, max_borrow, days, renew_max, renew_days, priority, rate, note) VALUES
('普通读者', 0,  3,  30, 1, 15, 0, 0.05, '默认'),
('银卡读者', 70, 5,  30, 2, 15, 1, 0.04, '信用良好'),
('金卡读者', 85, 8,  45, 2, 15, 2, 0.03, '信用优秀'),
('钻石读者', 95, 10, 60, 3, 15, 3, 0.02, '最高等级');

-- 2.2 权限定义（位掩码）
CREATE TABLE IF NOT EXISTS tb_perm (
    id INT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '权限编码',
    name VARCHAR(100) NOT NULL COMMENT '权限名',
    grp VARCHAR(50) NOT NULL COMMENT '分组',
    pos INT NOT NULL COMMENT '位位置 0~63',
    note VARCHAR(255),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO tb_perm (code, name, grp, pos, note) VALUES
('user.view',     '查看读者', '用户', 0,  '查看列表和详情'),
('user.edit',     '编辑读者', '用户', 1,  '增改读者'),
('user.delete',   '删除读者', '用户', 2,  '删除账号'),
('user.credit',   '调整信用', '用户', 3,  '手动调信用分'),

('book.view',     '查看图书', '图书', 10, '查看列表和详情'),
('book.edit',     '编辑图书', '图书', 11, '增改图书'),
('book.delete',   '删除图书', '图书', 12, '删除图书'),

('borrow.view',   '查看借阅', '借阅', 20, '查看借阅记录'),
('borrow.force',  '强制归还', '借阅', 21, '代还/强还'),
('reserve.manage', '预约管理', '借阅', 22, '处理预约'),
('renew.manage',   '续借审批', '借阅', 23, '审批续借'),

('fine.view',     '查看罚金', '财务', 30, '看逾期罚金'),
('fine.waive',    '减免罚金', '财务', 31, '减免/清零'),
('claim.handle',  '遗失理赔', '财务', 32, '处理遗失赔偿'),

('inv.view',      '查看盘点', '盘点', 40, '看盘点任务'),
('inv.do',        '执行盘点', '盘点', 41, '实地盘点'),
('inv.finish',    '结算盘点', '盘点', 42, '审批盘点差异'),

('sug.view',      '查看荐购', '荐购', 50, '看荐购建议'),
('sug.approve',   '审批荐购', '荐购', 51, '采纳/驳回'),

('log.view',      '查看日志', '系统', 60, '看操作日志'),
('log.export',    '导出日志', '系统', 61, '导出文件'),
('dash.view',     '数据看板', '系统', 62, '看借阅看板'),
('sys.config',    '系统配置', '系统', 63, '改参数/等级');

-- 2.3 操作日志
CREATE TABLE IF NOT EXISTS tb_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    username VARCHAR(50),
    action VARCHAR(50) NOT NULL COMMENT 'LOGIN/LOGOUT/CREATE/UPDATE/DELETE/BORROW/RETURN/RESERVE/PAY',
    target VARCHAR(50) COMMENT '对象类型 USER/BOOK/BORROW/RESERVE',
    target_id INT,
    detail VARCHAR(1000),
    ip VARCHAR(50),
    ua VARCHAR(255),
    ok TINYINT NOT NULL DEFAULT 1 COMMENT '1成功 0失败',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_log_user (user_id),
    INDEX idx_log_action (action),
    INDEX idx_log_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2.4 图书预约
CREATE TABLE IF NOT EXISTS tb_reserve (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    book_id INT NOT NULL,
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0等待 1已通知 2已取消 3已过期 4已取书',
    reserve_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notify_time TIMESTAMP NULL,
    deadline TIMESTAMP NULL COMMENT '取书截止时间',
    FOREIGN KEY (user_id) REFERENCES tb_user(id),
    FOREIGN KEY (book_id) REFERENCES tb_book(id),
    INDEX idx_rsv_user (user_id),
    INDEX idx_rsv_book (book_id),
    INDEX idx_rsv_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2.5 罚金流水
CREATE TABLE IF NOT EXISTS tb_fine (
    id INT PRIMARY KEY AUTO_INCREMENT,
    borrow_id INT NOT NULL,
    user_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    overdue_days INT NOT NULL,
    rate DECIMAL(5,3) NOT NULL,
    reason VARCHAR(200) DEFAULT '逾期归还',
    pay_status TINYINT NOT NULL DEFAULT 0 COMMENT '0未付 1已付 2已减免',
    pay_time TIMESTAMP NULL,
    waive_reason VARCHAR(200),
    waive_by INT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (borrow_id) REFERENCES tb_borrow(id),
    FOREIGN KEY (user_id) REFERENCES tb_user(id),
    INDEX idx_fine_user (user_id),
    INDEX idx_fine_borrow (borrow_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2.6 遗失理赔
CREATE TABLE IF NOT EXISTS tb_claim (
    id INT PRIMARY KEY AUTO_INCREMENT,
    borrow_id INT NOT NULL,
    user_id INT NOT NULL,
    book_id INT NOT NULL,
    report_date DATE NOT NULL COMMENT '报失日期',
    type TINYINT NOT NULL COMMENT '1原价 2折旧 3原书购买',
    book_price DECIMAL(10,2) NOT NULL COMMENT '图书原价',
    dep_rate DECIMAL(5,3) DEFAULT 1.0 COMMENT '折旧率',
    amount DECIMAL(10,2) NOT NULL COMMENT '应赔金额',
    paid DECIMAL(10,2) DEFAULT 0 COMMENT '实付',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0待处理 1处理中 2已结案 3已驳回',
    handle_by INT COMMENT '处理人',
    handle_remark VARCHAR(500),
    pay_time TIMESTAMP NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (borrow_id) REFERENCES tb_borrow(id),
    FOREIGN KEY (user_id) REFERENCES tb_user(id),
    FOREIGN KEY (book_id) REFERENCES tb_book(id),
    INDEX idx_claim_user (user_id),
    INDEX idx_claim_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2.7 信用变动流水
CREATE TABLE IF NOT EXISTS tb_credit (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    delta INT NOT NULL COMMENT '变动值 正负',
    `before` INT NOT NULL,
    `after` INT NOT NULL,
    reason VARCHAR(200) NOT NULL COMMENT '按时还书/逾期/遗失赔偿/管理员调整',
    op_by INT COMMENT '管理员调整时填',
    ref_type VARCHAR(50) COMMENT 'BORROW/FINE/CLAIM/ADMIN',
    ref_id INT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES tb_user(id),
    INDEX idx_credit_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2.8 盘点主表
CREATE TABLE IF NOT EXISTS tb_inventory (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '盘点名称',
    type TINYINT NOT NULL COMMENT '1全盘 2区域 3抽样',
    area VARCHAR(100) COMMENT '盘点区域',
    total INT DEFAULT 0 COMMENT '应盘',
    actual INT DEFAULT 0 COMMENT '实盘',
    missing INT DEFAULT 0 COMMENT '遗失',
    extra INT DEFAULT 0 COMMENT '多出',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0待开始 1进行中 2已完成 3已取消',
    start_time TIMESTAMP NULL,
    end_time TIMESTAMP NULL,
    creator INT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2.9 盘点明细
CREATE TABLE IF NOT EXISTS tb_inv_item (
    id INT PRIMARY KEY AUTO_INCREMENT,
    inv_id INT NOT NULL,
    book_id INT NOT NULL,
    isbn VARCHAR(20),
    title VARCHAR(200),
    sys_status TINYINT COMMENT '系统馆藏状态',
    real_status TINYINT COMMENT '实地 1在架 2遗失 3破损 4错架',
    remark VARCHAR(255),
    checked_by INT,
    checked_time TIMESTAMP NULL,
    FOREIGN KEY (inv_id) REFERENCES tb_inventory(id),
    FOREIGN KEY (book_id) REFERENCES tb_book(id),
    INDEX idx_item_inv (inv_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2.10 图书荐购
CREATE TABLE IF NOT EXISTS tb_suggestion (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    type TINYINT NOT NULL DEFAULT 1 COMMENT '1补库存 2新增图书',
    title VARCHAR(200) NOT NULL,
    author VARCHAR(100),
    publisher VARCHAR(200),
    isbn VARCHAR(20),
    category VARCHAR(50),
    price DECIMAL(10,2),
    reason VARCHAR(500),
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0待审 1采纳 2驳回 3采购 4到馆',
    votes INT NOT NULL DEFAULT 0,
    handle_by INT,
    handle_remark VARCHAR(500),
    handle_time TIMESTAMP NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES tb_user(id),
    INDEX idx_sug_user (user_id),
    INDEX idx_sug_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2.11 消息通知
CREATE TABLE IF NOT EXISTS tb_notice (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    type TINYINT NOT NULL COMMENT '1到期提醒 2逾期提醒 3预约取书 4系统 5荐购处理',
    title VARCHAR(200) NOT NULL,
    content VARCHAR(1000),
    ref_type VARCHAR(50),
    ref_id INT,
    read_flag TINYINT NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES tb_user(id),
    INDEX idx_notice_user (user_id),
    INDEX idx_notice_read (read_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 三、回填数据（迁移脚本后半段，执行完建表后运行）
-- ============================================================

UPDATE tb_user SET tier_id = 1 WHERE tier_id IS NULL;
UPDATE tb_user SET tier_id = 4, credit = 100 WHERE role = 1;
UPDATE tb_user SET tier_id = 2, credit = 80 WHERE role = 3;
UPDATE tb_user SET tier_id = 2 WHERE role = 2 AND tier_id = 1;
UPDATE tb_user SET perms = -1 WHERE role = 1;

-- 现有在借（status=0）的借阅按 borrow_date + 30 天 回填应还日期
UPDATE tb_borrow SET due_date = DATE_ADD(borrow_date, INTERVAL 30 DAY) WHERE status = 0 AND due_date IS NULL;

-- 已归还的也补 due_date（取归还日期兜底）
UPDATE tb_borrow SET due_date = return_date WHERE status = 1 AND due_date IS NULL AND return_date IS NOT NULL;

-- 所有图书默认馆藏状态
UPDATE tb_book SET coll_status = 1 WHERE coll_status IS NULL;

-- 2.12 荐购点赞记录（判重）
CREATE TABLE IF NOT EXISTS tb_suggestion_like (
    id INT PRIMARY KEY AUTO_INCREMENT,
    sug_id INT NOT NULL,
    user_id INT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_sug_user (sug_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 修正种子用户乱码姓名
UPDATE tb_user SET name = '张三' WHERE username = 'user1';
UPDATE tb_user SET name = '李四' WHERE username = 'user2';
UPDATE tb_user SET name = '王五' WHERE username = 'user3';
