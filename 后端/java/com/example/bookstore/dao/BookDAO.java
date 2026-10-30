package com.example.bookstore.dao;

import com.example.bookstore.entity.Book;
import com.example.bookstore.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    private Book mapBook(ResultSet rs) throws SQLException {
        Book b = new Book();
        b.setId(rs.getInt("id"));
        b.setIsbn(rs.getString("isbn"));
        b.setTitle(rs.getString("title"));
        b.setAuthor(rs.getString("author"));
        b.setPublisher(rs.getString("publisher"));
        b.setPublishDate(rs.getDate("publish_date"));
        b.setCategory(rs.getString("category"));
        b.setPrice(rs.getBigDecimal("price"));
        b.setStock(rs.getInt("stock"));
        b.setDescription(rs.getString("description"));
        b.setImage(rs.getString("image"));
        b.setCreateTime(rs.getTimestamp("create_time"));
        b.setUpdateTime(rs.getTimestamp("update_time"));
        // V2
        b.setLocation(rs.getString("location"));
        b.setCollStatus(rs.getInt("coll_status"));
        if (rs.wasNull()) b.setCollStatus(1);
        return b;
    }

    public List<Book> findAll() {
        return query("SELECT * FROM tb_book ORDER BY id");
    }

    public Book findById(Integer id) {
        List<Book> list = query("SELECT * FROM tb_book WHERE id = ?", id);
        return list.isEmpty() ? null : list.get(0);
    }

    public Book findByIsbn(String isbn) {
        List<Book> list = query("SELECT * FROM tb_book WHERE isbn = ?", isbn);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Book> search(String keyword) {
        String like = "%" + keyword + "%";
        return query("SELECT * FROM tb_book WHERE title LIKE ? OR author LIKE ? OR isbn LIKE ? OR category LIKE ?",
                like, like, like, like);
    }

    public List<Book> findByCategory(String category) {
        return query("SELECT * FROM tb_book WHERE category = ?", category);
    }

    public int insert(Book book) {
        String sql = "INSERT INTO tb_book (isbn,title,author,publisher,publish_date,category,price,stock,description,image,location,coll_status) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, book.getIsbn());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.setString(4, book.getPublisher());
            stmt.setDate(5, book.getPublishDate());
            stmt.setString(6, book.getCategory());
            stmt.setBigDecimal(7, book.getPrice());
            stmt.setInt(8, book.getStock());
            stmt.setString(9, book.getDescription());
            stmt.setString(10, book.getImage());
            stmt.setString(11, book.getLocation());
            stmt.setInt(12, book.getCollStatus() != null ? book.getCollStatus() : 1);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int update(Book book) {
        String sql = "UPDATE tb_book SET isbn=?,title=?,author=?,publisher=?,publish_date=?,category=?,price=?,stock=?,description=?,image=?,location=?,coll_status=? WHERE id=?";
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, book.getIsbn());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.setString(4, book.getPublisher());
            stmt.setDate(5, book.getPublishDate());
            stmt.setString(6, book.getCategory());
            stmt.setBigDecimal(7, book.getPrice());
            stmt.setInt(8, book.getStock());
            stmt.setString(9, book.getDescription());
            stmt.setString(10, book.getImage());
            stmt.setString(11, book.getLocation());
            stmt.setInt(12, book.getCollStatus() != null ? book.getCollStatus() : 1);
            stmt.setInt(13, book.getId());
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int delete(Integer id) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM tb_book WHERE id = ?")) {
            stmt.setInt(1, id);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int updateStock(Integer id, Integer delta) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE tb_book SET stock = stock + ? WHERE id = ?")) {
            stmt.setInt(1, delta);
            stmt.setInt(2, id);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // V2 更新馆藏状态（1在馆 2借出 3遗失 4维修）
    public int updateCollStatus(Integer bookId, int status) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE tb_book SET coll_status=? WHERE id=?")) {
            stmt.setInt(1, status);
            stmt.setInt(2, bookId);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public boolean existsByIsbn(String isbn) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM tb_book WHERE isbn = ?")) {
            stmt.setString(1, isbn);
            try (ResultSet rs = stmt.executeQuery()) { if (rs.next()) return rs.getInt(1) > 0; }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private List<Book> query(String sql, Object... params) {
        List<Book> list = new ArrayList<>();
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapBook(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}
