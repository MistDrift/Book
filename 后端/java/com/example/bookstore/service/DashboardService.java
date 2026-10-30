package com.example.bookstore.service;

import com.example.bookstore.dao.DashboardDAO;

import java.util.HashMap;
import java.util.Map;

public class DashboardService {

    private final DashboardDAO dashboardDAO = new DashboardDAO();

    public Map<String, Object> overview(int days) {
        Map<String, Object> map = new HashMap<>();
        map.put("summary", dashboardDAO.summary());
        map.put("trend", dashboardDAO.borrowTrend(days));
        map.put("categories", dashboardDAO.categoryDist());
        map.put("topBooks", dashboardDAO.topBooks(10));
        map.put("roles", dashboardDAO.roleDist());
        map.put("recent", dashboardDAO.recentBorrows(8));
        map.put("fines", dashboardDAO.fineSummary());
        map.put("borrowTotal", dashboardDAO.borrowTotal());
        return map;
    }
}
