package com.example.bookstore.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class InitData {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://localhost:3306/db_book?useSSL=false&serverTimezone=UTC&characterEncoding=utf8&allowPublicKeyRetrieval=true";
        String user = "root";
        String password = "root@root";
        
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String[] userSql = {
                "INSERT INTO tb_user (username, password, name, role, email, phone) VALUES ('admin', 'admin123', '管理员', 1, 'admin@example.com', '13800138000')",
                "INSERT INTO tb_user (username, password, name, role, email, phone) VALUES ('user1', 'user123', '张三', 0, 'user1@example.com', '13800138001')",
                "INSERT INTO tb_user (username, password, name, role, email, phone) VALUES ('user2', 'user123', '李四', 0, 'user2@example.com', '13800138002')",
                "INSERT INTO tb_user (username, password, name, role, email, phone) VALUES ('user3', 'user123', '王五', 0, 'user3@example.com', '13800138003')"
            };
            
            String[] bookSql = {
                "INSERT INTO tb_book (isbn, title, author, publisher, publish_date, category, price, stock, description) VALUES ('9787111544937', 'Java编程思想', 'Bruce Eckel', '机械工业出版社', '2019-10-01', '编程', 139.00, 10, 'Java学习经典书籍')",
                "INSERT INTO tb_book (isbn, title, author, publisher, publish_date, category, price, stock, description) VALUES ('9787508698766', '深入理解计算机系统', 'Randal E. Bryant', '机械工业出版社', '2016-01-01', '计算机', 129.00, 8, '深入理解计算机系统原理')",
                "INSERT INTO tb_book (isbn, title, author, publisher, publish_date, category, price, stock, description) VALUES ('9787115428577', 'Python编程从入门到实践', 'Eric Matthes', '人民邮电出版社', '2020-01-01', '编程', 79.00, 15, 'Python入门经典')",
                "INSERT INTO tb_book (isbn, title, author, publisher, publish_date, category, price, stock, description) VALUES ('9787564183528', '数据库系统概念', 'Abraham Silberschatz', '机械工业出版社', '2018-01-01', '数据库', 99.00, 5, '数据库系统基础')",
                "INSERT INTO tb_book (isbn, title, author, publisher, publish_date, category, price, stock, description) VALUES ('9787114070101', '算法导论', 'Mark Allen Weiss', '机械工业出版社', '2017-01-01', '算法', 79.00, 12, '算法学习经典')",
                "INSERT INTO tb_book (isbn, title, author, publisher, publish_date, category, price, stock, description) VALUES ('9787115375577', 'JavaScript高级程序设计', 'Nicholas C.Zakas', '人民邮电出版社', '2020-01-01', '编程', 129.00, 6, 'JavaScript高级技术')",
                "INSERT INTO tb_book (isbn, title, author, publisher, publish_date, category, price, stock, description) VALUES ('9787115451474', 'Vue.js设计与实现', '霍春阳', '人民邮电出版社', '2022-01-01', '编程', 119.00, 8, 'Vue.js核心原理')",
                "INSERT INTO tb_book (isbn, title, author, publisher, publish_date, category, price, stock, description) VALUES ('9787568288666', '深入浅出Java', '陈刚', '电子工业出版社', '2021-01-01', '编程', 129.00, 10, 'Java入门到精通')",
                "INSERT INTO tb_book (isbn, title, author, publisher, publish_date, category, price, stock, description) VALUES ('9787115478577', 'Spring Boot实战', 'Craig Walls', '人民邮电出版社', '2021-06-01', '编程', 89.00, 12, 'Spring Boot核心技术')",
                "INSERT INTO tb_book (isbn, title, author, publisher, publish_date, category, price, stock, description) VALUES ('9787115541474', 'Redis设计与实现', '黄健宏', '人民邮电出版社', '2020-01-01', '数据库', 79.00, 8, 'Redis内部设计详解')",
                "INSERT INTO tb_book (isbn, title, author, publisher, publish_date, category, price, stock, description) VALUES ('9787115488577', 'MySQL技术内幕', '姜承尧', '机械工业出版社', '2019-03-01', '数据库', 99.00, 6, 'MySQL存储引擎与索引')",
                "INSERT INTO tb_book (isbn, title, author, publisher, publish_date, category, price, stock, description) VALUES ('9787115498477', 'HTTP权威指南', 'David Gourley', '人民邮电出版社', '2020-09-01', '网络', 109.00, 5, 'HTTP协议详解')",
                "INSERT INTO tb_book (isbn, title, author, publisher, publish_date, category, price, stock, description) VALUES ('9787115508477', 'Linux高性能服务器编程', '游双', '机械工业出版社', '2018-08-01', '操作系统', 89.00, 7, 'Linux服务器开发指南')",
                "INSERT INTO tb_book (isbn, title, author, publisher, publish_date, category, price, stock, description) VALUES ('9787115518477', 'Effective Java', 'Joshua Bloch', '电子工业出版社', '2020-05-01', '编程', 119.00, 9, 'Java最佳实践')",
                "INSERT INTO tb_book (isbn, title, author, publisher, publish_date, category, price, stock, description) VALUES ('9787115528477', '计算机网络', '谢希仁', '电子工业出版社', '2019-01-01', '网络', 59.00, 15, '计算机网络基础')",
                "INSERT INTO tb_book (isbn, title, author, publisher, publish_date, category, price, stock, description) VALUES ('9787115538477', '数据结构与算法', '张铭', '高等教育出版社', '2020-01-01', '基础课', 49.00, 20, '数据结构与算法基础')",
                "INSERT INTO tb_book (isbn, title, author, publisher, publish_date, category, price, stock, description) VALUES ('9787115548477', '操作系统概念', 'Silberschatz', '高等教育出版社', '2018-06-01', '操作系统', 89.00, 10, '操作系统核心概念')",
                "INSERT INTO tb_book (isbn, title, author, publisher, publish_date, category, price, stock, description) VALUES ('9787115558477', '编译原理', 'Alfred Aho', '机械工业出版社', '2019-11-01', '计算机', 99.00, 5, '编译技术基础')",
                "INSERT INTO tb_book (isbn, title, author, publisher, publish_date, category, price, stock, description) VALUES ('9787115568477', '人工智能：一种现代方法', 'Stuart Russell', '电子工业出版社', '2021-02-01', '人工智能', 149.00, 6, 'AI经典教材')",
                "INSERT INTO tb_book (isbn, title, author, publisher, publish_date, category, price, stock, description) VALUES ('9787115578477', '深度学习入门', '斋藤康毅', '人民邮电出版社', '2022-03-01', '人工智能', 59.00, 8, '深度学习基础教程')"
            };
            
            String[] borrowSql = {
                "INSERT INTO tb_borrow (user_id, book_id, borrow_date, return_date, status) VALUES (2, 1, '2024-01-10', '2024-01-25', 1)",
                "INSERT INTO tb_borrow (user_id, book_id, borrow_date, return_date, status) VALUES (2, 3, '2024-02-01', NULL, 0)",
                "INSERT INTO tb_borrow (user_id, book_id, borrow_date, return_date, status) VALUES (3, 2, '2024-02-10', NULL, 0)",
                "INSERT INTO tb_borrow (user_id, book_id, borrow_date, return_date, status) VALUES (4, 5, '2024-03-01', '2024-03-15', 1)",
                "INSERT INTO tb_borrow (user_id, book_id, borrow_date, return_date, status) VALUES (3, 6, '2024-03-10', NULL, 0)"
            };
            
            // 先清空所有表
            conn.prepareStatement("SET FOREIGN_KEY_CHECKS=0").executeUpdate();
            conn.prepareStatement("TRUNCATE TABLE tb_borrow").executeUpdate();
            conn.prepareStatement("TRUNCATE TABLE tb_user").executeUpdate();
            conn.prepareStatement("TRUNCATE TABLE tb_book").executeUpdate();
            conn.prepareStatement("SET FOREIGN_KEY_CHECKS=1").executeUpdate();
            System.out.println("Tables truncated");
            
            for (String sql : userSql) {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.executeUpdate();
                }
            }
            System.out.println("Users inserted");
            
            for (String sql : bookSql) {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.executeUpdate();
                }
            }
            System.out.println("Books inserted");
            
            for (String sql : borrowSql) {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.executeUpdate();
                }
            }
            System.out.println("Borrows inserted");
            
            System.out.println("Data initialization completed!");
        }
    }
}
