package com.example.bookstore.entity;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class Book {

    private Integer id;
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private Date publishDate;
    private String category;
    private BigDecimal price;
    private Integer stock;
    private String description;
    private String image;
    private Timestamp createTime;
    private Timestamp updateTime;

    // V2 字段
    private String location;
    private Integer collStatus;

    public Book() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public Date getPublishDate() { return publishDate; }
    public void setPublishDate(Date publishDate) { this.publishDate = publishDate; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public Timestamp getCreateTime() { return createTime; }
    public void setCreateTime(Timestamp createTime) { this.createTime = createTime; }
    public Timestamp getUpdateTime() { return updateTime; }
    public void setUpdateTime(Timestamp updateTime) { this.updateTime = updateTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Integer getCollStatus() { return collStatus; }
    public void setCollStatus(Integer collStatus) { this.collStatus = collStatus; }
}
