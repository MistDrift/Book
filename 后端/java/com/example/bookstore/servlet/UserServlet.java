package com.example.bookstore.servlet;

import com.example.bookstore.entity.User;
import com.example.bookstore.service.UserService;
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

@WebServlet("/api/user/*")
public class UserServlet extends HttpServlet {
    
    private UserService userService = new UserService();
    private ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        
        PrintWriter out = response.getWriter();

        User currentUser = TokenUtil.currentUser(request);
        if (currentUser == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "未登录");
            out.write(objectMapper.writeValueAsString(result));
            out.flush();
            out.close();
            return;
        }

        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            if (currentUser.getRole() != Roles.ADMIN || !PermUtil.hasPerm(currentUser, "user.view")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "无权限");
                out.write(objectMapper.writeValueAsString(result));
                out.flush();
                out.close();
                return;
            }
            List<User> users = userService.findAll();
            for (User u : users) {
                u.setPassword(null);
            }
            out.write(objectMapper.writeValueAsString(users));
        } else if (pathInfo.equals("/profile")) {
            currentUser.setPassword(null);
            out.write(objectMapper.writeValueAsString(currentUser));
        } else if (pathInfo.matches("/\\d+")) {
            if (currentUser.getRole() != Roles.ADMIN || !PermUtil.hasPerm(currentUser, "user.view")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "无权限");
                out.write(objectMapper.writeValueAsString(result));
                out.flush();
                out.close();
                return;
            }
            try {
                Integer id = Integer.parseInt(pathInfo.substring(1));
                User user = userService.findById(id);
                if (user != null) {
                    user.setPassword(null);
                    out.write(objectMapper.writeValueAsString(user));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", false);
                    result.put("message", "用户不存在");
                    out.write(objectMapper.writeValueAsString(result));
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "无效的用户ID");
                out.write(objectMapper.writeValueAsString(result));
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "无效的请求路径");
            out.write(objectMapper.writeValueAsString(result));
        }
        
        out.flush();
        out.close();
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        
        Map<String, Object> result = new HashMap<>();
        PrintWriter out = response.getWriter();

        User currentUser = TokenUtil.currentUser(request);
        if (currentUser == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            result.put("success", false);
            result.put("message", "未登录");
            out.write(objectMapper.writeValueAsString(result));
            out.flush();
            out.close();
            return;
        }
        if (currentUser.getRole() != Roles.ADMIN || !PermUtil.hasPerm(currentUser, "user.edit")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            result.put("success", false);
            result.put("message", "无权限");
            out.write(objectMapper.writeValueAsString(result));
            out.flush();
            out.close();
            return;
        }
        
        try {
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String name = request.getParameter("name");
            String role = request.getParameter("role");
            String email = request.getParameter("email");
            String phone = request.getParameter("phone");
            
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            user.setName(name);
            user.setRole(Integer.parseInt(role));
            user.setEmail(email);
            user.setPhone(phone);
            
            if (userService.insert(user)) {
                result.put("success", true);
                result.put("message", "添加成功");
            } else {
                result.put("success", false);
                result.put("message", "添加失败，用户名可能已存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "添加失败：" + e.getMessage());
        }
        
        out.write(objectMapper.writeValueAsString(result));
        out.flush();
        out.close();
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        
        Map<String, Object> result = new HashMap<>();
        PrintWriter out = response.getWriter();

        User currentUser = TokenUtil.currentUser(request);
        if (currentUser == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            result.put("success", false);
            result.put("message", "未登录");
            out.write(objectMapper.writeValueAsString(result));
            out.flush();
            out.close();
            return;
        }

        String pathInfo = request.getPathInfo();

        // PUT /api/user/password 修改本人密码
        if (pathInfo != null && pathInfo.equals("/password")) {
            String oldPassword = request.getParameter("oldPassword");
            String newPassword = request.getParameter("newPassword");
            if (newPassword == null || newPassword.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "新密码不能为空");
            } else {
                User dbUser = userService.findById(currentUser.getId());
                if (dbUser == null || oldPassword == null || !oldPassword.equals(dbUser.getPassword())) {
                    result.put("success", false);
                    result.put("message", "原密码错误");
                } else {
                    dbUser.setPassword(newPassword);
                    if (userService.update(dbUser)) {
                        result.put("success", true);
                        result.put("message", "密码修改成功");
                    } else {
                        result.put("success", false);
                        result.put("message", "密码修改失败");
                    }
                }
            }
            out.write(objectMapper.writeValueAsString(result));
            out.flush();
            out.close();
            return;
        }

        // PUT /api/user/{id}/status 启用/禁用（管理员 + 学员可操作）
        if (pathInfo != null && pathInfo.matches("/\\d+/status")) {
            if (currentUser.getRole() != Roles.ADMIN && currentUser.getRole() != Roles.STUDENT) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                result.put("success", false);
                result.put("message", "无权限");
                out.write(objectMapper.writeValueAsString(result));
                out.flush();
                out.close();
                return;
            }
            try {
                Integer targetId = Integer.parseInt(pathInfo.split("/")[1]);
                String statusStr = request.getParameter("status");
                if (!"0".equals(statusStr) && !"1".equals(statusStr)) {
                    result.put("success", false);
                    result.put("message", "无效的 status 参数");
                } else if (targetId.equals(currentUser.getId()) && "0".equals(statusStr)) {
                    result.put("success", false);
                    result.put("message", "不能禁用自己的账号");
                } else if (userService.findById(targetId) == null) {
                    result.put("success", false);
                    result.put("message", "用户不存在");
                } else if (userService.updateStatus(targetId, Integer.parseInt(statusStr))) {
                    result.put("success", true);
                    result.put("message", "1".equals(statusStr) ? "已启用" : "已禁用");
                } else {
                    result.put("success", false);
                    result.put("message", "操作失败");
                }
            } catch (NumberFormatException e) {
                result.put("success", false);
                result.put("message", "无效的用户ID");
            }
            out.write(objectMapper.writeValueAsString(result));
            out.flush();
            out.close();
            return;
        }
        
        try {
            String id = request.getParameter("id");
            String password = request.getParameter("password");
            String name = request.getParameter("name");
            String email = request.getParameter("email");
            String phone = request.getParameter("phone");
            
            User user = userService.findById(Integer.parseInt(id));
            if (user == null) {
                result.put("success", false);
                result.put("message", "用户不存在");
                out.write(objectMapper.writeValueAsString(result));
                out.flush();
                out.close();
                return;
            }
            
            if (currentUser.getRole() != Roles.ADMIN && !currentUser.getId().equals(user.getId())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                result.put("success", false);
                result.put("message", "无权限");
                out.write(objectMapper.writeValueAsString(result));
                out.flush();
                out.close();
                return;
            }

            // 管理员编辑他人需要 user.edit 权限
            if (currentUser.getRole() == Roles.ADMIN && !currentUser.getId().equals(user.getId())
                    && !PermUtil.hasPerm(currentUser, "user.edit")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                result.put("success", false);
                result.put("message", "无权限编辑用户");
                out.write(objectMapper.writeValueAsString(result));
                out.flush();
                out.close();
                return;
            }

            if (password != null && !password.trim().isEmpty()) {
                user.setPassword(password);
            }
            if (name != null) {
                user.setName(name);
            }
            if (email != null) {
                user.setEmail(email);
            }
            if (phone != null) {
                user.setPhone(phone);
            }

            // 管理员且有 user.edit 权限才能改角色
            if (currentUser.getRole() == Roles.ADMIN && PermUtil.hasPerm(currentUser, "user.edit")) {
                String role = request.getParameter("role");
                if (role != null) {
                    user.setRole(Integer.parseInt(role));
                }
            }
            
            if (userService.update(user)) {
                result.put("success", true);
                result.put("message", "更新成功");
            } else {
                result.put("success", false);
                result.put("message", "更新失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败：" + e.getMessage());
        }
        
        out.write(objectMapper.writeValueAsString(result));
        out.flush();
        out.close();
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        
        Map<String, Object> result = new HashMap<>();
        PrintWriter out = response.getWriter();

        User user = TokenUtil.currentUser(request);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            result.put("success", false);
            result.put("message", "未登录");
            out.write(objectMapper.writeValueAsString(result));
            out.flush();
            out.close();
            return;
        }

        if (user.getRole() != Roles.ADMIN || !PermUtil.hasPerm(user, "user.delete")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            result.put("success", false);
            result.put("message", "无权限");
            out.write(objectMapper.writeValueAsString(result));
            out.flush();
            out.close();
            return;
        }
        
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || !pathInfo.matches("/\\d+")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.put("success", false);
            result.put("message", "无效的请求路径");
            out.write(objectMapper.writeValueAsString(result));
            out.flush();
            out.close();
            return;
        }
        
        try {
            Integer id = Integer.parseInt(pathInfo.substring(1));
            if (id.equals(user.getId())) {
                result.put("success", false);
                result.put("message", "不能删除自己");
            } else if (userService.delete(id)) {
                result.put("success", true);
                result.put("message", "删除成功");
            } else {
                result.put("success", false);
                result.put("message", "删除失败");
            }
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "无效的用户ID");
        }
        
        out.write(objectMapper.writeValueAsString(result));
        out.flush();
        out.close();
    }
}