package com.example.bookstore.servlet;

import com.example.bookstore.entity.User;
import com.example.bookstore.service.InventoryService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/inventory/*")
public class InventoryServlet extends BaseApiServlet {

    private InventoryService inventoryService = new InventoryService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requirePerm(req, resp, "inv.view")) return;

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            json(resp, 200, inventoryService.findAll());
            return;
        }
        Integer id = pathId(req);
        if (id != null && "items".equals(lastSegment(req))) {
            json(resp, 200, inventoryService.items(id));
            return;
        }
        json(resp, 400, fail("无效的请求路径"));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        if (!requireLogin(req, resp)) return;
        User user = sessionUser(req);

        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.startsWith("/create")) {
            if (!requirePerm(req, resp, "inv.do")) return;
            String name = req.getParameter("name");
            Integer type = intParam(req, "type");
            if (type == null) {
                json(resp, 400, fail("缺少 type 参数"));
                return;
            }
            writeResult(resp, inventoryService.create(name, type, req.getParameter("area"), user.getId()));
            return;
        }

        Integer id = pathId(req);
        if (id == null) {
            json(resp, 400, fail("无效的请求路径"));
            return;
        }
        String last = lastSegment(req);
        switch (last) {
            case "start":
                if (!requirePerm(req, resp, "inv.do")) return;
                if (inventoryService.start(id)) ok(resp, "盘点已开始");
                else json(resp, 400, fail("启动失败，任务不存在或状态不允许"));
                break;
            case "check":
                if (!requirePerm(req, resp, "inv.do")) return;
                Integer bookId = intParam(req, "bookId");
                Integer realStatus = intParam(req, "realStatus");
                if (bookId == null || realStatus == null) {
                    json(resp, 400, fail("缺少 bookId 或 realStatus 参数"));
                    return;
                }
                if (inventoryService.check(id, bookId, realStatus, req.getParameter("remark"), user.getId())) {
                    ok(resp, "已登记");
                } else {
                    json(resp, 400, fail("登记失败，任务不在进行中或图书不在盘点范围"));
                }
                break;
            case "finish":
                if (!requirePerm(req, resp, "inv.finish")) return;
                writeResult(resp, inventoryService.finish(id));
                break;
            case "cancel":
                if (!requirePerm(req, resp, "inv.finish")) return;
                if (inventoryService.cancel(id)) ok(resp, "盘点已取消");
                else json(resp, 400, fail("取消失败，任务不存在或状态不允许"));
                break;
            default:
                json(resp, 400, fail("无效的请求路径"));
        }
    }
}
