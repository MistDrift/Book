package com.example.bookstore.service;

import com.example.bookstore.dao.BookDAO;
import com.example.bookstore.dao.BorrowDAO;
import com.example.bookstore.entity.Book;

import java.util.List;

public class BookService {
    
    private BookDAO bookDAO = new BookDAO();
    private BorrowDAO borrowDAO = new BorrowDAO();

    public List<Book> findAll() {
        return bookDAO.findAll();
    }

    public Book findById(Integer id) {
        if (id == null || id <= 0) {
            return null;
        }
        return bookDAO.findById(id);
    }

    public Book findByIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return null;
        }
        return bookDAO.findByIsbn(isbn);
    }

    public List<Book> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        return bookDAO.search(keyword);
    }

    public List<Book> findByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return findAll();
        }
        return bookDAO.findByCategory(category);
    }

    public boolean insert(Book book) {
        if (book == null || book.getIsbn() == null || book.getTitle() == null ||
            book.getAuthor() == null || book.getPublisher() == null ||
            book.getPublishDate() == null || book.getCategory() == null ||
            book.getPrice() == null || book.getStock() == null) {
            return false;
        }
        if (bookDAO.existsByIsbn(book.getIsbn())) {
            return false;
        }
        return bookDAO.insert(book) > 0;
    }

    public boolean update(Book book) {
        if (book == null || book.getId() == null || book.getId() <= 0) {
            return false;
        }
        return bookDAO.update(book) > 0;
    }

    public boolean delete(Integer id) {
        if (id == null || id <= 0) {
            return false;
        }
        if (bookDAO.findById(id) == null) {
            return false;
        }
        int borrowingCount = borrowDAO.countBorrowingByBookId(id);
        if (borrowingCount > 0) {
            return false;
        }
        return bookDAO.delete(id) > 0;
    }

    public boolean deleteWithBorrowRecords(Integer id) {
        if (id == null || id <= 0) {
            return false;
        }
        if (bookDAO.findById(id) == null) {
            return false;
        }
        borrowDAO.deleteByBookId(id);
        return bookDAO.delete(id) > 0;
    }

    public boolean updateStock(Integer id, Integer delta) {
        if (id == null || id <= 0 || delta == null) {
            return false;
        }
        Book book = bookDAO.findById(id);
        if (book == null || book.getStock() + delta < 0) {
            return false;
        }
        return bookDAO.updateStock(id, delta) > 0;
    }
}