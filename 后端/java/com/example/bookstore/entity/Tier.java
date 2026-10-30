package com.example.bookstore.entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Tier {
    private Integer id;
    private String name;
    private Integer minScore;
    private Integer maxBorrow;
    private Integer days;
    private Integer renewMax;
    private Integer renewDays;
    private Integer priority;
    private BigDecimal rate;
    private String note;
    private Timestamp createTime;

    public Tier() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getMinScore() { return minScore; }
    public void setMinScore(Integer minScore) { this.minScore = minScore; }
    public Integer getMaxBorrow() { return maxBorrow; }
    public void setMaxBorrow(Integer maxBorrow) { this.maxBorrow = maxBorrow; }
    public Integer getDays() { return days; }
    public void setDays(Integer days) { this.days = days; }
    public Integer getRenewMax() { return renewMax; }
    public void setRenewMax(Integer renewMax) { this.renewMax = renewMax; }
    public Integer getRenewDays() { return renewDays; }
    public void setRenewDays(Integer renewDays) { this.renewDays = renewDays; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Timestamp getCreateTime() { return createTime; }
    public void setCreateTime(Timestamp createTime) { this.createTime = createTime; }
}
