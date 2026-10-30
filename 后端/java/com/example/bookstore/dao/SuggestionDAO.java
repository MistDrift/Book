package com.example.bookstore.dao;

import com.example.bookstore.entity.Suggestion;
import com.example.bookstore.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SuggestionDAO {

    private static final String BASE =
        "SELECT s.*, u.username FROM tb_suggestion s JOIN tb_user u ON s.user_id=u.id";

    private Suggestion map(ResultSet rs) throws SQLException {
        Suggestion s = new Suggestion();
        s.id = rs.getInt("id");
        s.userId = rs.getInt("user_id");
        s.type = rs.getInt("type");
        s.title = rs.getString("title");
        s.author = rs.getString("author");
        s.publisher = rs.getString("publisher");
        s.isbn = rs.getString("isbn");
        s.category = rs.getString("category");
        s.price = rs.getBigDecimal("price");
        s.reason = rs.getString("reason");
        s.status = rs.getInt("status");
        s.votes = rs.getInt("votes");
        s.handleBy = rs.getInt("handle_by");
        if (rs.wasNull()) s.handleBy = null;
        s.handleRemark = rs.getString("handle_remark");
        s.handleTime = rs.getTimestamp("handle_time");
        s.createTime = rs.getTimestamp("create_time");
        s.username = rs.getString("username");
        return s;
    }

    private List<Suggestion> query(String sql, Object... params) {
        List<Suggestion> list = new ArrayList<>();
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Suggestion> findAll() {
        return query(BASE + " ORDER BY s.votes DESC, s.id DESC");
    }

    public List<Suggestion> findByUser(Integer userId) {
        return query(BASE + " WHERE s.user_id=? ORDER BY s.id DESC", userId);
    }

    public Suggestion findById(Integer id) {
        List<Suggestion> list = query(BASE + " WHERE s.id=?", id);
        return list.isEmpty() ? null : list.get(0);
    }

    public int insert(Suggestion s) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO tb_suggestion (user_id,type,title,author,publisher,isbn,category,price,reason,status) " +
                 "VALUES (?,?,?,?,?,?,?,?,?,0)")) {
            stmt.setInt(1, s.userId);
            stmt.setInt(2, s.type);
            stmt.setString(3, s.title);
            stmt.setString(4, s.author);
            stmt.setString(5, s.publisher);
            stmt.setString(6, s.isbn);
            stmt.setString(7, s.category);
            stmt.setBigDecimal(8, s.price);
            stmt.setString(9, s.reason);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int handle(Integer id, int status, Integer handlerId, String remark) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE tb_suggestion SET status=?, handle_by=?, handle_remark=?, handle_time=NOW() WHERE id=?")) {
            stmt.setInt(1, status);
            stmt.setObject(2, handlerId);
            stmt.setString(3, remark);
            stmt.setInt(4, id);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int vote(Integer id) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE tb_suggestion SET votes=votes+1 WHERE id=?")) {
            stmt.setInt(1, id);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public boolean hasLiked(Integer sugId, Integer userId) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT COUNT(*) FROM tb_suggestion_like WHERE sug_id=? AND user_id=?")) {
            stmt.setInt(1, sugId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public int insertLike(Integer sugId, Integer userId) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO tb_suggestion_like (sug_id, user_id) VALUES (?,?)")) {
            stmt.setInt(1, sugId);
            stmt.setInt(2, userId);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}
