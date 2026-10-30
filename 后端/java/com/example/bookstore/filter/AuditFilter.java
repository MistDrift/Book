package com.example.bookstore.filter;

import com.example.bookstore.entity.Log;
import com.example.bookstore.entity.User;
import com.example.bookstore.service.LogService;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 审计日志 Filter：拦截所有 /api/* 请求，记录操作日志到 tb_log。
 * 在 chain.doFilter 之后用 response.getStatus() 判断成功/失败。
 */
@WebFilter("/api/*")
public class AuditFilter implements Filter {

    private LogService logService = new LogService();

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void destroy() {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        long start = System.currentTimeMillis();

        // 放行前调用
        chain.doFilter(request, response);

        // 之后写日志
        try {
            HttpSession session = req.getSession(false);
            User user = session != null ? (User) session.getAttribute("user") : null;

            String method = req.getMethod();
            String uri = req.getRequestURI();
            String action = method + "_" + inferAction(uri);
            if (resp.getStatus() >= 400) {
                action += "_FAIL";
            }

            Log log = new Log();
            if (user != null) {
                log.setUserId(user.getId());
                log.setUsername(user.getUsername());
            }
            log.setAction(action.toUpperCase());
            // 简单从 URI 推断 target 类型
            String path = uri.replaceAll("/api/", "");
            String[] parts = path.split("/");
            if (parts.length > 0) {
                log.setTarget(parts[0]);
            }
            // 提取 target id
            if (parts.length > 1) {
                try { log.setTargetId(Integer.parseInt(parts[1].split("\\?")[0])); }
                catch (NumberFormatException ignored) {}
            }
            log.setDetail(req.getParameterMap().isEmpty() ? "" : truncate(req.getQueryString(), 500));
            log.setIp(getClientIp(req));
            log.setUa(req.getHeader("User-Agent"));
            log.setOk(resp.getStatus() < 400 ? 1 : 0);

            // 异步化不重要，这里简单同步写入即可（非高频 API 场景）
            logService.add(log);
        } catch (Exception ignored) {
            // 日志写入失败不影响主流程
        }
    }

    private String inferAction(String uri) {
        if (uri.contains("login")) return "LOGIN";
        if (uri.contains("logout")) return "LOGOUT";
        if (uri.contains("dashboard")) return "DASHBOARD";
        if (uri.contains("inventory")) return "INVENTORY";
        if (uri.contains("suggestion")) return "SUGGESTION";
        if (uri.contains("reserve")) return "RESERVE";
        if (uri.contains("notice")) return "NOTICE";
        if (uri.contains("credit")) return "CREDIT";
        if (uri.contains("borrow")) return "BORROW";
        if (uri.contains("claim")) return "CLAIM";
        if (uri.contains("fine")) return "FINE";
        if (uri.contains("admin")) return "ADMIN";
        if (uri.contains("book")) return "BOOK";
        if (uri.contains("user")) return "USER";
        if (uri.contains("tier")) return "TIER";
        if (uri.contains("perm")) return "PERM";
        if (uri.contains("log")) return "LOG";
        return "OTHER";
    }

    private String getClientIp(HttpServletRequest req) {
        String[] headers = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP"};
        for (String h : headers) {
            String ip = req.getHeader(h);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        return req.getRemoteAddr();
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) : s;
    }
}
