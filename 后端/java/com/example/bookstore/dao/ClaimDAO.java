package com.example.bookstore.dao;

import com.example.bookstore.entity.Claim;
import com.example.bookstore.util.JdbcUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClaimDAO {

    private static final String BASE =
        "SELECT c.*, u.username, b.title AS book_title FROM tb_claim c " +
        "JOIN tb_user u ON c.user_id=u.id " +
        "JOIN tb_book b ON c.book_id=b.id";

    private Claim map(ResultSet rs) throws SQLException {
        Claim c = new Claim();
        c.id = rs.getInt("id");
        c.borrowId = rs.getInt("borrow_id");
        c.userId = rs.getInt("user_id");
        c.bookId = rs.getInt("book_id");
        c.reportDate = rs.getDate("report_date");
        c.type = rs.getInt("type");
        c.bookPrice = rs.getBigDecimal("book_price");
        c.depRate = rs.getBigDecimal("dep_rate");
        c.amount = rs.getBigDecimal("amount");
        c.paid = rs.getBigDecimal("paid");
        c.status = rs.getInt("status");
        c.handleBy = rs.getInt("handle_by");
        if (rs.wasNull()) c.handleBy = null;
        c.handleRemark = rs.getString("handle_remark");
        c.payTime = rs.getTimestamp("pay_time");
        c.createTime = rs.getTimestamp("create_time");
        c.username = rs.getString("username");
        c.bookTitle = rs.getString("book_title");
        return c;
    }

    private List<Claim> query(String sql, Object... params) {
        List<Claim> list = new ArrayList<>();
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Claim> findAll() {
        return query(BASE + " ORDER BY c.id DESC");
    }

    public List<Claim> findByUser(Integer userId) {
        return query(BASE + " WHERE c.user_id=? ORDER BY c.id DESC", userId);
    }

    public Claim findById(Integer id) {
        List<Claim> list = query(BASE + " WHERE c.id=?", id);
        return list.isEmpty() ? null : list.get(0);
    }

    public Claim findByBorrow(Integer borrowId) {
        List<Claim> list = query(BASE + " WHERE c.borrow_id=?", borrowId);
        return list.isEmpty() ? null : list.get(0);
    }

    public int insert(Claim c) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO tb_claim (borrow_id,user_id,book_id,report_date,status,type,book_price,dep_rate,amount,paid) " +
                 "VALUES (?,?,?,?,0,?,?,?,?,0)")) {
            stmt.setInt(1, c.borrowId);
            stmt.setInt(2, c.userId);
            stmt.setInt(3, c.bookId);
            stmt.setDate(4, c.reportDate);
            stmt.setInt(5, c.type);
            stmt.setBigDecimal(6, c.bookPrice);
            stmt.setBigDecimal(7, c.depRate);
            stmt.setBigDecimal(8, c.amount);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int handle(Integer id, int status, BigDecimal paid, Integer handlerId, String remark) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE tb_claim SET status=?, paid=?, handle_by=?, handle_remark=?, " +
                 "pay_time = IF(?=2, NOW(), pay_time) WHERE id=?")) {
            stmt.setInt(1, status);
            stmt.setBigDecimal(2, paid);
            stmt.setObject(3, handlerId);
            stmt.setString(4, remark);
            stmt.setInt(5, status);
            stmt.setInt(6, id);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}
