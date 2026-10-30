package com.example.bookstore.dao;

import com.example.bookstore.entity.Notice;
import com.example.bookstore.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NoticeDAO {

    private Notice map(ResultSet rs) throws SQLException {
        Notice n = new Notice();
        n.id = rs.getInt("id");
        n.userId = rs.getInt("user_id");
        n.type = rs.getInt("type");
        n.title = rs.getString("title");
        n.content = rs.getString("content");
        n.refType = rs.getString("ref_type");
        n.refId = rs.getInt("ref_id");
        if (rs.wasNull()) n.refId = null;
        n.readFlag = rs.getInt("read_flag");
        n.createTime = rs.getTimestamp("create_time");
        return n;
    }

    private List<Notice> query(String sql, Object... params) {
        List<Notice> list = new ArrayList<>();
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Notice> findByUser(Integer userId, boolean unreadOnly) {
        String sql = "SELECT * FROM tb_notice WHERE user_id=?";
        if (unreadOnly) sql += " AND read_flag=0";
        sql += " ORDER BY id DESC";
        return query(sql, userId);
    }

    public List<Notice> findAll() {
        return query("SELECT * FROM tb_notice ORDER BY id DESC");
    }

    public int unreadCount(Integer userId) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT COUNT(*) FROM tb_notice WHERE user_id=? AND read_flag=0")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int insert(Notice n) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO tb_notice (user_id,type,title,content,ref_type,ref_id,read_flag) " +
                 "VALUES (?,?,?,?,?,?,0)")) {
            stmt.setInt(1, n.userId);
            stmt.setInt(2, n.type);
            stmt.setString(3, n.title);
            stmt.setString(4, n.content);
            stmt.setString(5, n.refType);
            stmt.setObject(6, n.refId);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int markRead(Integer id, Integer userId) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE tb_notice SET read_flag=1 WHERE id=? AND user_id=?")) {
            stmt.setInt(1, id);
            stmt.setInt(2, userId);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int markAllRead(Integer userId) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE tb_notice SET read_flag=1 WHERE user_id=?")) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // 是否已存在同类型同引用的未读通知（避免重复提醒）
    public boolean exists(Integer userId, int type, String refType, Integer refId) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT COUNT(*) FROM tb_notice WHERE user_id=? AND type=? AND ref_type=? AND ref_id=? AND read_flag=0")) {
            stmt.setInt(1, userId);
            stmt.setInt(2, type);
            stmt.setString(3, refType);
            stmt.setObject(4, refId);
            try (ResultSet rs = stmt.executeQuery()) { if (rs.next()) return rs.getInt(1) > 0; }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
}
