package com.example.bookstore.entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Suggestion {

    public Integer id;
    public Integer userId;
    public Integer type;
    public String title;
    public String author;
    public String publisher;
    public String isbn;
    public String category;
    public BigDecimal price;
    public String reason;
    public Integer status;
    public Integer votes;
    public Integer handleBy;
    public String handleRemark;
    public Timestamp handleTime;
    public Timestamp createTime;

    // 关联查询回填
    public String username;

    public Suggestion() {}
}
