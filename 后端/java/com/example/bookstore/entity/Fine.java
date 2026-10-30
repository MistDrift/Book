package com.example.bookstore.entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Fine {

    public Integer id;
    public Integer borrowId;
    public Integer userId;
    public BigDecimal amount;
    public Integer overdueDays;
    public BigDecimal rate;
    public String reason;
    public Integer payStatus;
    public Timestamp payTime;
    public String waiveReason;
    public Integer waiveBy;
    public Timestamp createTime;

    // 关联查询回填
    public String username;
    public String bookTitle;

    public Fine() {}
}
