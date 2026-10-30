package com.example.bookstore.dao;

import com.example.bookstore.entity.Fine;
import com.example.bookstore.util.JdbcUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FineDAO {

    private static final String BASE =
        "SELECT f.*, u.username, b.title AS book_title FROM tb_fine f " +
        "JOIN tb_user u ON f.user_id=u.id " +
        "JOIN tb_borrow br ON f.borrow_id=br.id " +
        "JOIN tb_book b ON br.book_id=b.id";

    private Fine map(ResultSet rs) throws SQLException {
        Fine f = new Fine();
        f.id = rs.getInt("id");
        f.borrowId = rs.getInt("borrow_id");
        f.userId = rs.getInt("user_id");
        f.amount = rs.getBigDecimal("amount");
        f.overdueDays = rs.getInt("overdue_days");
        f.rate = rs.getBigDecimal("rate");
        f.reason = rs.getString("reason");
        f.payStatus = rs.getInt("pay_status");
        f.payTime = rs.getTimestamp("pay_time");
        f.waiveReason = rs.getString("waive_reason");
        f.waiveBy = rs.getInt("waive_by");
        if (rs.wasNull()) f.waiveBy = null;
        f.createTime = rs.getTimestamp("create_time");
        f.username = rs.getString("username");
        f.bookTitle = rs.getString("book_title");
        return f;
    }

    private List<Fine> query(String sql, Object... params) {
        List<Fine> list = new ArrayList<>();
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Fine> findAll() {
        return query(BASE + " ORDER BY f.id DESC");
    }

    public List<Fine> findByUser(Integer userId) {
        return query(BASE + " WHERE f.user_id=? ORDER BY f.id DESC", userId);
    }

    public List<Fine> findByPayStatus(Integer payStatus) {
        return query(BASE + " WHERE f.pay_status=? ORDER BY f.id DESC", payStatus);
    }

    public Fine findById(Integer id) {
        List<Fine> list = query(BASE + " WHERE f.id=?", id);
        return list.isEmpty() ? null : list.get(0);
    }

    public int insert(Fine f) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO tb_fine (borrow_id,user_id,amount,overdue_days,rate,reason,pay_status) " +
                 "VALUES (?,?,?,?,?,?,0)")) {
            stmt.setInt(1, f.borrowId);
            stmt.setInt(2, f.userId);
            stmt.setBigDecimal(3, f.amount);
            stmt.setInt(4, f.overdueDays);
            stmt.setBigDecimal(5, f.rate);
            stmt.setString(6, f.reason);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int pay(Integer id) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE tb_fine SET pay_status=1, pay_time=NOW() WHERE id=?")) {
            stmt.setInt(1, id);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int waive(Integer id, String reason, Integer adminId) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE tb_fine SET pay_status=2, waive_reason=?, waive_by=? WHERE id=?")) {
            stmt.setString(1, reason);
            stmt.setObject(2, adminId);
            stmt.setInt(3, id);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public BigDecimal sumUnpaid(Integer userId) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT COALESCE(SUM(amount),0) FROM tb_fine WHERE user_id=? AND pay_status=0")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) { if (rs.next()) return rs.getBigDecimal(1); }
        } catch (SQLException e) { e.printStackTrace(); }
        return BigDecimal.ZERO;
    }
}
