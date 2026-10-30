package com.example.bookstore.servlet;

import com.example.bookstore.entity.Suggestion;
import com.example.bookstore.entity.User;
import com.example.bookstore.service.SuggestionService;
import com.example.bookstore.util.PermUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/api/suggestion/*")
public class SuggestionServlet extends BaseApiServlet {

    private SuggestionService suggestionService = new SuggestionService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireLogin(req, resp)) return;
        User user = sessionUser(req);
        if (isAdmin(user) && PermUtil.hasPerm(user, "sug.view")) {
            json(resp, 200, suggestionService.findAll());
        } else {
            json(resp, 200, suggestionService.findByUser(user.getId()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        if (!requireLogin(req, resp)) return;
        User user = sessionUser(req);

        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.startsWith("/submit")) {
            Suggestion s = new Suggestion();
            s.userId = user.getId();
            s.type = intParam(req, "type");
            s.title = req.getParameter("title");
            s.author = req.getParameter("author");
            s.publisher = req.getParameter("publisher");
            s.isbn = req.getParameter("isbn");
            s.category = req.getParameter("category");
            s.price = parseDecimal(req.getParameter("price"));
            s.reason = req.getParameter("reason");
            writeResult(resp, suggestionService.submit(s));
            return;
        }

        Integer id = pathId(req);
        if (id == null) {
            json(resp, 400, fail("无效的请求路径"));
            return;
        }
        String last = lastSegment(req);
        if ("vote".equals(last)) {
            writeResult(resp, suggestionService.vote(id, user.getId()));
        } else if ("handle".equals(last)) {
            if (!requirePerm(req, resp, "sug.approve")) return;
            String action = req.getParameter("action");
            int status;
            switch (action != null ? action : "") {
                case "adopt": status = 1; break;
                case "reject": status = 2; break;
                case "purchase": status = 3; break;
                case "arrive": status = 4; break;
                default:
                    json(resp, 400, fail("无效的 action 参数"));
                    return;
            }
            writeResult(resp, suggestionService.handle(id, status, req.getParameter("remark"), user.getId()));
        } else {
            json(resp, 400, fail("无效的请求路径"));
        }
    }

    private BigDecimal parseDecimal(String v) {
        if (v == null || v.trim().isEmpty()) return null;
        try { return new BigDecimal(v.trim()); }
        catch (NumberFormatException e) { return null; }
    }
}
