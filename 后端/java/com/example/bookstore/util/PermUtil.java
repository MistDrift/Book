package com.example.bookstore.util;

import com.example.bookstore.dao.PermDAO;
import com.example.bookstore.entity.Perm;
import com.example.bookstore.entity.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 权限工具类：根据 permission_bits 位掩码判断用户是否拥有指定权限。
 * pos 缓存在内存中，避免每次查表。
 */
public class PermUtil {

    private static final PermDAO permDAO = new PermDAO();
    private static volatile Map<String, Integer> posCache = null;

    // 懒加载缓存：code -> pos
    private static Map<String, Integer> getCache() {
        if (posCache == null) {
            synchronized (PermUtil.class) {
                if (posCache == null) {
                    Map<String, Integer> map = new HashMap<>();
                    try {
                        List<Perm> perms = permDAO.findAll();
                        for (Perm p : perms) {
                            map.put(p.getCode(), p.getPos());
                        }
                    } catch (Exception ignored) {
                        // tb_perm 表可能还没创建，降级为空 map
                    }
                    posCache = map;
                }
            }
        }
        return posCache;
    }

    /**
     * 判断用户是否拥有指定权限。
     * 规则：
     *   - role=1 且 perms=-1  → 全权限（不查表）
     *   - perms=0 或 null      → 无权限（非管理员或未分配）
     *   - 其他                 → 查 tb_perm 得到位位置，按位与
     */
    public static boolean hasPerm(User user, String code) {
        if (user == null) return false;
        Long perms = user.getPerms();
        if (perms == null) perms = 0L;
        if (perms == -1L) return true;  // 全权限
        if (perms == 0L) return false;  // 无权限

        Integer pos = getCache().get(code);
        if (pos == null) return false;  // 权限码不存在
        return (perms & (1L << pos)) != 0;
    }

    // 清除缓存（权限表变更后调用）
    public static void clearCache() {
        posCache = null;
    }
}
