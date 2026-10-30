package com.example.bookstore.dao;

import com.example.bookstore.entity.Borrow;
import com.example.bookstore.util.JdbcUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BorrowDAO {

    private Borrow mapBorrow(ResultSet rs) throws SQLException {
        Borrow b = new Borrow();
        b.setId(rs.getInt("id"));
        b.setUserId(rs.getInt("user_id"));
        b.setBookId(rs.getInt("book_id"));
        b.setBorrowDate(rs.getDate("borrow_date"));
        b.setReturnDate(rs.getDate("return_date"));
        b.setStatus(rs.getInt("status"));
        b.setCreateTime(rs.getTimestamp("create_time"));
        b.setUpdateTime(rs.getTimestamp("update_time"));
        // V2 新字段
        b.setDueDate(rs.getDate("due_date"));
        b.setFine(rs.getBigDecimal("fine"));
        if (rs.wasNull()) b.setFine(BigDecimal.ZERO);
        b.setPaid(rs.getInt("paid"));
        if (rs.wasNull()) b.setPaid(0);
        b.setOverdue(rs.getInt("overdue"));
        if (rs.wasNull()) b.setOverdue(0);
        b.setRenewed(rs.getInt("renewed"));
        if (rs.wasNull()) b.setRenewed(0);
        // JOIN 字段
        b.setUsername(rs.getString("username"));
        b.setBookTitle(rs.getString("book_title"));
        b.setBookImage(rs.getString("book_image"));
        b.setBookAuthor(rs.getString("book_author"));
        b.setBookCategory(rs.getString("book_category"));
        return b;
    }

    public int insert(Borrow borrow) {
        String sql = "INSERT INTO tb_borrow (user_id, book_id, borrow_date, due_date, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, borrow.getUserId());
            stmt.setInt(2, borrow.getBookId());
            stmt.setDate(3, borrow.getBorrowDate());
            stmt.setDate(4, borrow.getDueDate());
            stmt.setInt(5, borrow.getStatus());
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int update(Borrow borrow) {
        String sql = "UPDATE tb_borrow SET return_date=?, status=?, due_date=?, fine=?, paid=?, overdue=?, renewed=? WHERE id=?";
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, borrow.getReturnDate());
            stmt.setInt(2, borrow.getStatus());
            stmt.setDate(3, borrow.getDueDate());
            stmt.setBigDecimal(4, borrow.getFine());
            stmt.setInt(5, borrow.getPaid() != null ? borrow.getPaid() : 0);
            stmt.setInt(6, borrow.getOverdue() != null ? borrow.getOverdue() : 0);
            stmt.setInt(7, borrow.getRenewed() != null ? borrow.getRenewed() : 0);
            stmt.setInt(8, borrow.getId());
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int delete(Integer id) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM tb_borrow WHERE id = ?")) {
            stmt.setInt(1, id);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int deleteByBookId(Integer bookId) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM tb_borrow WHERE book_id = ?")) {
            stmt.setInt(1, bookId);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public List<Borrow> findAll() {
        String sql = "SELECT b.*, u.username, bo.title AS book_title, bo.image AS book_image, " +
                     "bo.author AS book_author, bo.category AS book_category FROM tb_borrow b " +
                     "JOIN tb_user u ON b.user_id = u.id " +
                     "JOIN tb_book bo ON b.book_id = bo.id ORDER BY b.create_time DESC";
        return query(sql);
    }

    public List<Borrow> findByUserId(Integer userId) {
        return query("SELECT b.*, u.username, bo.title AS book_title, bo.image AS book_image, " +
                     "bo.author AS book_author, bo.category AS book_category FROM tb_borrow b " +
                     "JOIN tb_user u ON b.user_id = u.id " +
                     "JOIN tb_book bo ON b.book_id = bo.id WHERE b.user_id = ? ORDER BY b.create_time DESC", userId);
    }

    public List<Borrow> findByStatus(Integer status) {
        return query("SELECT b.*, u.username, bo.title AS book_title, bo.image AS book_image, " +
                     "bo.author AS book_author, bo.category AS book_category FROM tb_borrow b " +
                     "JOIN tb_user u ON b.user_id = u.id " +
                     "JOIN tb_book bo ON b.book_id = bo.id WHERE b.status = ? ORDER BY b.create_time DESC", status);
    }

    public Borrow findById(Integer id) {
        List<Borrow> list = query("SELECT b.*, u.username, bo.title AS book_title, bo.image AS book_image, " +
                     "bo.author AS book_author, bo.category AS book_category FROM tb_borrow b " +
                                  "JOIN tb_user u ON b.user_id = u.id " +
                                  "JOIN tb_book bo ON b.book_id = bo.id WHERE b.id = ?", id);
        return list.isEmpty() ? null : list.get(0);
    }

    public int countBorrowingByBookId(Integer bookId) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM tb_borrow WHERE book_id=? AND status=0")) {
            stmt.setInt(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int countBorrowingByUserId(Integer userId) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM tb_borrow WHERE user_id=? AND status=0")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // V2: 查询应还日期已过且未归还的借阅
    public List<Borrow> findOverdue() {
        return query("SELECT b.*, u.username, bo.title AS book_title, bo.image AS book_image, " +
                     "bo.author AS book_author, bo.category AS book_category FROM tb_borrow b " +
                     "JOIN tb_user u ON b.user_id = u.id " +
                     "JOIN tb_book bo ON b.book_id = bo.id " +
                     "WHERE b.status = 0 AND b.due_date < CURDATE() ORDER BY b.due_date ASC");
    }

    // V2: 查询某用户对某书正在借阅中的记录
    public Borrow findActiveByUserAndBook(Integer userId, Integer bookId) {
        List<Borrow> list = query("SELECT b.*, u.username, bo.title AS book_title, bo.image AS book_image, " +
                     "bo.author AS book_author, bo.category AS book_category FROM tb_borrow b " +
                "JOIN tb_user u ON b.user_id = u.id " +
                "JOIN tb_book bo ON b.book_id = bo.id " +
                "WHERE b.status=0 AND b.user_id=? AND b.book_id=? LIMIT 1", userId, bookId);
        return list.isEmpty() ? null : list.get(0);
    }

    // V2: 即将到期（days 天内）且未归还的借阅
    public List<Borrow> findDueSoon(int days) {
        return query("SELECT b.*, u.username, bo.title AS book_title, bo.image AS book_image, " +
                     "bo.author AS book_author, bo.category AS book_category FROM tb_borrow b " +
                     "JOIN tb_user u ON b.user_id = u.id " +
                     "JOIN tb_book bo ON b.book_id = bo.id " +
                     "WHERE b.status = 0 AND b.due_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL ? DAY) " +
                     "ORDER BY b.due_date ASC", days);
    }

    // V2: 全部借阅中记录
    public List<Borrow> findActive() {
        return query("SELECT b.*, u.username, bo.title AS book_title, bo.image AS book_image, " +
                     "bo.author AS book_author, bo.category AS book_category FROM tb_borrow b " +
                     "JOIN tb_user u ON b.user_id = u.id " +
                     "JOIN tb_book bo ON b.book_id = bo.id " +
                     "WHERE b.status = 0 ORDER BY b.create_time DESC");
    }

    public int updateStatus(Integer id, int status) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE tb_borrow SET status=? WHERE id=?")) {
            stmt.setInt(1, status);
            stmt.setInt(2, id);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // V2: 续借：延长应还日期并累计续借次数
    public int renew(Integer id, java.sql.Date newDueDate) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE tb_borrow SET due_date=?, renewed=renewed+1 WHERE id=?")) {
            stmt.setDate(1, newDueDate);
            stmt.setInt(2, id);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // V2: 标记罚金已缴清
    public int markFinePaid(Integer id) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE tb_borrow SET paid=1 WHERE id=?")) {
            stmt.setInt(1, id);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private List<Borrow> query(String sql, Object... params) {
        List<Borrow> list = new ArrayList<>();
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapBorrow(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}
