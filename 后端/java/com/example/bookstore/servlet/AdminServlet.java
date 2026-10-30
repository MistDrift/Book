package com.example.bookstore.servlet;

import com.example.bookstore.entity.User;
import com.example.bookstore.service.PermService;
import com.example.bookstore.service.UserService;

import com.example.bookstore.util.Roles;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 多级管理员权限分配：查看权限位、管理员列表、授权/收回。
 */
@WebServlet("/api/admin/*")
public class AdminServlet extends BaseApiServlet {

    private UserService userService = new UserService();
    private PermService permService = new PermService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requirePerm(req, resp, "sys.config")) return;

        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.startsWith("/perms")) {
            json(resp, 200, permService.findAll());
            return;
        }
        if (pathInfo != null && pathInfo.startsWith("/admins")) {
            List<User> admins = new ArrayList<>();
            for (User u : userService.findAll()) {
                if (u.getRole() != null && u.getRole() == Roles.ADMIN) admins.add(u);
            }
            json(resp, 200, admins);
            return;
        }
        json(resp, 400, fail("无效的请求路径"));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        if (!requirePerm(req, resp, "sys.config")) return;

        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            json(resp, 400, fail("无效的请求路径"));
            return;
        }

        if (pathInfo.startsWith("/grant")) {
            Integer userId = intParam(req, "userId");
            Long perms = longParam(req, "perms");
            if (userId == null || perms == null) {
                json(resp, 400, fail("缺少 userId 或 perms 参数"));
                return;
            }
            User target = userService.findById(userId);
            if (target == null) {
                json(resp, 404, fail("用户不存在"));
                return;
            }
            userService.updatePerms(userId, perms);
            target.setRole(1);
            target.setPerms(perms);
            userService.update(target);
            ok(resp, "已授权");
            return;
        }

        if (pathInfo.startsWith("/revoke")) {
            Integer userId = intParam(req, "userId");
            if (userId == null) {
                json(resp, 400, fail("缺少 userId 参数"));
                return;
            }
            User target = userService.findById(userId);
            if (target == null) {
                json(resp, 404, fail("用户不存在"));
                return;
            }
            target.setRole(0);
            target.setPerms(0L);
            userService.update(target);
            ok(resp, "已收回权限");
            return;
        }

        json(resp, 400, fail("无效的请求路径"));
    }

    private Long longParam(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        if (v == null || v.trim().isEmpty()) return null;
        try { return Long.parseLong(v.trim()); }
        catch (NumberFormatException e) { return null; }
    }
}
