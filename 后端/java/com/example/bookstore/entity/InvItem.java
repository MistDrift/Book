package com.example.bookstore.entity;

import java.sql.Timestamp;

public class InvItem {

    public Integer id;
    public Integer invId;
    public Integer bookId;
    public String isbn;
    public String title;
    public Integer sysStatus;
    public Integer realStatus;
    public String remark;
    public Integer checkedBy;
    public Timestamp checkedTime;

    public InvItem() {}
}
