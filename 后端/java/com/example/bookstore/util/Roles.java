package com.example.bookstore.util;

/**
 * 用户角色常量。role 值与数据库 tb_user.role 字段保持一致。
 * 四类角色：
 *   ADMIN    = 1  管理员      — 系统全部管理权限
 *   TEACHER  = 3  教师        — 借阅/归还/预约/认领遗失图书
 *   STUDENT  = 2  线上学员    — 借阅/归还/预约/续借、查看个人借阅/罚款/信用
 *   SOCIAL   = 0  社会人员    — 浏览图书目录、注册后可借阅
 */
public final class Roles {

    public static final int ADMIN   = 1;
    public static final int STUDENT = 2;
    public static final int TEACHER = 3;
    public static final int SOCIAL  = 0;

    private static final String[] NAMES = { "社会人员", "管理员", "线上学员", "教师" };

    private Roles() {}

    public static String name(Integer role) {
        if (role == null) return "未知";
        if (role >= 0 && role < NAMES.length) return NAMES[role];
        return "未知";
    }

    /** 角色的身份标签，用于前端导航显示（括号里的短名称）。 */
    public static String tag(Integer role) {
        if (role == null) return "";
        switch (role) {
            case ADMIN:   return "(管理员)";
            case TEACHER: return "(教师)";
            case STUDENT: return "(学员)";
            case SOCIAL:  return "(社会)";
            default:      return "";
        }
    }
}
