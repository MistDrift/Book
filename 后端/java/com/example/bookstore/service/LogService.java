package com.example.bookstore.service;

import com.example.bookstore.dao.LogDAO;
import com.example.bookstore.entity.Log;

import java.util.List;

public class LogService {

    private LogDAO logDAO = new LogDAO();

    public int add(Log log) {
        return logDAO.insert(log);
    }

    /** 便捷审计写入：服务端组装 Log 对象。level INFO→ok=1, WARN/ERROR→ok=0 */
    public int log(Integer userId, String action, String target, String detail, String level) {
        Log l = new Log();
        l.setUserId(userId);
        l.setAction(action);
        l.setTarget(target);
        l.setDetail(detail);
        l.setOk("INFO".equalsIgnoreCase(level) ? 1 : 0);
        return logDAO.insert(l);
    }

    public List<Log> findAll(String action, int page, int size) {
        return logDAO.findAll(action, page, size);
    }

    public int count(String action) {
        return logDAO.count(action);
    }
}
