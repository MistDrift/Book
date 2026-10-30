package com.example.bookstore.entity;

import java.sql.Timestamp;

public class Reserve {

    public Integer id;
    public Integer userId;
    public Integer bookId;
    public Integer status;
    public Timestamp reserveTime;
    public Timestamp notifyTime;
    public Timestamp deadline;

    // 关联查询回填
    public String username;
    public String bookTitle;
    public String tierName;
    public Integer tierPriority;

    public Reserve() {}
}
