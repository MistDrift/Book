package com.example.bookstore.servlet;

import com.example.bookstore.entity.User;
import com.example.bookstore.service.ReserveService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/reserve/*")
public class ReserveServlet extends BaseApiServlet {

    private ReserveService reserveService = new ReserveService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireLogin(req, resp)) return;
        User user = sessionUser(req);
        if (isAdmin(user)) {
            json(resp, 200, reserveService.findAll());
        } else {
            json(resp, 200, reserveService.findByUser(user.getId()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        if (!requireLogin(req, resp)) return;
        User user = sessionUser(req);

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            Integer bookId = intParam(req, "bookId");
            if (bookId == null) {
                json(resp, 400, fail("缺少 bookId 参数"));
                return;
            }
            writeResult(resp, reserveService.reserve(user.getId(), bookId));
            return;
        }

        Integer id = pathId(req);
        if (id == null) {
            json(resp, 400, fail("无效的请求路径"));
            return;
        }
        String last = lastSegment(req);
        if ("cancel".equals(last)) {
            writeResult(resp, reserveService.cancel(id, user.getId(), isAdmin(user)));
        } else if ("pick".equals(last)) {
            if (!requirePerm(req, resp, "reserve.manage")) return;
            if (reserveService.pick(id)) {
                ok(resp, "取书成功，已转为借阅");
            } else {
                json(resp, 400, fail("取书失败，预约不存在或未通知"));
            }
        } else {
            json(resp, 400, fail("无效的请求路径"));
        }
    }
}
