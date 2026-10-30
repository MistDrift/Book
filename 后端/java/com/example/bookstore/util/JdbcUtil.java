package com.example.bookstore.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class JdbcUtil {
    
    private static String URL;
    private static String USER;
    private static String PASSWORD;
    
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            Properties props = new Properties();
            InputStream is = JdbcUtil.class.getClassLoader().getResourceAsStream("db.properties");
            
            if (is != null) {
                props.load(is);
                URL = props.getProperty("db.url", "jdbc:mysql://localhost:3306/db_book?useSSL=false&serverTimezone=UTC&characterEncoding=utf8&allowPublicKeyRetrieval=true");
                USER = props.getProperty("db.username", "root");
                PASSWORD = props.getProperty("db.password", "root@root");
                is.close();
            } else {
                URL = "jdbc:mysql://localhost:3306/db_book?useSSL=false&serverTimezone=UTC&characterEncoding=utf8&allowPublicKeyRetrieval=true";
                USER = "root";
                PASSWORD = "root@root";
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL驱动加载失败", e);
        } catch (Exception e) {
            throw new RuntimeException("配置文件加载失败", e);
        }
    }
    
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException("数据库连接失败", e);
        }
    }
    
    public static void close(Connection conn, PreparedStatement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void close(Connection conn, PreparedStatement stmt) {
        close(conn, stmt, null);
    }
    
    public static void close(Connection conn) {
        close(conn, null, null);
    }
}
