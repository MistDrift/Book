package com.example.bookstore.service;

import com.example.bookstore.dao.BookDAO;
import com.example.bookstore.dao.BorrowDAO;
import com.example.bookstore.dao.NoticeDAO;
import com.example.bookstore.dao.ReserveDAO;
import com.example.bookstore.dao.TierDAO;
import com.example.bookstore.dao.UserDAO;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Borrow;
import com.example.bookstore.entity.Notice;
import com.example.bookstore.entity.Reserve;
import com.example.bookstore.entity.Tier;
import com.example.bookstore.entity.User;

import java.sql.Date;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReserveService {

    private final ReserveDAO reserveDAO = new ReserveDAO();
    private final BookDAO bookDAO = new BookDAO();
    private final BorrowDAO borrowDAO = new BorrowDAO();
    private final NoticeDAO noticeDAO = new NoticeDAO();
    private final TierDAO tierDAO = new TierDAO();
    private final UserDAO userDAO = new UserDAO();

    private Map<String, Object> fail(Map<String, Object> map, String msg) {
        map.put("success", false);
        map.put("message", msg);
        return map;
    }

    public Map<String, Object> reserve(Integer userId, Integer bookId) {
        Map<String, Object> result = new HashMap<>();
        Book book = bookDAO.findById(bookId);
        if (book == null) return fail(result, "图书不存在");
        if (book.getStock() != null && book.getStock() > 0) {
            return fail(result, "该书当前有库存，可直接借阅，无需预约");
        }
        if (borrowDAO.findActiveByUserAndBook(userId, bookId) != null) {
            return fail(result, "您正在借阅该书");
        }
        if (reserveDAO.existsActive(userId, bookId)) {
            return fail(result, "您已预约过该书");
        }
        Reserve r = new Reserve();
        r.userId = userId;
        r.bookId = bookId;
        reserveDAO.insert(r);
        result.put("success", true);
        result.put("message", "预约成功");
        return result;
    }

    public Map<String, Object> cancel(Integer id, Integer userId, boolean isAdmin) {
        Map<String, Object> result = new HashMap<>();
        Reserve r = reserveDAO.findById(id);
        if (r == null) return fail(result, "预约不存在");
        if (!isAdmin && !r.userId.equals(userId)) return fail(result, "无权限取消他人预约");
        if (r.status != 0 && r.status != 1) return fail(result, "该预约已不可取消");
        reserveDAO.updateStatus(id, 2);
        if (r.status == 1) {
            // 已通知的预约持有库存：转给下一位等待者，无人等待则回补库存
            if (!notifyNext(r.bookId)) {
                bookDAO.updateStock(r.bookId, 1);
            }
        }
        result.put("success", true);
        result.put("message", "已取消");
        return result;
    }

    // 已通知的预约取书，转为借阅
    public boolean pick(Integer id) {
        Reserve r = reserveDAO.findById(id);
        if (r == null || r.status != 1) return false;

        User user = userDAO.findById(r.userId);
        if (user == null) return false;
        Tier tier = tierDAO.findById(user.getTierId() != null ? user.getTierId() : 1);
        if (tier == null) tier = tierDAO.findById(1);

        Borrow borrow = new Borrow();
        borrow.setUserId(r.userId);
        borrow.setBookId(r.bookId);
        Date today = new Date(System.currentTimeMillis());
        borrow.setBorrowDate(today);
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        cal.add(Calendar.DAY_OF_MONTH, tier.getDays());
        borrow.setDueDate(new Date(cal.getTimeInMillis()));
        borrow.setStatus(0);

        if (borrowDAO.insert(borrow) > 0) {
            // 库存在通知环节已被该书预留（归还时未回补），此处不再扣减
            reserveDAO.updateStatus(id, 4);
            return true;
        }
        return false;
    }

    // 过期处理 + 通知下一位预约人，返回过期数量
    public int expireOverdue() {
        List<Reserve> expired = reserveDAO.expireOverdue();
        for (Reserve r : expired) {
            // 过期的预约持有库存：转给下一位等待者，无人等待则回补库存
            if (!notifyNext(r.bookId)) {
                bookDAO.updateStock(r.bookId, 1);
            }
        }
        return expired.size();
    }

    // 通知该书预约队列第一位取书（库存随之转给该预约人），返回是否通知成功
    private boolean notifyNext(Integer bookId) {
        Reserve holder = reserveDAO.findFirstHolder(bookId);
        if (holder == null) return false;
        reserveDAO.notifyReady(holder.id, 3);
        Notice n = new Notice();
        n.userId = holder.userId;
        n.type = 3;
        n.title = "预约取书通知";
        n.content = "您预约的《" + holder.bookTitle + "》已可借阅，请在3天内取书";
        n.refType = "RESERVE";
        n.refId = holder.id;
        noticeDAO.insert(n);
        return true;
    }

    public List<Reserve> findAll() { return reserveDAO.findAll(); }
    public List<Reserve> findByUser(Integer userId) { return reserveDAO.findByUser(userId); }
}
