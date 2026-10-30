package com.example.bookstore.service;

import com.example.bookstore.dao.TierDAO;
import com.example.bookstore.entity.Tier;
import com.example.bookstore.util.PermUtil;

import java.util.List;

public class TierService {

    private TierDAO tierDAO = new TierDAO();

    public List<Tier> findAll() {
        return tierDAO.findAll();
    }

    public Tier findById(Integer id) {
        return tierDAO.findById(id);
    }

    public boolean insert(Tier t) {
        return tierDAO.insert(t) > 0;
    }

    public boolean update(Tier t) {
        boolean ok = tierDAO.update(t) > 0;
        if (ok) PermUtil.clearCache();
        return ok;
    }

    public boolean delete(Integer id) {
        boolean ok = tierDAO.delete(id) > 0;
        if (ok) PermUtil.clearCache();
        return ok;
    }
}
