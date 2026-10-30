package com.example.bookstore.service;

import com.example.bookstore.dao.BorrowDAO;
import com.example.bookstore.dao.NoticeDAO;
import com.example.bookstore.entity.Borrow;
import com.example.bookstore.entity.Notice;

import java.util.List;

public class NoticeService {

    private final NoticeDAO noticeDAO = new NoticeDAO();
    private final BorrowDAO borrowDAO = new BorrowDAO();
    private final ReserveService reserveService = new ReserveService();

    public List<Notice> list(Integer userId, boolean unreadOnly) {
        return noticeDAO.findByUser(userId, unreadOnly);
    }

    public int unreadCount(Integer userId) {
        return noticeDAO.unreadCount(userId);
    }

    public boolean markRead(Integer id, Integer userId) {
        return noticeDAO.markRead(id, userId) > 0;
    }

    public boolean markAllRead(Integer userId) {
        return noticeDAO.markAllRead(userId) >= 0;
    }

    public void create(Integer userId, int type, String title, String content, String refType, Integer refId) {
        Notice n = new Notice();
        n.userId = userId;
        n.type = type;
        n.title = title;
        n.content = content;
        n.refType = refType;
        n.refId = refId;
        noticeDAO.insert(n);
    }

    // 扫描到期/逾期/预约过期并生成通知，返回创建的通知数
    public int scanAndNotify() {
        int created = 0;

        for (Borrow b : borrowDAO.findDueSoon(3)) {
            if (!noticeDAO.exists(b.getUserId(), 1, "BORROW", b.getId())) {
                create(b.getUserId(), 1, "到期提醒",
                        "您借阅的《" + b.getBookTitle() + "》将于 " + b.getDueDate() + " 到期，请及时归还",
                        "BORROW", b.getId());
                created++;
            }
        }

        for (Borrow b : borrowDAO.findOverdue()) {
            if (!noticeDAO.exists(b.getUserId(), 2, "BORROW", b.getId())) {
                create(b.getUserId(), 2, "逾期提醒",
                        "您借阅的《" + b.getBookTitle() + "》已逾期，请尽快归还，逾期将产生罚金",
                        "BORROW", b.getId());
                created++;
            }
        }

        created += reserveService.expireOverdue();
        return created;
    }
}
