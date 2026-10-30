package com.example.bookstore.servlet;

import com.example.bookstore.dao.UserDAO;
import com.example.bookstore.entity.User;
import com.example.bookstore.service.LogService;
import com.example.bookstore.util.Roles;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * 公开注册接口：任何人都能 POST 注册。
 * 安全约束：role 只能是 0(社会人员)/2(线上学员)/3(教师)，拒绝管理员自注册。
 */
@WebServlet("/api/register")
public class RegisterServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAO();
    private LogService logService = new LogService();
    private ObjectMapper om = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        Map<String, Object> r = new HashMap<>();

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = om.readValue(req.getInputStream(), Map.class);

            String username = (String) body.get("username");
            String password = (String) body.get("password");
            String name     = (String) body.getOrDefault("name", username);
            String email    = (String) body.getOrDefault("email", "");
            String phone    = (String) body.getOrDefault("phone", "");
            // 注册表单里 role 是字符串 "0"/"2"/"3"
            Object roleObj  = body.getOrDefault("role", Roles.SOCIAL);
            int role;
            if (roleObj instanceof Number) role = ((Number) roleObj).intValue();
            else role = Integer.parseInt(roleObj.toString().trim());

            // ====== 校验 ======
            if (username == null || username.trim().isEmpty() || username.length() < 2) {
                resp.setStatus(400);
                r.put("success", false); r.put("message", "用户名至少 2 个字符");
                out.write(om.writeValueAsString(r)); out.flush(); out.close(); return;
            }
            if (password == null || password.length() < 4) {
                resp.setStatus(400);
                r.put("success", false); r.put("message", "密码至少 4 个字符");
                out.write(om.writeValueAsString(r)); out.flush(); out.close(); return;
            }
            if (role == Roles.ADMIN) {
                resp.setStatus(400);
                r.put("success", false); r.put("message", "管理员不可自行注册");
                out.write(om.writeValueAsString(r)); out.flush(); out.close(); return;
            }
            if (role != Roles.SOCIAL && role != Roles.STUDENT && role != Roles.TEACHER) {
                resp.setStatus(400);
                r.put("success", false); r.put("message", "非法角色");
                out.write(om.writeValueAsString(r)); out.flush(); out.close(); return;
            }
            // 用户名唯一
            if (userDAO.existsByUsername(username)) {
                resp.setStatus(409);
                r.put("success", false); r.put("message", "用户名已存在");
                out.write(om.writeValueAsString(r)); out.flush(); out.close(); return;
            }

            // ====== 入库 ======
            User u = new User();
            u.setUsername(username.trim());
            u.setPassword(password);          // 明文（当前系统统一明文，登录时比对明文）
            u.setName(name.trim().isEmpty() ? username.trim() : name.trim());
            u.setEmail(email == null ? "" : email.trim());
            u.setPhone(phone == null ? "" : phone.trim());
            u.setRole(role);
            u.setTierId(1);                    // 默认等级
            u.setCredit(100);                  // 默认信用分
            u.setPerms(0L);
            u.setStatus(1);                    // 默认启用

            int ok = userDAO.insert(u);
            if (ok <= 0) {
                resp.setStatus(500);
                r.put("success", false); r.put("message", "注册失败，请稍后重试");
                out.write(om.writeValueAsString(r)); out.flush(); out.close(); return;
            }

            // 审计日志（用户自己写自己的注册记录，此时还没 token → 用 user_id 0 兜底）
            logService.log(u.getId(), "REGISTER", "register.html",
                "新用户注册 username=" + u.getUsername() + " role=" + role, "INFO");

            r.put("success", true);
            r.put("message", "注册成功，请登录");
            out.write(om.writeValueAsString(r));

        } catch (Exception e) {
            resp.setStatus(400);
            r.put("success", false);
            r.put("message", "参数错误");
            out.write(om.writeValueAsString(r));
        }
        out.flush(); out.close();
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // 让 CorsFilter 处理；这里兜底返回 200
        resp.setStatus(200);
    }
}
