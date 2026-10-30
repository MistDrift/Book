package com.example.bookstore.dao;

import com.example.bookstore.entity.Tier;
import com.example.bookstore.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TierDAO {

    private Tier map(ResultSet rs) throws SQLException {
        Tier t = new Tier();
        t.setId(rs.getInt("id"));
        t.setName(rs.getString("name"));
        t.setMinScore(rs.getInt("min_score"));
        t.setMaxBorrow(rs.getInt("max_borrow"));
        t.setDays(rs.getInt("days"));
        t.setRenewMax(rs.getInt("renew_max"));
        t.setRenewDays(rs.getInt("renew_days"));
        t.setPriority(rs.getInt("priority"));
        t.setRate(rs.getBigDecimal("rate"));
        t.setNote(rs.getString("note"));
        t.setCreateTime(rs.getTimestamp("create_time"));
        return t;
    }

    public List<Tier> findAll() {
        List<Tier> list = new ArrayList<>();
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM tb_tier ORDER BY min_score DESC");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Tier findById(Integer id) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM tb_tier WHERE id = ?")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // 根据信用分找到匹配等级（min_score <= credit 的最高等级）
    public Tier findByCredit(int credit) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM tb_tier WHERE min_score <= ? ORDER BY min_score DESC LIMIT 1")) {
            stmt.setInt(1, credit);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return findById(1);  // 默认普通读者
    }

    public int insert(Tier t) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO tb_tier (name,min_score,max_borrow,days,renew_max,renew_days,priority,rate,note) " +
                 "VALUES (?,?,?,?,?,?,?,?,?)")) {
            stmt.setString(1, t.getName());
            stmt.setInt(2, t.getMinScore());
            stmt.setInt(3, t.getMaxBorrow());
            stmt.setInt(4, t.getDays());
            stmt.setInt(5, t.getRenewMax());
            stmt.setInt(6, t.getRenewDays());
            stmt.setInt(7, t.getPriority());
            stmt.setBigDecimal(8, t.getRate());
            stmt.setString(9, t.getNote());
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int update(Tier t) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE tb_tier SET name=?,min_score=?,max_borrow=?,days=?,renew_max=?,renew_days=?,priority=?,rate=?,note=? WHERE id=?")) {
            stmt.setString(1, t.getName());
            stmt.setInt(2, t.getMinScore());
            stmt.setInt(3, t.getMaxBorrow());
            stmt.setInt(4, t.getDays());
            stmt.setInt(5, t.getRenewMax());
            stmt.setInt(6, t.getRenewDays());
            stmt.setInt(7, t.getPriority());
            stmt.setBigDecimal(8, t.getRate());
            stmt.setString(9, t.getNote());
            stmt.setInt(10, t.getId());
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int delete(Integer id) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM tb_tier WHERE id = ?")) {
            stmt.setInt(1, id);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}
