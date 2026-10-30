package com.example.bookstore.entity;

import java.sql.Timestamp;

public class Notice {

    public Integer id;
    public Integer userId;
    public Integer type;
    public String title;
    public String content;
    public String refType;
    public Integer refId;
    public Integer readFlag;
    public Timestamp createTime;

    public Notice() {}
}
