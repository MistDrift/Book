package com.example.bookstore.servlet;

import com.example.bookstore.entity.User;
import com.example.bookstore.util.PermUtil;
import com.example.bookstore.util.Roles;
import com.example.bookstore.util.TokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * API Servlet 基类：会话、权限、JSON 响应、路径参数解析的公共逻辑。
 */
public abstract class BaseApiServlet extends HttpServlet {

    protected final ObjectMapper om = new ObjectMapper();

    protected User sessionUser(HttpServletRequest req) {
        return TokenUtil.currentUser(req);
    }

    protected boolean requireLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (sessionUser(req) == null) {
            json(resp, 401, fail("未登录"));
            return false;
        }
        return true;
    }

    protected boolean requirePerm(HttpServletRequest req, HttpServletResponse resp, String code) throws IOException {
        if (!requireLogin(req, resp)) return false;
        if (!PermUtil.hasPerm(sessionUser(req), code)) {
            json(resp, 403, fail("无权限"));
            return false;
        }
        return true;
    }

    /** 管理员（role=1）——拥有系统管理权限。 */
    protected boolean isAdmin(User u) {
        return u != null && u.getRole() != null && u.getRole() == Roles.ADMIN;
    }

    /** 教师（role=3）。 */
    protected boolean isTeacher(User u) {
        return u != null && u.getRole() != null && u.getRole() == Roles.TEACHER;
    }

    /** 线上学员（role=2）。 */
    protected boolean isStudent(User u) {
        return u != null && u.getRole() != null && u.getRole() == Roles.STUDENT;
    }

    /** 社会人员（role=0）。 */
    protected boolean isSocial(User u) {
        return u != null && u.getRole() != null && u.getRole() == Roles.SOCIAL;
    }

    /** 登录态角色能借阅的统一判定（管理员/教师/学员/社会均可借阅）。 */
    protected boolean canBorrow(User u) {
        return u != null && u.getRole() != null;
    }

    protected void json(HttpServletResponse resp, int status, Object obj) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setStatus(status);
        resp.getWriter().write(om.writeValueAsString(obj));
    }

    protected void ok(HttpServletResponse resp, String msg) throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("success", true);
        map.put("message", msg);
        json(resp, 200, map);
    }

    protected Map<String, Object> fail(String msg) {
        Map<String, Object> map = new HashMap<>();
        map.put("success", false);
        map.put("message", msg);
        return map;
    }

    // 按 success 键决定 HTTP 状态码：成功 200，失败 400
    protected void writeResult(HttpServletResponse resp, Map<String, Object> result) throws IOException {
        int status = Boolean.TRUE.equals(result.get("success")) ? 200 : 400;
        json(resp, status, result);
    }

    protected Integer intParam(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        if (v == null || v.trim().isEmpty()) return null;
        try { return Integer.parseInt(v.trim()); }
        catch (NumberFormatException e) { return null; }
    }

    protected String lastSegment(HttpServletRequest req) {
        String path = req.getPathInfo();
        if (path == null || path.equals("/")) return "";
        String[] parts = path.split("/");
        return parts.length > 0 ? parts[parts.length - 1] : "";
    }

    // pathInfo 形如 /123 或 /123/xxx 时解析出 123
    protected Integer pathId(HttpServletRequest req) {
        String path = req.getPathInfo();
        if (path == null || path.equals("/")) return null;
        String[] parts = path.split("/");
        if (parts.length < 2 || parts[1].isEmpty()) return null;
        try { return Integer.parseInt(parts[1]); }
        catch (NumberFormatException e) { return null; }
    }
}
