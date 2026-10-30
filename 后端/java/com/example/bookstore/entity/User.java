package com.example.bookstore.entity;

import java.sql.Timestamp;

public class User {

    private Integer id;
    private String username;
    private String password;
    private String name;
    private Integer role;
    private String email;
    private String phone;
    private Timestamp createTime;
    private Timestamp updateTime;

    // V2 新增字段
    private Integer tierId;
    private Integer credit;
    private Long perms;
    private Integer status;

    // 关联查询回填
    private String tierName;

    public User() {}

    public User(String username, String password, String name, Integer role) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getRole() { return role; }
    public void setRole(Integer role) { this.role = role; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Timestamp getCreateTime() { return createTime; }
    public void setCreateTime(Timestamp createTime) { this.createTime = createTime; }
    public Timestamp getUpdateTime() { return updateTime; }
    public void setUpdateTime(Timestamp updateTime) { this.updateTime = updateTime; }

    public Integer getTierId() { return tierId; }
    public void setTierId(Integer tierId) { this.tierId = tierId; }
    public Integer getCredit() { return credit; }
    public void setCredit(Integer credit) { this.credit = credit; }
    public Long getPerms() { return perms; }
    public void setPerms(Long perms) { this.perms = perms; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getTierName() { return tierName; }
    public void setTierName(String tierName) { this.tierName = tierName; }
}
