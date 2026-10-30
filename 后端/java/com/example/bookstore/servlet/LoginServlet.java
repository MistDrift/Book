package com.example.bookstore.servlet;

import com.example.bookstore.entity.User;
import com.example.bookstore.service.UserService;
import com.example.bookstore.util.TokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {

    private UserService userService = new UserService();
    private ObjectMapper om = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        User user = userService.login(username, password);

        Map<String, Object> result = new HashMap<>();
        if (user == null) {
            resp.setStatus(401);
            result.put("success", false);
            result.put("message", "用户名或密码错误");
        } else if (user.getStatus() != null && user.getStatus() == 0) {
            resp.setStatus(403);
            result.put("success", false);
            result.put("message", "账号已被禁用，请联系管理员");
        } else {
            user.setPassword(null);
            HttpSession session = req.getSession();
            session.setAttribute("user", user);
            session.setMaxInactiveInterval(3600);

            String token = TokenUtil.create(user);
            result.put("token", token);

            result.put("success", true);
            result.put("message", "登录成功");
            result.put("user", user);
        }

        PrintWriter out = resp.getWriter();
        out.write(om.writeValueAsString(result));
        out.flush(); out.close();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        User user = TokenUtil.currentUser(req);
        Map<String, Object> result = new HashMap<>();

        if (user != null) {
            user.setPassword(null);
            result.put("success", true);
            result.put("user", user);
        } else {
            result.put("success", false);
            result.put("message", "未登录");
        }

        PrintWriter out = resp.getWriter();
        out.write(om.writeValueAsString(result));
        out.flush(); out.close();
    }
}
