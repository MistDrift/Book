package com.example.bookstore.servlet;

import com.example.bookstore.entity.Borrow;
import com.example.bookstore.entity.User;
import com.example.bookstore.service.BorrowService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/api/borrow/*")
public class BorrowServlet extends BaseApiServlet {

    private BorrowService borrowService = new BorrowService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireLogin(req, resp)) return;
        User user = sessionUser(req);

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            List<Borrow> borrows;
            if (isAdmin(user)) {
                Integer status = intParam(req, "status");
                borrows = status != null ? borrowService.findByStatus(status) : borrowService.findAll();
            } else {
                borrows = borrowService.findByUserId(user.getId());
            }
            json(resp, 200, borrows);
            return;
        }

        Integer id = pathId(req);
        if (id == null) {
            json(resp, 400, fail("无效的请求路径"));
            return;
        }
        Borrow borrow = borrowService.findById(id);
        if (borrow == null) {
            json(resp, 404, fail("借阅记录不存在"));
            return;
        }
        if (!isAdmin(user) && !user.getId().equals(borrow.getUserId())) {
            json(resp, 403, fail("无权限查看他人借阅记录"));
            return;
        }
        json(resp, 200, borrow);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        if (!requireLogin(req, resp)) return;
        User user = sessionUser(req);

        String pathInfo = req.getPathInfo();
        if (pathInfo != null && !pathInfo.equals("/")) {
            json(resp, 400, fail("无效的请求路径"));
            return;
        }

        Integer bookId = intParam(req, "bookId");
        if (bookId == null) {
            json(resp, 400, fail("缺少 bookId 参数"));
            return;
        }
        // 管理员可代读者借阅（传 userId）；普通读者只能为自己借阅
        Integer borrowerId = user.getId();
        if (isAdmin(user)) {
            Integer targetUserId = intParam(req, "userId");
            if (targetUserId != null) borrowerId = targetUserId;
        }
        Map<String, Object> result = borrowService.borrow(borrowerId, bookId);
        writeResult(resp, result);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        if (!requireLogin(req, resp)) return;
        User user = sessionUser(req);

        Integer id = pathId(req);
        if (id == null) {
            json(resp, 400, fail("无效的请求路径"));
            return;
        }

        Borrow borrow = borrowService.findById(id);
        if (borrow == null) {
            json(resp, 404, fail("借阅记录不存在"));
            return;
        }
        if (!isAdmin(user) && !user.getId().equals(borrow.getUserId())) {
            json(resp, 403, fail("无权限操作他人借阅记录"));
            return;
        }

        String pathInfo = req.getPathInfo();
        Map<String, Object> result;
        if (pathInfo != null && pathInfo.equals("/" + id)) {
            result = borrowService.returnBookDetail(id, user.getId());
        } else if (pathInfo != null && pathInfo.equals("/" + id + "/renew")) {
            result = borrowService.renew(id, user.getId());
        } else {
            json(resp, 400, fail("未知操作"));
            return;
        }
        writeResult(resp, result);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requirePerm(req, resp, "borrow.view")) return;
        User user = sessionUser(req);
        if (!isAdmin(user)) {
            json(resp, 403, fail("无权限"));
            return;
        }

        Integer id = pathId(req);
        if (id == null) {
            json(resp, 400, fail("无效的请求路径"));
            return;
        }

        if (borrowService.delete(id)) {
            ok(resp, "删除成功");
        } else {
            json(resp, 400, fail("删除失败"));
        }
    }
}
