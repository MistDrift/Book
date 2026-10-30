package com.example.bookstore.service;

import com.example.bookstore.dao.PermDAO;
import com.example.bookstore.entity.Perm;
import com.example.bookstore.util.PermUtil;

import java.util.List;

public class PermService {

    private PermDAO permDAO = new PermDAO();

    public List<Perm> findAll() {
        return permDAO.findAll();
    }

    public List<Perm> findByGroup(String grp) {
        return permDAO.findByGroup(grp);
    }

    // 根据权限 code 列表生成 perms 位掩码
    public long encodeBits(List<String> codes) {
        long bits = 0L;
        for (String code : codes) {
            Integer pos = permDAO.findPosByCode(code);
            if (pos != null && pos >= 0 && pos < 64) {
                bits |= (1L << pos);
            }
        }
        return bits;
    }

    // 清除权限缓存
    public void clearCache() {
        PermUtil.clearCache();
    }
}
