package com.example.bookstore.dao;

import com.example.bookstore.entity.CreditLog;
import com.example.bookstore.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CreditDAO {

    // 注意：before/after 为列名，after 加反引号避免与保留字冲突
    private CreditLog map(ResultSet rs) throws SQLException {
        CreditLog log = new CreditLog();
        log.id = rs.getInt("id");
        log.userId = rs.getInt("user_id");
        log.delta = rs.getInt("delta");
        log.beforeCredit = rs.getInt("before");
        log.afterCredit = rs.getInt("after");
        log.reason = rs.getString("reason");
        log.opBy = rs.getInt("op_by");
        if (rs.wasNull()) log.opBy = null;
        log.refType = rs.getString("ref_type");
        log.refId = rs.getInt("ref_id");
        if (rs.wasNull()) log.refId = null;
        log.createTime = rs.getTimestamp("create_time");
        try { log.username = rs.getString("username"); } catch (SQLException ignored) {}
        return log;
    }

    private List<CreditLog> query(String sql, Object... params) {
        List<CreditLog> list = new ArrayList<>();
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public int insert(CreditLog log) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO tb_credit (user_id,delta,`before`,`after`,reason,op_by,ref_type,ref_id) " +
                 "VALUES (?,?,?,?,?,?,?,?)")) {
            stmt.setInt(1, log.userId);
            stmt.setInt(2, log.delta);
            stmt.setInt(3, log.beforeCredit);
            stmt.setInt(4, log.afterCredit);
            stmt.setString(5, log.reason);
            stmt.setObject(6, log.opBy);
            stmt.setString(7, log.refType);
            stmt.setObject(8, log.refId);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public List<CreditLog> findByUser(Integer userId) {
        return query("SELECT c.*, u.username FROM tb_credit c JOIN tb_user u ON c.user_id=u.id " +
                     "WHERE c.user_id=? ORDER BY c.id DESC", userId);
    }

    public List<CreditLog> findAll() {
        return query("SELECT c.*, u.username FROM tb_credit c JOIN tb_user u ON c.user_id=u.id " +
                     "ORDER BY c.id DESC");
    }
}
