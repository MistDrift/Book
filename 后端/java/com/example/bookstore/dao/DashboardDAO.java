package com.example.bookstore.dao;

import com.example.bookstore.util.JdbcUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardDAO {

    private Map<String, Object> row(String sql, Object... params) {
        List<Map<String, Object>> list = rows(sql, params);
        return list.isEmpty() ? new HashMap<>() : list.get(0);
    }

    private List<Map<String, Object>> rows(String sql, Object... params) {
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    int cols = rs.getMetaData().getColumnCount();
                    for (int i = 1; i <= cols; i++) {
                        row.put(rs.getMetaData().getColumnLabel(i), rs.getObject(i));
                    }
                    list.add(row);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private int count(String sql) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public Map<String, Object> summary() {
        Map<String, Object> map = new HashMap<>();
        map.put("bookCount", count("SELECT COUNT(*) FROM tb_book"));
        map.put("userCount", count("SELECT COUNT(*) FROM tb_user"));
        map.put("borrowing", count("SELECT COUNT(*) FROM tb_borrow WHERE status=0"));
        map.put("overdue", count("SELECT COUNT(*) FROM tb_borrow WHERE status=0 AND due_date<CURDATE()"));
        map.put("reserveActive", count("SELECT COUNT(*) FROM tb_reserve WHERE status IN (0,1)"));
        map.put("unpaidFine", count("SELECT COUNT(*) FROM tb_fine WHERE pay_status=0"));
        return map;
    }

    public List<Map<String, Object>> borrowTrend(int days) {
        return rows("SELECT DATE_FORMAT(borrow_date,'%Y-%m-%d') AS d, COUNT(*) AS c FROM tb_borrow " +
                    "WHERE borrow_date >= DATE_SUB(CURDATE(), INTERVAL ? DAY) " +
                    "GROUP BY d ORDER BY d", days);
    }

    public List<Map<String, Object>> categoryDist() {
        return rows("SELECT category, COUNT(*) AS c FROM tb_book GROUP BY category ORDER BY c DESC");
    }

    public List<Map<String, Object>> topBooks(int n) {
        return rows("SELECT bo.title, COUNT(*) AS c FROM tb_borrow b " +
                    "JOIN tb_book bo ON b.book_id=bo.id " +
                    "GROUP BY bo.id, bo.title ORDER BY c DESC LIMIT ?", n);
    }

    /**
     * 用户身份分布：仅统计教师(role=3)和学生(role=2)两类，
     * 返回 name（身份名称）、c（数量）、pct（占两类总人数百分比，保留1位小数）。
     */
    public List<Map<String, Object>> roleDist() {
        List<Map<String, Object>> list = rows(
            "SELECT CASE role WHEN 3 THEN '教师' ELSE '学生' END AS name, COUNT(*) AS c " +
            "FROM tb_user WHERE role IN (2,3) GROUP BY role ORDER BY role DESC");
        long total = 0;
        for (Map<String, Object> r : list) total += ((Number) r.get("c")).longValue();
        for (Map<String, Object> r : list) {
            long c = ((Number) r.get("c")).longValue();
            double pct = total == 0 ? 0.0 : Math.round(c * 1000.0 / total) / 10.0;
            r.put("pct", pct);
        }
        return list;
    }

    public List<Map<String, Object>> recentBorrows(int n) {
        return rows("SELECT u.username, bo.title AS bookTitle, b.borrow_date AS borrowDate, b.status " +
                    "FROM tb_borrow b " +
                    "JOIN tb_user u ON b.user_id=u.id " +
                    "JOIN tb_book bo ON b.book_id=bo.id " +
                    "ORDER BY b.create_time DESC LIMIT ?", n);
    }

    public Map<String, Object> fineSummary() {
        Map<String, Object> map = new HashMap<>();
        map.put("unpaidAmount", row("SELECT COALESCE(SUM(amount),0) AS v FROM tb_fine WHERE pay_status=0").get("v"));
        map.put("unpaidCount", count("SELECT COUNT(*) FROM tb_fine WHERE pay_status=0"));
        map.put("collected", row("SELECT COALESCE(SUM(amount),0) AS v FROM tb_fine WHERE pay_status=1").get("v"));
        return map;
    }

    public BigDecimal borrowTotal() {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM tb_borrow");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return BigDecimal.ZERO;
    }
}
