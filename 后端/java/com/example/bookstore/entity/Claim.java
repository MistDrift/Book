package com.example.bookstore.entity;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class Claim {

    public Integer id;
    public Integer borrowId;
    public Integer userId;
    public Integer bookId;
    public Date reportDate;
    public Integer type;
    public BigDecimal bookPrice;
    public BigDecimal depRate;
    public BigDecimal amount;
    public BigDecimal paid;
    public Integer status;
    public Integer handleBy;
    public String handleRemark;
    public Timestamp payTime;
    public Timestamp createTime;

    // 关联查询回填
    public String username;
    public String bookTitle;

    public Claim() {}
}
