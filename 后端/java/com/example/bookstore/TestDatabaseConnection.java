package com.example.bookstore;

import com.example.bookstore.dao.BookDAO;
import com.example.bookstore.dao.UserDAO;
import com.example.bookstore.dao.BorrowDAO;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.User;
import com.example.bookstore.entity.Borrow;

import java.util.List;

public class TestDatabaseConnection {
    
    public static void main(String[] args) {
        System.out.println("=== 测试数据库连接 ===");
        
        try {
            // 测试用户DAO
            System.out.println("\n1. 测试用户表连接...");
            UserDAO userDAO = new UserDAO();
            List<User> users = userDAO.findAll();
            System.out.println("   ✓ 用户表查询成功，共 " + users.size() + " 个用户");
            for (User user : users) {
                System.out.println("     - " + user.getUsername() + " (" + user.getName() + ")");
            }
            
            // 测试图书DAO
            System.out.println("\n2. 测试图书表连接...");
            BookDAO bookDAO = new BookDAO();
            List<Book> books = bookDAO.findAll();
            System.out.println("   ✓ 图书表查询成功，共 " + books.size() + " 本书");
            
            // 统计各类别图书
            System.out.println("\n   图书分类统计：");
            long historyBooks = books.stream().filter(b -> "历史".equals(b.getCategory())).count();
            long programmingBooks = books.stream().filter(b -> "编程".equals(b.getCategory())).count();
            long otherBooks = books.size() - historyBooks - programmingBooks;
            System.out.println("     - 历史类：" + historyBooks + " 本");
            System.out.println("     - 编程类：" + programmingBooks + " 本");
            System.out.println("     - 其他类：" + otherBooks + " 本");
            
            // 测试借阅DAO
            System.out.println("\n3. 测试借阅表连接...");
            BorrowDAO borrowDAO = new BorrowDAO();
            List<Borrow> borrows = borrowDAO.findAll();
            System.out.println("   ✓ 借阅表查询成功，共 " + borrows.size() + " 条记录");
            
            // 测试登录功能
            System.out.println("\n4. 测试登录功能...");
            User admin = userDAO.login("admin", "admin123");
            if (admin != null) {
                System.out.println("   ✓ 管理员登录成功：" + admin.getName());
            } else {
                System.out.println("   ✗ 管理员登录失败");
            }
            
            User user1 = userDAO.login("user1", "user123");
            if (user1 != null) {
                System.out.println("   ✓ 普通用户登录成功：" + user1.getName());
            } else {
                System.out.println("   ✗ 普通用户登录失败");
            }
            
            System.out.println("\n=== 所有功能已成功连接数据库 ===");
            
        } catch (Exception e) {
            System.out.println("\n✗ 数据库连接失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
}