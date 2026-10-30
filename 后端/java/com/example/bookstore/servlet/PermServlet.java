package com.example.bookstore.servlet;

import com.example.bookstore.entity.Perm;
import com.example.bookstore.entity.User;
import com.example.bookstore.service.PermService;
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

/** 权限字典查询（供前端下拉列表） */
@WebServlet("/api/perm/*")
public class PermServlet extends HttpServlet {

    private PermService permService = new PermService();
    private ObjectMapper om = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        User user = TokenUtil.currentUser(req);

        // 所有登录用户都能查询权限字典（前端展示权限复选框需要）
        if (user == null) {
            resp.setStatus(401);
            Map<String,Object> r = new HashMap<>();
            r.put("success", false); r.put("message", "未登录");
            out.write(om.writeValueAsString(r));
        } else {
            String grp = req.getParameter("grp");
            List<Perm> perms = grp != null ? permService.findByGroup(grp) : permService.findAll();
            out.write(om.writeValueAsString(perms));
        }
        out.flush(); out.close();
    }
}
