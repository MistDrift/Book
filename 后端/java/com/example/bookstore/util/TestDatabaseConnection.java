package com.example.bookstore.util;

import com.example.bookstore.dao.BookDAO;
import com.example.bookstore.dao.BorrowDAO;
import com.example.bookstore.dao.UserDAO;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Borrow;
import com.example.bookstore.entity.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

public class TestDatabaseConnection {
    public static void main(String[] args) {
        try {
            // 测试数据库连接
            String url = "jdbc:mysql://localhost:3306/db_book?useSSL=false&serverTimezone=UTC&characterEncoding=utf8&allowPublicKeyRetrieval=true";
            String user = "root";
            String password = "root@root";
            
            try (Connection conn = DriverManager.getConnection(url, user, password)) {
                System.out.println("✅ 数据库连接成功！");
                System.out.println();
            }
            
            // 测试用户功能
            UserDAO userDAO = new UserDAO();
            System.out.println("=== 用户功能测试 ===");
            User loginUser = userDAO.login("admin", "admin123");
            System.out.println("✅ 登录测试: " + (loginUser != null ? "成功 - " + loginUser.getName() : "失败"));
            
            List<User> allUsers = userDAO.findAll();
            System.out.println("✅ 查询所有用户: " + allUsers.size() + " 个用户");
            System.out.println();
            
            // 测试图书功能
            BookDAO bookDAO = new BookDAO();
            System.out.println("=== 图书功能测试 ===");
            List<Book> allBooks = bookDAO.findAll();
            System.out.println("✅ 查询所有图书: " + allBooks.size() + " 本书");
            
            Book book1 = bookDAO.findById(1);
            System.out.println("✅ 查询图书ID=1: " + (book1 != null ? book1.getTitle() : "未找到"));
            
            List<Book> searchBooks = bookDAO.search("Java");
            System.out.println("✅ 搜索'Java': " + searchBooks.size() + " 本书");
            
            List<Book> categoryBooks = bookDAO.findByCategory("编程");
            System.out.println("✅ 按分类'编程'查询: " + categoryBooks.size() + " 本书");
            System.out.println();
            
            // 测试借阅功能
            BorrowDAO borrowDAO = new BorrowDAO();
            System.out.println("=== 借阅功能测试 ===");
            List<Borrow> allBorrows = borrowDAO.findAll();
            System.out.println("✅ 查询所有借阅记录: " + allBorrows.size() + " 条");
            
            List<Borrow> userBorrows = borrowDAO.findByUserId(2);
            System.out.println("✅ 查询用户ID=2的借阅记录: " + userBorrows.size() + " 条");
            
            int borrowingCount = borrowDAO.countBorrowingByBookId(3);
            System.out.println("✅ 查询图书ID=3的未归还借阅数: " + borrowingCount + " 条");
            System.out.println();
            
            System.out.println("=== 所有功能测试完成 ===");
            System.out.println("✅ 系统已完全连接到数据库！");
            
        } catch (Exception e) {
            System.err.println("❌ 测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}