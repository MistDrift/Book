package com.example.bookstore.servlet;

import com.example.bookstore.entity.User;
import com.example.bookstore.service.NoticeService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/notice/*")
public class NoticeServlet extends BaseApiServlet {

    private NoticeService noticeService = new NoticeService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireLogin(req, resp)) return;
        User user = sessionUser(req);

        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.startsWith("/count")) {
            Map<String, Object> map = new HashMap<>();
            map.put("count", noticeService.unreadCount(user.getId()));
            json(resp, 200, map);
            return;
        }

        boolean unreadOnly = "1".equals(req.getParameter("unread"));
        json(resp, 200, noticeService.list(user.getId(), unreadOnly));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        if (!requireLogin(req, resp)) return;
        User user = sessionUser(req);

        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.startsWith("/read-all")) {
            noticeService.markAllRead(user.getId());
            ok(resp, "已全部标记为已读");
            return;
        }
        if (pathInfo != null && pathInfo.startsWith("/scan")) {
            if (!requirePerm(req, resp, "dash.view")) return;
            Map<String, Object> map = new HashMap<>();
            map.put("success", true);
            map.put("created", noticeService.scanAndNotify());
            json(resp, 200, map);
            return;
        }

        Integer id = pathId(req);
        if (id != null && "read".equals(lastSegment(req))) {
            noticeService.markRead(id, user.getId());
            ok(resp, "已读");
            return;
        }
        json(resp, 400, fail("无效的请求路径"));
    }
}
