package com.example.bookstore.servlet;

import com.example.bookstore.entity.Fine;
import com.example.bookstore.entity.User;
import com.example.bookstore.service.FineService;
import com.example.bookstore.util.Roles;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/fine/*")
public class FineServlet extends BaseApiServlet {

    private FineService fineService = new FineService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireLogin(req, resp)) return;
        User user = sessionUser(req);

        if (!isAdmin(user)) {
            // 非管理员仅能查看本人罚金
            json(resp, 200, fineService.findByUser(user.getId()));
            return;
        }
        Integer userId = intParam(req, "userId");
        Integer status = intParam(req, "status");
        if (userId != null) {
            json(resp, 200, fineService.findByUser(userId));
        } else if (status != null) {
            json(resp, 200, fineService.findByStatus(status));
        } else {
            json(resp, 200, fineService.findAll());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        if (!requireLogin(req, resp)) return;
        User user = sessionUser(req);

        Integer id = pathId(req);
        if (id == null) {
            json(resp, 400, fail("无效的请求路径"));
            return;
        }
        String last = lastSegment(req);

        if ("pay".equals(last)) {
            Fine fine = fineService.findById(id);
            if (fine == null) {
                json(resp, 404, fail("罚金记录不存在"));
                return;
            }
            if (!isAdmin(user) && !user.getId().equals(fine.userId)) {
                json(resp, 403, fail("无权限缴纳他人罚金"));
                return;
            }
            writeResult(resp, fineService.pay(id, user.getId()));
        } else if ("waive".equals(last)) {
            if (!requirePerm(req, resp, "fine.waive")) return;
            writeResult(resp, fineService.waive(id, req.getParameter("reason"), user.getId()));
        } else {
            json(resp, 400, fail("无效的请求路径"));
        }
    }
}
