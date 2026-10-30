package com.example.bookstore.servlet;

import com.example.bookstore.entity.User;
import com.example.bookstore.service.LogService;
import com.example.bookstore.util.PermUtil;
import com.example.bookstore.util.Roles;
import com.example.bookstore.util.TokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 操作审计日志查询（仅管理员 + 有 log.view 权限） */
@WebServlet("/api/log/*")
public class LogServlet extends HttpServlet {

    private LogService logService = new LogService();
    private ObjectMapper om = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        User user = TokenUtil.currentUser(req);

        Map<String,Object> r = new HashMap<>();
        if (user == null) {
            resp.setStatus(401);
            r.put("success", false); r.put("message", "未登录");
            out.write(om.writeValueAsString(r));
        } else if (user.getRole() != Roles.ADMIN && !PermUtil.hasPerm(user, "log.view")) {
            resp.setStatus(403);
            r.put("success", false); r.put("message", "无权限");
            out.write(om.writeValueAsString(r));
        } else {
            String action = req.getParameter("action");
            int page = parseInt(req.getParameter("page"), 1);
            int size = parseInt(req.getParameter("size"), 20);
            if (size > 100) size = 100;  // 最多一页100条

            List logs = logService.findAll(action, page, size);
            int total = logService.count(action);

            r.put("success", true);
            r.put("list", logs);
            r.put("total", total);
            r.put("page", page);
            r.put("size", size);
            r.put("pages", (total + size - 1) / size);
            out.write(om.writeValueAsString(r));
        }
        out.flush(); out.close();
    }

    /**
     * 任何人（已登录）都能写自己的访问审计记录；
     * 服务端把 userId 从 Token 解，不信任前端 body 里的 userId。
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        Map<String,Object> r = new HashMap<>();

        User user = TokenUtil.currentUser(req);
        if (user == null) {
            resp.setStatus(401);
            r.put("success", false); r.put("message", "未登录");
            out.write(om.writeValueAsString(r));
            out.flush(); out.close();
            return;
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String,Object> body = om.readValue(req.getInputStream(), Map.class);
            String action = (String) body.getOrDefault("action", "ACCESS");
            String target = (String) body.getOrDefault("target", "");
            String detail = (String) body.getOrDefault("detail", "");
            String level  = (String) body.getOrDefault("level", "INFO");

            // 服务端写日志，userId 从登录态解
            logService.log(user.getId(), action, target, detail, level);

            r.put("success", true);
            r.put("message", "ok");
            out.write(om.writeValueAsString(r));
        } catch (Exception e) {
            resp.setStatus(400);
            r.put("success", false);
            r.put("message", "参数错误");
            out.write(om.writeValueAsString(r));
        }
        out.flush(); out.close();
    }

    private int parseInt(String s, int def) {
        if (s == null) return def;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return def; }
    }
}
