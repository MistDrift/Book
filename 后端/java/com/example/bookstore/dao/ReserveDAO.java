package com.example.bookstore.dao;

import com.example.bookstore.entity.Reserve;
import com.example.bookstore.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReserveDAO {

    private static final String BASE =
        "SELECT r.*, u.username, b.title AS book_title, t.name AS tier_name, t.priority AS tier_priority " +
        "FROM tb_reserve r " +
        "JOIN tb_user u ON r.user_id=u.id " +
        "LEFT JOIN tb_tier t ON u.tier_id=t.id " +
        "JOIN tb_book b ON r.book_id=b.id";

    private Reserve map(ResultSet rs) throws SQLException {
        Reserve r = new Reserve();
        r.id = rs.getInt("id");
        r.userId = rs.getInt("user_id");
        r.bookId = rs.getInt("book_id");
        r.status = rs.getInt("status");
        r.reserveTime = rs.getTimestamp("reserve_time");
        r.notifyTime = rs.getTimestamp("notify_time");
        r.deadline = rs.getTimestamp("deadline");
        r.username = rs.getString("username");
        r.bookTitle = rs.getString("book_title");
        r.tierName = rs.getString("tier_name");
        r.tierPriority = rs.getInt("tier_priority");
        return r;
    }

    private List<Reserve> query(String sql, Object... params) {
        List<Reserve> list = new ArrayList<>();
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Reserve> findAll() {
        return query(BASE + " ORDER BY r.reserve_time DESC");
    }

    public List<Reserve> findByUser(Integer userId) {
        return query(BASE + " WHERE r.user_id=? ORDER BY r.reserve_time DESC", userId);
    }

    public List<Reserve> findByBook(Integer bookId) {
        return query(BASE + " WHERE r.book_id=? ORDER BY r.reserve_time DESC", bookId);
    }

    public Reserve findById(Integer id) {
        List<Reserve> list = query(BASE + " WHERE r.id=?", id);
        return list.isEmpty() ? null : list.get(0);
    }

    // 预约中（status=0）按等级优先级、预约时间取队首
    public Reserve findFirstHolder(Integer bookId) {
        List<Reserve> list = query(BASE + " WHERE r.book_id=? AND r.status=0 " +
                "ORDER BY t.priority DESC, r.reserve_time ASC LIMIT 1", bookId);
        return list.isEmpty() ? null : list.get(0);
    }

    public boolean existsActive(Integer userId, Integer bookId) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT COUNT(*) FROM tb_reserve WHERE user_id=? AND book_id=? AND status IN (0,1)")) {
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            try (ResultSet rs = stmt.executeQuery()) { if (rs.next()) return rs.getInt(1) > 0; }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public int insert(Reserve r) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO tb_reserve (user_id, book_id, status) VALUES (?,?,0)")) {
            stmt.setInt(1, r.userId);
            stmt.setInt(2, r.bookId);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int updateStatus(Integer id, int status) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE tb_reserve SET status=? WHERE id=?")) {
            stmt.setInt(1, status);
            stmt.setInt(2, id);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // 通知取书：置为已通知并给出取书期限
    public int notifyReady(Integer id, int days) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE tb_reserve SET status=1, notify_time=NOW(), deadline=DATE_ADD(NOW(), INTERVAL ? DAY) WHERE id=?")) {
            stmt.setInt(1, days);
            stmt.setInt(2, id);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // 已通知但超过取书期限的预约置为过期，返回过期列表
    public List<Reserve> expireOverdue() {
        List<Reserve> list = query(BASE + " WHERE r.status=1 AND r.deadline<NOW()");
        for (Reserve r : list) {
            updateStatus(r.id, 3);
        }
        return list;
    }

    public int countActiveByBook(Integer bookId) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT COUNT(*) FROM tb_reserve WHERE book_id=? AND status IN (0,1)")) {
            stmt.setInt(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}
