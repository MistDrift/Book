package com.example.bookstore.service;

import com.example.bookstore.dao.BookDAO;
import com.example.bookstore.dao.BorrowDAO;
import com.example.bookstore.dao.FineDAO;
import com.example.bookstore.dao.NoticeDAO;
import com.example.bookstore.dao.ReserveDAO;
import com.example.bookstore.dao.TierDAO;
import com.example.bookstore.dao.UserDAO;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Borrow;
import com.example.bookstore.entity.Fine;
import com.example.bookstore.entity.Notice;
import com.example.bookstore.entity.Reserve;
import com.example.bookstore.entity.Tier;
import com.example.bookstore.entity.User;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BorrowService {

    private final BorrowDAO borrowDAO = new BorrowDAO();
    private final BookDAO bookDAO = new BookDAO();
    private final TierDAO tierDAO = new TierDAO();
    private final UserDAO userDAO = new UserDAO();
    private final FineDAO fineDAO = new FineDAO();
    private final ReserveDAO reserveDAO = new ReserveDAO();
    private final NoticeDAO noticeDAO = new NoticeDAO();
    private final CreditService creditService = new CreditService();

    private Map<String, Object> fail(Map<String, Object> map, String msg) {
        map.put("success", false);
        map.put("message", msg);
        return map;
    }

    /**
     * 发起借阅：校验账号状态、信用分、未缴罚金、等级上限与库存
     */
    public Map<String, Object> borrow(Integer userId, Integer bookId) {
        Map<String, Object> result = new HashMap<>();
        User user = userDAO.findById(userId);
        if (user == null) return fail(result, "用户不存在");
        if (user.getStatus() == null || user.getStatus() != 1) return fail(result, "账号已被禁用，无法借阅");
        if (user.getCredit() != null && user.getCredit() < 60) return fail(result, "信用分不足(低于60)，无法借阅");
        if (fineDAO.sumUnpaid(userId).compareTo(BigDecimal.ZERO) > 0) {
            return fail(result, "您有未缴罚金，请先缴纳");
        }

        Tier tier = tierDAO.findById(user.getTierId() != null ? user.getTierId() : 1);
        if (tier == null) tier = tierDAO.findById(1);

        int current = borrowDAO.countBorrowingByUserId(userId);
        if (current >= tier.getMaxBorrow()) {
            return fail(result, String.format("借阅失败：您当前已借阅%d本，%s借书上限为%d本",
                    current, tier.getName(), tier.getMaxBorrow()));
        }

        Book book = bookDAO.findById(bookId);
        if (book == null) return fail(result, "图书不存在");
        if (book.getStock() == null || book.getStock() <= 0) return fail(result, "库存不足，可选择预约该书");

        Borrow borrow = new Borrow();
        borrow.setUserId(userId);
        borrow.setBookId(bookId);
        Date borrowDate = new Date(System.currentTimeMillis());
        borrow.setBorrowDate(borrowDate);
        borrow.setStatus(0);
        Calendar cal = Calendar.getInstance();
        cal.setTime(borrowDate);
        cal.add(Calendar.DAY_OF_MONTH, tier.getDays());
        borrow.setDueDate(new Date(cal.getTimeInMillis()));

        if (borrowDAO.insert(borrow) > 0) {
            bookDAO.updateStock(bookId, -1);
            result.put("success", true);
            result.put("message", "借阅成功");
            return result;
        }
        return fail(result, "借阅失败");
    }

    /** 兼容旧接口：布尔返回 */
    public boolean borrowBook(Integer userId, Integer bookId) {
        if (userId == null || bookId == null) return false;
        return Boolean.TRUE.equals(borrow(userId, bookId).get("success"));
    }

    public int getCurrentBorrowCount(Integer userId) {
        if (userId == null) return 0;
        return borrowDAO.countBorrowingByUserId(userId);
    }

    /** 供前端显示的借阅上限（用 tier.max_borrow） */
    public int getBorrowLimitByUserId(Integer userId) {
        if (userId == null) return 0;
        User user = userDAO.findById(userId);
        if (user == null) return 0;
        Tier tier = tierDAO.findById(user.getTierId() != null ? user.getTierId() : 1);
        if (tier == null) return 3;  // 兜底
        return tier.getMaxBorrow();
    }

    /** 归还图书：计算逾期罚金、调整信用、联动预约 */
    public Map<String, Object> returnBookDetail(Integer borrowId, Integer operatorUserId) {
        Map<String, Object> result = new HashMap<>();
        Borrow borrow = borrowDAO.findById(borrowId);
        if (borrow == null || borrow.getStatus() != 0) {
            return fail(result, "归还失败，该图书可能已经归还");
        }

        Date now = new Date(System.currentTimeMillis());
        borrow.setReturnDate(now);
        borrow.setStatus(1);

        int overdueDays = 0;
        BigDecimal fineAmount = BigDecimal.ZERO;

        User user = userDAO.findById(borrow.getUserId());
        Tier tier = null;
        if (user != null) {
            tier = tierDAO.findById(user.getTierId() != null ? user.getTierId() : 1);
        }
        if (tier == null) tier = tierDAO.findById(1);

        if (borrow.getDueDate() != null && now.after(borrow.getDueDate())) {
            long diff = now.getTime() - borrow.getDueDate().getTime();
            overdueDays = (int) Math.ceil(diff / (1000.0 * 60 * 60 * 24));
            borrow.setOverdue(overdueDays);
            BigDecimal rate = tier.getRate() != null ? tier.getRate() : new BigDecimal("0.05");
            fineAmount = rate.multiply(new BigDecimal(overdueDays)).setScale(2, BigDecimal.ROUND_HALF_UP);
            borrow.setFine(fineAmount);

            Fine fine = new Fine();
            fine.borrowId = borrowId;
            fine.userId = borrow.getUserId();
            fine.amount = fineAmount;
            fine.overdueDays = overdueDays;
            fine.rate = rate;
            fine.reason = "逾期归还";
            fineDAO.insert(fine);
            creditService.change(borrow.getUserId(), -5, "逾期归还", null, "BORROW", borrowId);
        } else {
            creditService.change(borrow.getUserId(), 2, "按时归还", null, "BORROW", borrowId);
        }

        if (borrowDAO.update(borrow) > 0) {
            // 若有人预约该书则通知队首预约人，库存不归还（直接转给预约人）
            Reserve holder = reserveDAO.findFirstHolder(borrow.getBookId());
            if (holder != null) {
                notifyFirstHolder(borrow.getBookId(), borrow.getBookTitle());
            } else {
                bookDAO.updateStock(borrow.getBookId(), 1);
            }

            result.put("success", true);
            result.put("overdueDays", overdueDays);
            result.put("fine", fineAmount);
            if (overdueDays > 0) {
                result.put("message", "归还成功，逾期" + overdueDays + "天，产生罚金" + fineAmount + "元");
            } else {
                result.put("message", "归还成功");
            }
            return result;
        }
        return fail(result, "归还失败");
    }

    /** 兼容旧接口：布尔返回 */
    public boolean returnBook(Integer borrowId) {
        if (borrowId == null) return false;
        return Boolean.TRUE.equals(returnBookDetail(borrowId, null).get("success"));
    }

    /** 续借：未逾期、无人预约、未超续借次数 */
    public Map<String, Object> renew(Integer borrowId, Integer userId) {
        Map<String, Object> result = new HashMap<>();
        Borrow borrow = borrowDAO.findById(borrowId);
        if (borrow == null || borrow.getStatus() != 0) return fail(result, "借阅记录不存在或已归还");

        Date today = new Date(System.currentTimeMillis());
        if (borrow.getDueDate() != null && borrow.getDueDate().before(today)) {
            return fail(result, "该书已逾期，不可续借");
        }
        if (reserveDAO.countActiveByBook(borrow.getBookId()) > 0) {
            return fail(result, "该书已被他人预约，不可续借");
        }

        User user = userDAO.findById(borrow.getUserId());
        Tier tier = user != null ? tierDAO.findById(user.getTierId() != null ? user.getTierId() : 1) : null;
        if (tier == null) tier = tierDAO.findById(1);

        int renewed = borrow.getRenewed() != null ? borrow.getRenewed() : 0;
        if (renewed >= tier.getRenewMax()) {
            return fail(result, "已达最大续借次数");
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(borrow.getDueDate() != null ? borrow.getDueDate() : today);
        cal.add(Calendar.DAY_OF_MONTH, tier.getRenewDays());
        Date newDue = new Date(cal.getTimeInMillis());

        if (borrowDAO.renew(borrowId, newDue) > 0) {
            result.put("success", true);
            result.put("message", "续借成功");
            result.put("dueDate", newDue);
            return result;
        }
        return fail(result, "续借失败");
    }

    // 通知该书预约队列第一位取书
    private void notifyFirstHolder(Integer bookId, String bookTitle) {
        Reserve holder = reserveDAO.findFirstHolder(bookId);
        if (holder == null) return;
        reserveDAO.notifyReady(holder.id, 3);
        Notice n = new Notice();
        n.userId = holder.userId;
        n.type = 3;
        n.title = "预约取书通知";
        n.content = "您预约的《" + bookTitle + "》已可借阅，请在3天内取书";
        n.refType = "RESERVE";
        n.refId = holder.id;
        noticeDAO.insert(n);
    }

    public List<Borrow> findAll() { return borrowDAO.findAll(); }
    public List<Borrow> findByUserId(Integer userId) { return borrowDAO.findByUserId(userId); }
    public List<Borrow> findByStatus(Integer status) { return borrowDAO.findByStatus(status); }
    public Borrow findById(Integer id) { return borrowDAO.findById(id); }
    public boolean delete(Integer id) { return borrowDAO.delete(id) > 0; }

    // V2：查询所有逾期中未归还的借阅
    public List<Borrow> findOverdue() {
        return borrowDAO.findOverdue();
    }

    // V2：查询即将到期的借阅
    public List<Borrow> findDueSoon(int days) {
        return borrowDAO.findDueSoon(days);
    }
}
