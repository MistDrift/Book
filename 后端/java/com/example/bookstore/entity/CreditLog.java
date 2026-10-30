package com.example.bookstore.entity;

import java.sql.Timestamp;

public class CreditLog {

    public Integer id;
    public Integer userId;
    public Integer delta;
    public Integer beforeCredit;
    public Integer afterCredit;
    public String reason;
    public Integer opBy;
    public String refType;
    public Integer refId;
    public Timestamp createTime;

    // 关联查询回填
    public String username;

    public CreditLog() {}
}
