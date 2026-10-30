package com.example.bookstore.dao;

import com.example.bookstore.entity.Perm;
import com.example.bookstore.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PermDAO {

    private Perm map(ResultSet rs) throws SQLException {
        Perm p = new Perm();
        p.setId(rs.getInt("id"));
        p.setCode(rs.getString("code"));
        p.setName(rs.getString("name"));
        p.setGrp(rs.getString("grp"));
        p.setPos(rs.getInt("pos"));
        p.setNote(rs.getString("note"));
        p.setCreateTime(rs.getTimestamp("create_time"));
        return p;
    }

    public List<Perm> findAll() {
        List<Perm> list = new ArrayList<>();
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM tb_perm ORDER BY pos");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // 根据 group 分组返回（方便前端按组展示）
    public List<Perm> findByGroup(String grp) {
        List<Perm> list = new ArrayList<>();
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM tb_perm WHERE grp = ? ORDER BY pos")) {
            stmt.setString(1, grp);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // 根据 code 返回位置
    public Integer findPosByCode(String code) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT pos FROM tb_perm WHERE code = ?")) {
            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("pos");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}
