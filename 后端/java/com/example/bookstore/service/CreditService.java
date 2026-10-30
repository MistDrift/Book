package com.example.bookstore.service;

import com.example.bookstore.dao.CreditDAO;
import com.example.bookstore.dao.TierDAO;
import com.example.bookstore.dao.UserDAO;
import com.example.bookstore.entity.CreditLog;
import com.example.bookstore.entity.Tier;
import com.example.bookstore.entity.User;

import java.util.List;

public class CreditService {

    private final UserDAO userDAO = new UserDAO();
    private final CreditDAO creditDAO = new CreditDAO();
    private final TierDAO tierDAO = new TierDAO();

    // 调整信用分并自动匹配等级，返回新信用分；用户不存在返回 -1
    public int change(Integer userId, int delta, String reason, Integer opBy, String refType, Integer refId) {
        User user = userDAO.findById(userId);
        if (user == null) return -1;

        int before = user.getCredit() != null ? user.getCredit() : 100;
        userDAO.updateCredit(userId, delta);

        User after = userDAO.findById(userId);
        int newCredit = after.getCredit() != null ? after.getCredit() : before;

        CreditLog log = new CreditLog();
        log.userId = userId;
        log.delta = delta;
        log.beforeCredit = before;
        log.afterCredit = newCredit;
        log.reason = reason;
        log.opBy = opBy;
        log.refType = refType;
        log.refId = refId;
        creditDAO.insert(log);

        // 自动调整等级
        Tier tier = tierDAO.findByCredit(newCredit);
        if (tier != null && (after.getTierId() == null || !tier.getId().equals(after.getTierId()))) {
            userDAO.updateTier(userId, tier.getId());
        }
        return newCredit;
    }

    public int adjust(Integer userId, int delta, String reason, Integer opBy) {
        return change(userId, delta, reason, opBy, "ADMIN", null);
    }

    public List<CreditLog> history(Integer userId) {
        return creditDAO.findByUser(userId);
    }

    public List<CreditLog> all() {
        return creditDAO.findAll();
    }
}
