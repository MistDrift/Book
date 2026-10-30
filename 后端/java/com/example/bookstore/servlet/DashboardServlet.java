package com.example.bookstore.servlet;

import com.example.bookstore.service.DashboardService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/dashboard/*")
public class DashboardServlet extends BaseApiServlet {

    private DashboardService dashboardService = new DashboardService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requirePerm(req, resp, "dash.view")) return;
        Integer days = intParam(req, "days");
        json(resp, 200, dashboardService.overview(days != null ? days : 30));
    }
}
