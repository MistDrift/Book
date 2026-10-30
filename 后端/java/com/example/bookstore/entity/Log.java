package com.example.bookstore.entity;

import java.sql.Timestamp;

public class Log {
    private Long id;
    private Integer userId;
    private String username;
    private String action;
    private String target;
    private Integer targetId;
    private String detail;
    private String ip;
    private String ua;
    private Integer ok;
    private Timestamp createTime;

    public Log() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
    public Integer getTargetId() { return targetId; }
    public void setTargetId(Integer targetId) { this.targetId = targetId; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public String getUa() { return ua; }
    public void setUa(String ua) { this.ua = ua; }
    public Integer getOk() { return ok; }
    public void setOk(Integer ok) { this.ok = ok; }
    public Timestamp getCreateTime() { return createTime; }
    public void setCreateTime(Timestamp createTime) { this.createTime = createTime; }
}
