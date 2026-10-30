package com.example.bookstore.entity;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class Borrow {

    private Integer id;
    private Integer userId;
    private Integer bookId;
    private Date borrowDate;
    private Date returnDate;
    private Integer status;
    private Timestamp createTime;
    private Timestamp updateTime;
    private String username;
    private String bookTitle;

    // V2 新增字段
    private Date dueDate;
    private BigDecimal fine;
    private Integer paid;
    private Integer overdue;
    private Integer renewed;

    public Borrow() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getBookId() { return bookId; }
    public void setBookId(Integer bookId) { this.bookId = bookId; }
    public Date getBorrowDate() { return borrowDate; }
    public void setBorrowDate(Date borrowDate) { this.borrowDate = borrowDate; }
    public Date getReturnDate() { return returnDate; }
    public void setReturnDate(Date returnDate) { this.returnDate = returnDate; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Timestamp getCreateTime() { return createTime; }
    public void setCreateTime(Timestamp createTime) { this.createTime = createTime; }
    public Timestamp getUpdateTime() { return updateTime; }
    public void setUpdateTime(Timestamp updateTime) { this.updateTime = updateTime; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }
    public BigDecimal getFine() { return fine; }
    public void setFine(BigDecimal fine) { this.fine = fine; }
    public Integer getPaid() { return paid; }
    public void setPaid(Integer paid) { this.paid = paid; }
    public Integer getOverdue() { return overdue; }
    public void setOverdue(Integer overdue) { this.overdue = overdue; }
    public Integer getRenewed() { return renewed; }
    public void setRenewed(Integer renewed) { this.renewed = renewed; }
}
