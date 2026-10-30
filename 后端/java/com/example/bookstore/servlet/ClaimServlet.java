package com.example.bookstore.servlet;

import com.example.bookstore.entity.Borrow;
import com.example.bookstore.entity.User;
import com.example.bookstore.service.BorrowService;
import com.example.bookstore.service.ClaimService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/api/claim/*")
public class ClaimServlet extends BaseApiServlet {

    private ClaimService claimService = new ClaimService();
    private BorrowService borrowService = new BorrowService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireLogin(req, resp)) return;
        User user = sessionUser(req);
        if (isAdmin(user)) {
            json(resp, 200, claimService.findAll());
        } else {
            json(resp, 200, claimService.findByUser(user.getId()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        if (!requireLogin(req, resp)) return;
        User user = sessionUser(req);

        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.startsWith("/report")) {
            Integer borrowId = intParam(req, "borrowId");
            Integer type = intParam(req, "type");
            if (borrowId == null || type == null) {
                json(resp, 400, fail("缺少 borrowId 或 type 参数"));
                return;
            }
            Borrow borrow = borrowService.findById(borrowId);
            if (borrow == null) {
                json(resp, 404, fail("借阅记录不存在"));
                return;
            }
            if (!isAdmin(user) && !user.getId().equals(borrow.getUserId())) {
                json(resp, 403, fail("无权限报失他人借阅"));
                return;
            }
            writeResult(resp, claimService.reportLost(borrowId, borrow.getUserId(), type));
            return;
        }

        Integer id = pathId(req);
        if (id == null || !"handle".equals(lastSegment(req))) {
            json(resp, 400, fail("无效的请求路径"));
            return;
        }
        if (!requirePerm(req, resp, "claim.handle")) return;

        String action = req.getParameter("action");
        int status;
        if ("approve".equals(action)) {
            status = 2;
        } else if ("reject".equals(action)) {
            status = 3;
        } else {
            json(resp, 400, fail("无效的 action 参数"));
            return;
        }
        BigDecimal paid = parseDecimal(req.getParameter("paid"));
        writeResult(resp, claimService.handle(id, status, paid, req.getParameter("remark"), user.getId()));
    }

    private BigDecimal parseDecimal(String v) {
        if (v == null || v.trim().isEmpty()) return null;
        try { return new BigDecimal(v.trim()); }
        catch (NumberFormatException e) { return null; }
    }
}
