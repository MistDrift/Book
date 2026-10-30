package com.example.bookstore.entity;

import java.sql.Timestamp;

public class Inventory {

    public Integer id;
    public String name;
    public Integer type;
    public String area;
    public Integer total;
    public Integer actual;
    public Integer missing;
    public Integer extra;
    public Integer status;
    public Timestamp startTime;
    public Timestamp endTime;
    public Integer creator;
    public Timestamp createTime;

    // 关联查询回填
    public String creatorName;

    public Inventory() {}
}
