package com.example.bookstore.servlet;

import com.example.bookstore.entity.User;
import com.example.bookstore.service.CreditService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/credit/*")
public class CreditServlet extends BaseApiServlet {

    private CreditService creditService = new CreditService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireLogin(req, resp)) return;
        User user = sessionUser(req);
        if (isAdmin(user)) {
            json(resp, 200, creditService.all());
        } else {
            json(resp, 200, creditService.history(user.getId()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        if (!requirePerm(req, resp, "user.credit")) return;

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || !pathInfo.startsWith("/adjust")) {
            json(resp, 400, fail("无效的请求路径"));
            return;
        }

        Integer userId = intParam(req, "userId");
        Integer delta = intParam(req, "delta");
        String reason = req.getParameter("reason");
        if (userId == null || delta == null || delta == 0 || reason == null || reason.trim().isEmpty()) {
            json(resp, 400, fail("userId、delta、reason 均必填且 delta 不能为 0"));
            return;
        }

        User user = sessionUser(req);
        int newCredit = creditService.adjust(userId, delta, reason, user.getId());
        if (newCredit < 0) {
            json(resp, 400, fail("用户不存在"));
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("success", true);
        map.put("credit", newCredit);
        json(resp, 200, map);
    }
}
