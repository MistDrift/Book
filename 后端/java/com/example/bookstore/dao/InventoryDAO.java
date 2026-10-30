package com.example.bookstore.dao;

import com.example.bookstore.entity.InvItem;
import com.example.bookstore.entity.Inventory;
import com.example.bookstore.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {

    private Inventory map(ResultSet rs) throws SQLException {
        Inventory inv = new Inventory();
        inv.id = rs.getInt("id");
        inv.name = rs.getString("name");
        inv.type = rs.getInt("type");
        inv.area = rs.getString("area");
        inv.total = rs.getInt("total");
        inv.actual = rs.getInt("actual");
        inv.missing = rs.getInt("missing");
        inv.extra = rs.getInt("extra");
        inv.status = rs.getInt("status");
        inv.startTime = rs.getTimestamp("start_time");
        inv.endTime = rs.getTimestamp("end_time");
        inv.creator = rs.getInt("creator");
        inv.createTime = rs.getTimestamp("create_time");
        inv.creatorName = rs.getString("creator_name");
        return inv;
    }

    private InvItem mapItem(ResultSet rs) throws SQLException {
        InvItem item = new InvItem();
        item.id = rs.getInt("id");
        item.invId = rs.getInt("inv_id");
        item.bookId = rs.getInt("book_id");
        item.isbn = rs.getString("isbn");
        item.title = rs.getString("title");
        item.sysStatus = rs.getInt("sys_status");
        if (rs.wasNull()) item.sysStatus = null;
        item.realStatus = rs.getInt("real_status");
        if (rs.wasNull()) item.realStatus = null;
        item.remark = rs.getString("remark");
        item.checkedBy = rs.getInt("checked_by");
        if (rs.wasNull()) item.checkedBy = null;
        item.checkedTime = rs.getTimestamp("checked_time");
        return item;
    }

    public List<Inventory> findAll() {
        List<Inventory> list = new ArrayList<>();
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT i.*, u.name AS creator_name FROM tb_inventory i " +
                 "LEFT JOIN tb_user u ON i.creator=u.id ORDER BY i.id DESC");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Inventory findById(Integer id) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT i.*, u.name AS creator_name FROM tb_inventory i " +
                 "LEFT JOIN tb_user u ON i.creator=u.id WHERE i.id=?")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public int insert(Inventory inv) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO tb_inventory (name,type,area,total,status,creator) VALUES (?,?,?,0,0,?)",
                 Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, inv.name);
            stmt.setInt(2, inv.type);
            stmt.setString(3, inv.area);
            stmt.setInt(4, inv.creator);
            int rows = stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) inv.id = keys.getInt(1);
            }
            return rows;
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // 按馆藏位置生成盘点明细（area 为空则全盘）
    public int generateItems(Integer invId, String area) {
        String sql = "INSERT INTO tb_inv_item (inv_id, book_id, isbn, title, sys_status) " +
                     "SELECT ?, id, isbn, title, coll_status FROM tb_book";
        if (area != null && !area.trim().isEmpty()) {
            sql += " WHERE location = ?";
        }
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, invId);
            if (area != null && !area.trim().isEmpty()) {
                stmt.setString(2, area);
            }
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int refreshTotal(Integer invId) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE tb_inventory SET total=(SELECT COUNT(*) FROM tb_inv_item WHERE inv_id=?) WHERE id=?")) {
            stmt.setInt(1, invId);
            stmt.setInt(2, invId);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int start(Integer invId) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE tb_inventory SET status=1, start_time=NOW() WHERE id=?")) {
            stmt.setInt(1, invId);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int updateItem(Integer invId, Integer bookId, int realStatus, String remark, Integer checkerId) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE tb_inv_item SET real_status=?, remark=?, checked_by=?, checked_time=NOW() " +
                 "WHERE inv_id=? AND book_id=?")) {
            stmt.setInt(1, realStatus);
            stmt.setString(2, remark);
            stmt.setObject(3, checkerId);
            stmt.setInt(4, invId);
            stmt.setInt(5, bookId);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int finish(Integer invId) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE tb_inventory SET " +
                 "actual=(SELECT COUNT(*) FROM tb_inv_item WHERE inv_id=? AND real_status IN (1,3,4)), " +
                 "missing=(SELECT COUNT(*) FROM tb_inv_item WHERE inv_id=? AND real_status=2), " +
                 "extra=(SELECT COUNT(*) FROM tb_inv_item WHERE inv_id=? AND real_status=4), " +
                 "status=2, end_time=NOW() WHERE id=?")) {
            stmt.setInt(1, invId);
            stmt.setInt(2, invId);
            stmt.setInt(3, invId);
            stmt.setInt(4, invId);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // 盘点结果回写图书馆藏状态：1在架→1在馆，2遗失→3遗失，3破损→4维修
    public int applyResults(Integer invId) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE tb_book b JOIN tb_inv_item i ON b.id=i.book_id " +
                 "SET b.coll_status = CASE i.real_status WHEN 1 THEN 1 WHEN 2 THEN 3 WHEN 3 THEN 4 ELSE b.coll_status END " +
                 "WHERE i.inv_id=? AND i.real_status IS NOT NULL")) {
            stmt.setInt(1, invId);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int cancel(Integer invId) {
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE tb_inventory SET status=3 WHERE id=?")) {
            stmt.setInt(1, invId);
            return stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public List<InvItem> findItems(Integer invId) {
        List<InvItem> list = new ArrayList<>();
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM tb_inv_item WHERE inv_id=? ORDER BY id")) {
            stmt.setInt(1, invId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapItem(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}
