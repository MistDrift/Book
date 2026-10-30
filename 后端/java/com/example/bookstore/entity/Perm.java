package com.example.bookstore.entity;

import java.sql.Timestamp;

public class Perm {
    private Integer id;
    private String code;
    private String name;
    private String grp;
    private Integer pos;
    private String note;
    private Timestamp createTime;

    public Perm() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGrp() { return grp; }
    public void setGrp(String grp) { this.grp = grp; }
    public Integer getPos() { return pos; }
    public void setPos(Integer pos) { this.pos = pos; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Timestamp getCreateTime() { return createTime; }
    public void setCreateTime(Timestamp createTime) { this.createTime = createTime; }
}
