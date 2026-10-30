package com.example.bookstore.servlet;

import com.example.bookstore.entity.Tier;
import com.example.bookstore.entity.User;
import com.example.bookstore.service.TierService;
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
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 读者等级管理（管理员） */
@WebServlet("/api/tier/*")
public class TierServlet extends HttpServlet {

    private TierService tierService = new TierService();
    private ObjectMapper om = new ObjectMapper();

    private User requireLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = TokenUtil.currentUser(req);
        if (user == null) {
            resp.setContentType("application/json;charset=UTF-8");
            resp.setStatus(401);
            Map<String,Object> r = new HashMap<>();
            r.put("success", false); r.put("message", "未登录");
            PrintWriter out = resp.getWriter();
            out.write(om.writeValueAsString(r));
            out.flush(); out.close();
            return null;
        }
        return user;
    }

    private boolean requireAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = requireLogin(req, resp);
        if (user == null) return false;
        if (user.getRole() == null || user.getRole() != Roles.ADMIN) {
            resp.setContentType("application/json;charset=UTF-8");
            resp.setStatus(403);
            Map<String,Object> r = new HashMap<>();
            r.put("success", false); r.put("message", "无权限");
            PrintWriter out = resp.getWriter();
            out.write(om.writeValueAsString(r));
            out.flush(); out.close();
            return false;
        }
        return true;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        if (requireLogin(req, resp) == null) return;
        PrintWriter out = resp.getWriter();
        String path = req.getPathInfo();

        if (path == null || path.equals("/")) {
            List<Tier> tiers = tierService.findAll();
            out.write(om.writeValueAsString(tiers));
        } else if (path.matches("/\\d+")) {
            Integer id = Integer.parseInt(path.substring(1));
            Tier t = tierService.findById(id);
            if (t != null) out.write(om.writeValueAsString(t));
            else {
                resp.setStatus(404);
                Map<String,Object> r = new HashMap<>();
                r.put("success", false); r.put("message", "等级不存在");
                out.write(om.writeValueAsString(r));
            }
        } else {
            resp.setStatus(400);
        }
        out.flush(); out.close();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        if (!requireAdmin(req, resp)) return;
        Map<String,Object> r = new HashMap<>();
        PrintWriter out = resp.getWriter();
        try {
            Tier t = new Tier();
            t.setName(req.getParameter("name"));
            t.setMinScore(Integer.parseInt(req.getParameter("minScore")));
            t.setMaxBorrow(Integer.parseInt(req.getParameter("maxBorrow")));
            t.setDays(Integer.parseInt(req.getParameter("days")));
            t.setRenewMax(Integer.parseInt(req.getParameter("renewMax")));
            t.setRenewDays(Integer.parseInt(req.getParameter("renewDays")));
            t.setPriority(Integer.parseInt(req.getParameter("priority")));
            t.setRate(new BigDecimal(req.getParameter("rate")));
            t.setNote(req.getParameter("note"));
            r.put("success", tierService.insert(t));
        } catch (Exception e) {
            r.put("success", false); r.put("message", e.getMessage());
        }
        out.write(om.writeValueAsString(r));
        out.flush(); out.close();
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        if (!requireAdmin(req, resp)) return;
        Map<String,Object> r = new HashMap<>();
        PrintWriter out = resp.getWriter();
        try {
            Tier t = new Tier();
            t.setId(Integer.parseInt(req.getParameter("id")));
            t.setName(req.getParameter("name"));
            t.setMinScore(Integer.parseInt(req.getParameter("minScore")));
            t.setMaxBorrow(Integer.parseInt(req.getParameter("maxBorrow")));
            t.setDays(Integer.parseInt(req.getParameter("days")));
            t.setRenewMax(Integer.parseInt(req.getParameter("renewMax")));
            t.setRenewDays(Integer.parseInt(req.getParameter("renewDays")));
            t.setPriority(Integer.parseInt(req.getParameter("priority")));
            t.setRate(new BigDecimal(req.getParameter("rate")));
            t.setNote(req.getParameter("note"));
            r.put("success", tierService.update(t));
        } catch (Exception e) {
            r.put("success", false); r.put("message", e.getMessage());
        }
        out.write(om.writeValueAsString(r));
        out.flush(); out.close();
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        if (!requireAdmin(req, resp)) return;
        Map<String,Object> r = new HashMap<>();
        PrintWriter out = resp.getWriter();
        String path = req.getPathInfo();
        if (path != null && path.matches("/\\d+")) {
            Integer id = Integer.parseInt(path.substring(1));
            r.put("success", tierService.delete(id));
        } else {
            resp.setStatus(400); r.put("success", false); r.put("message", "无效ID");
        }
        out.write(om.writeValueAsString(r));
        out.flush(); out.close();
    }
}
