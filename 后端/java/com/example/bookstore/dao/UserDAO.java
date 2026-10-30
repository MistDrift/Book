package com.example.bookstore.dao;

import com.example.bookstore.entity.User;
import com.example.bookstore.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // 通用：从 ResultSet 映射 User（V2 新字段）
    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setName(rs.getString("name"));
        u.setRole(rs.getInt("role"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        u.setCreateTime(rs.getTimestamp("create_time"));
        u.setUpdateTime(rs.getTimestamp("update_time"));
        // V2 新字段，DB 可能还没 ALTER 前用 0 兜底
        u.setTierId(rs.getInt("tier_id"));
        if (rs.wasNull()) u.setTierId(1);
        u.setCredit(rs.getInt("credit"));
        if (rs.wasNull()) u.setCredit(100);
        u.setPerms(rs.getLong("perms"));
        if (rs.wasNull()) u.setPerms(0L);
        u.setStatus(rs.getInt("status"));
        if (rs.wasNull()) u.setStatus(1);
        return u;
    }

    public User login(String username, String password) {
        String sql = "SELECT * FROM tb_user WHERE username = ? AND password = ?";
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapUser(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public User findById(Integer id) {
        String sql = "SELECT u.*, t.name AS tier_name FROM tb_user u " +
                     "LEFT JOIN tb_tier t ON u.tier_id = t.id WHERE u.id = ?";
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User u = mapUser(rs);
                    u.setTierName(rs.getString("tier_name"));
                    return u;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<User> findAll() {
        String sql = "SELECT u.*, t.name AS tier_name FROM tb_user u " +
                     "LEFT JOIN tb_tier t ON u.tier_id = t.id ORDER BY u.id";
        List<User> list = new ArrayList<>();
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                User u = mapUser(rs);
                u.setTierName(rs.getString("tier_name"));
                list.add(u);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public int insert(User user) {
        String sql = "INSERT INTO tb_user (username, password, name, role, email, phone, tier_id, credit, perms, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getName());
            stmt.setInt(4, user.getRole());
            stmt.setString(5, user.getEmail());
            stmt.setString(6, user.getPhone());
            stmt.setInt(7, user.getTierId() != null ? user.getTierId() : 1);
            stmt.setInt(8, user.getCredit() != null ? user.getCredit() : 100);
            stmt.setLong(9, user.getPerms() != null ? user.getPerms() : 0L);
            stmt.setInt(10, user.getStatus() != null ? user.getStatus() : 1);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int update(User user) {
        String sql = "UPDATE tb_user SET username=?, password=?, name=?, role=?, email=?, phone=?, " +
                     "tier_id=?, credit=?, perms=?, status=? WHERE id=?";
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getName());
            stmt.setInt(4, user.getRole());
            stmt.setString(5, user.getEmail());
            stmt.setString(6, user.getPhone());
            stmt.setInt(7, user.getTierId());
            stmt.setInt(8, user.getCredit());
            stmt.setLong(9, user.getPerms());
            stmt.setInt(10, user.getStatus());
            stmt.setInt(11, user.getId());
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int delete(Integer id) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM tb_user WHERE id = ?")) {
            stmt.setInt(1, id);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public boolean existsByUsername(String username) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM tb_user WHERE username = ?")) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // V2 信用分增减：返回影响行数
    public int updateCredit(int userId, int delta) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE tb_user SET credit = GREATEST(0, LEAST(100, credit + ?)) WHERE id = ?")) {
            stmt.setInt(1, delta);
            stmt.setInt(2, userId);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // V2 更新管理员权限位
    public int updatePerms(int id, long perms) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE tb_user SET perms=? WHERE id=?")) {
            stmt.setLong(1, perms);
            stmt.setInt(2, id);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // V2 启用/禁用账号
    public int updateStatus(int id, int status) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE tb_user SET status=? WHERE id=?")) {
            stmt.setInt(1, status);
            stmt.setInt(2, id);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // V2 更新用户等级
    public int updateTier(int id, int tierId) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE tb_user SET tier_id=? WHERE id=?")) {
            stmt.setInt(1, tierId);
            stmt.setInt(2, id);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}
