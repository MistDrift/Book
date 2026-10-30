package com.example.bookstore.service;

import com.example.bookstore.dao.BookDAO;
import com.example.bookstore.dao.BorrowDAO;
import com.example.bookstore.dao.ClaimDAO;
import com.example.bookstore.dao.NoticeDAO;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Borrow;
import com.example.bookstore.entity.Claim;
import com.example.bookstore.entity.Notice;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClaimService {

    private final ClaimDAO claimDAO = new ClaimDAO();
    private final BorrowDAO borrowDAO = new BorrowDAO();
    private final BookDAO bookDAO = new BookDAO();
    private final NoticeDAO noticeDAO = new NoticeDAO();
    private final CreditService creditService = new CreditService();

    private Map<String, Object> fail(Map<String, Object> map, String msg) {
        map.put("success", false);
        map.put("message", msg);
        return map;
    }

    // 报失：type 1原价 2折旧 3原书购买
    public Map<String, Object> reportLost(Integer borrowId, Integer userId, Integer type) {
        Map<String, Object> result = new HashMap<>();
        Borrow borrow = borrowDAO.findById(borrowId);
        if (borrow == null || borrow.getStatus() != 0) return fail(result, "借阅记录不存在或已归还");
        if (claimDAO.findByBorrow(borrowId) != null) return fail(result, "该借阅已报失");
        if (type == null || type < 1 || type > 3) return fail(result, "无效的理赔类型");

        Book book = bookDAO.findById(borrow.getBookId());
        if (book == null) return fail(result, "图书不存在");

        BigDecimal depRate = type == 2 ? new BigDecimal("0.7")
                           : type == 3 ? BigDecimal.ZERO : BigDecimal.ONE;
        BigDecimal bookPrice = book.getPrice() != null ? book.getPrice() : BigDecimal.ZERO;
        BigDecimal amount = bookPrice.multiply(depRate).setScale(2, BigDecimal.ROUND_HALF_UP);

        Claim c = new Claim();
        c.borrowId = borrowId;
        c.userId = userId;
        c.bookId = borrow.getBookId();
        c.reportDate = new Date(System.currentTimeMillis());
        c.type = type;
        c.bookPrice = bookPrice;
        c.depRate = depRate;
        c.amount = amount;
        claimDAO.insert(c);

        borrowDAO.updateStatus(borrowId, 2);
        creditService.change(userId, -10, "图书遗失报失", null, "CLAIM", borrowId);

        result.put("success", true);
        result.put("message", "报失成功，等待管理员处理");
        result.put("amount", amount);
        return result;
    }

    public Map<String, Object> handle(Integer claimId, int status, BigDecimal paid, String remark, Integer handlerId) {
        Map<String, Object> result = new HashMap<>();
        Claim claim = claimDAO.findById(claimId);
        if (claim == null) return fail(result, "理赔单不存在");
        if (claim.status != 0 && claim.status != 1) return fail(result, "该理赔单已处理完毕");
        if (status != 2 && status != 3) return fail(result, "无效的处理结果");

        if (status == 2 && claim.type != 3) {
            if (paid == null || paid.compareTo(claim.amount) < 0) {
                return fail(result, "赔付金额不足");
            }
        }
        claimDAO.handle(claimId, status, paid, handlerId, remark);

        Notice n = new Notice();
        n.userId = claim.userId;
        n.type = 4;
        n.refType = "CLAIM";
        n.refId = claimId;
        if (status == 2) {
            bookDAO.updateCollStatus(claim.bookId, 3);
            n.title = "理赔已结案";
            n.content = "您报失的《" + claim.bookTitle + "》理赔已结案";
        } else {
            borrowDAO.updateStatus(claim.borrowId, 0);
            creditService.change(claim.userId, 10, "理赔驳回返还信用", handlerId, "CLAIM", claimId);
            n.title = "理赔被驳回";
            n.content = "您报失的《" + claim.bookTitle + "》理赔被驳回：" + (remark != null ? remark : "");
        }
        noticeDAO.insert(n);

        result.put("success", true);
        result.put("message", status == 2 ? "已结案" : "已驳回");
        return result;
    }

    public List<Claim> findAll() { return claimDAO.findAll(); }
    public List<Claim> findByUser(Integer userId) { return claimDAO.findByUser(userId); }
    public Claim findById(Integer id) { return claimDAO.findById(id); }
}
