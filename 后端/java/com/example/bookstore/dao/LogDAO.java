package com.example.bookstore.dao;

import com.example.bookstore.entity.Log;
import com.example.bookstore.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LogDAO {

    private Log map(ResultSet rs) throws SQLException {
        Log l = new Log();
        l.setId(rs.getLong("id"));
        l.setUserId(rs.getInt("user_id"));
        if (rs.wasNull()) l.setUserId(null);
        l.setUsername(rs.getString("username"));
        l.setAction(rs.getString("action"));
        l.setTarget(rs.getString("target"));
        l.setTargetId(rs.getInt("target_id"));
        if (rs.wasNull()) l.setTargetId(null);
        l.setDetail(rs.getString("detail"));
        l.setIp(rs.getString("ip"));
        l.setUa(rs.getString("ua"));
        l.setOk(rs.getInt("ok"));
        l.setCreateTime(rs.getTimestamp("create_time"));
        return l;
    }

    public int insert(Log log) {
        String sql = "INSERT INTO tb_log (user_id, username, action, target, target_id, detail, ip, ua, ok) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (log.getUserId() != null) stmt.setInt(1, log.getUserId()); else stmt.setNull(1, java.sql.Types.INTEGER);
            stmt.setString(2, log.getUsername());
            stmt.setString(3, log.getAction());
            stmt.setString(4, log.getTarget());
            if (log.getTargetId() != null) stmt.setInt(5, log.getTargetId()); else stmt.setNull(5, java.sql.Types.INTEGER);
            stmt.setString(6, log.getDetail());
            stmt.setString(7, log.getIp());
            stmt.setString(8, log.getUa());
            stmt.setInt(9, log.getOk() != null ? log.getOk() : 1);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // 简单分页查询
    public List<Log> findAll(String action, int page, int size) {
        StringBuilder sql = new StringBuilder("SELECT * FROM tb_log WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (action != null && !action.isEmpty()) {
            sql.append(" AND action = ?");
            params.add(action);
        }
        sql.append(" ORDER BY id DESC LIMIT ? OFFSET ?");

        List<Log> list = new ArrayList<>();
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int i = 1;
            for (Object p : params) stmt.setObject(i++, p);
            stmt.setInt(i++, size);
            stmt.setInt(i, (page - 1) * size);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public int count(String action) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM tb_log WHERE 1=1");
        if (action != null && !action.isEmpty()) sql.append(" AND action = ?");
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            if (action != null && !action.isEmpty()) stmt.setString(1, action);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}
