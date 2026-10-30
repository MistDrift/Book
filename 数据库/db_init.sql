CREATE DATABASE IF NOT EXISTS db_book DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE db_book;

CREATE TABLE IF NOT EXISTS tb_user (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    name VARCHAR(50) NOT NULL,
    role INT NOT NULL DEFAULT 0,
    email VARCHAR(100),
    phone VARCHAR(20),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_book (
    id INT PRIMARY KEY AUTO_INCREMENT,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(100) NOT NULL,
    publisher VARCHAR(200) NOT NULL,
    publish_date DATE NOT NULL,
    category VARCHAR(50) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    description TEXT,
    image VARCHAR(255),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_borrow (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    book_id INT NOT NULL,
    borrow_date DATE NOT NULL,
    return_date DATE,
    status INT NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES tb_user(id),
    FOREIGN KEY (book_id) REFERENCES tb_book(id)
);

INSERT INTO tb_user (username, password, name, role, email, phone) VALUES
('admin', 'admin123', '管理员', 1, 'admin@example.com', '13800138000'),
('student1', 'student123', '张三', 2, 'student1@example.com', '13800138001'),
('teacher1', 'teacher123', '李四', 3, 'teacher1@example.com', '13800138002'),
('social1', 'social123', '王五', 4, 'social1@example.com', '13800138003');

INSERT INTO tb_book (isbn, title, author, publisher, publish_date, category, price, stock, description, image) VALUES
('9787111544937', 'Java编程思想', 'Bruce Eckel', '机械工业出版社', '2019-10-01', '编程', 139.00, 10, 'Java学习经典书籍', 'https://picsum.photos/seed/java1/200/300.jpg'),
('9787508698766', '深入理解计算机系统', 'Randal E. Bryant', '机械工业出版社', '2016-01-01', '计算机', 129.00, 8, '深入理解计算机系统原理', 'https://picsum.photos/seed/computer1/200/300.jpg'),
('9787115428577', 'Python编程从入门到实践', 'Eric Matthes', '人民邮电出版社', '2020-01-01', '编程', 79.00, 15, 'Python入门经典', 'https://picsum.photos/seed/python1/200/300.jpg'),
('9787564183528', '数据库系统概念', 'Abraham Silberschatz', '机械工业出版社', '2018-01-01', '数据库', 99.00, 5, '数据库系统基础', 'https://picsum.photos/seed/database1/200/300.jpg'),
('9787114070101', '算法导论', 'Mark Allen Weiss', '机械工业出版社', '2017-01-01', '算法', 79.00, 12, '算法学习经典', 'https://picsum.photos/seed/algorithm1/200/300.jpg'),
('9787115375577', 'JavaScript高级程序设计', 'Nicholas C.Zakas', '人民邮电出版社', '2020-01-01', '编程', 129.00, 6, 'JavaScript高级技术', 'https://picsum.photos/seed/js1/200/300.jpg'),
('9787115451474', 'Vue.js设计与实现', '霍春阳', '人民邮电出版社', '2022-01-01', '编程', 119.00, 8, 'Vue.js核心原理', 'https://picsum.photos/seed/vue1/200/300.jpg'),
('9787568288666', '深入浅出Java', '陈刚', '电子工业出版社', '2021-01-01', '编程', 129.00, 10, 'Java入门到精通', 'https://picsum.photos/seed/javabook1/200/300.jpg');

INSERT INTO tb_borrow (user_id, book_id, borrow_date, return_date, status) VALUES
(2, 1, '2024-01-10', '2024-01-25', 1),
(2, 3, '2024-02-01', NULL, 0),
(3, 2, '2024-02-10', NULL, 0),
(4, 5, '2024-03-01', '2024-03-15', 1),
(3, 6, '2024-03-10', NULL, 0);
