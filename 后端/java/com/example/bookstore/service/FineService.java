package com.example.bookstore.service;

import com.example.bookstore.dao.BorrowDAO;
import com.example.bookstore.dao.FineDAO;
import com.example.bookstore.entity.Fine;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FineService {

    private final FineDAO fineDAO = new FineDAO();
    private final BorrowDAO borrowDAO = new BorrowDAO();
    private final CreditService creditService = new CreditService();

    private Map<String, Object> fail(Map<String, Object> map, String msg) {
        map.put("success", false);
        map.put("message", msg);
        return map;
    }

    public Map<String, Object> pay(Integer fineId, Integer userId) {
        Map<String, Object> result = new HashMap<>();
        Fine fine = fineDAO.findById(fineId);
        if (fine == null) return fail(result, "罚金记录不存在");
        if (fine.payStatus != 0) return fail(result, "无需缴纳");
        fineDAO.pay(fineId);
        borrowDAO.markFinePaid(fine.borrowId);
        creditService.change(fine.userId, 2, "缴纳罚金", null, "FINE", fineId);
        result.put("success", true);
        result.put("message", "缴纳成功");
        return result;
    }

    public Map<String, Object> waive(Integer fineId, String reason, Integer adminId) {
        Map<String, Object> result = new HashMap<>();
        Fine fine = fineDAO.findById(fineId);
        if (fine == null) return fail(result, "罚金记录不存在");
        if (fine.payStatus != 0) return fail(result, "无需减免");
        if (reason == null || reason.trim().isEmpty()) return fail(result, "请填写减免原因");
        fineDAO.waive(fineId, reason, adminId);
        result.put("success", true);
        result.put("message", "已减免");
        return result;
    }

    public List<Fine> findAll() { return fineDAO.findAll(); }
    public List<Fine> findByUser(Integer userId) { return fineDAO.findByUser(userId); }
    public List<Fine> findByStatus(Integer payStatus) { return fineDAO.findByPayStatus(payStatus); }
    public Fine findById(Integer id) { return fineDAO.findById(id); }
    public BigDecimal sumUnpaid(Integer userId) { return fineDAO.sumUnpaid(userId); }
}
