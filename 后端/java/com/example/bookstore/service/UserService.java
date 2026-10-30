package com.example.bookstore.service;

import com.example.bookstore.dao.UserDAO;
import com.example.bookstore.entity.User;

import java.util.List;

public class UserService {
    
    private UserDAO userDAO = new UserDAO();
    
    public User login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return null;
        }
        return userDAO.login(username, password);
    }
    
    public User findById(Integer id) {
        if (id == null || id <= 0) {
            return null;
        }
        return userDAO.findById(id);
    }
    
    public List<User> findAll() {
        return userDAO.findAll();
    }
    
    public boolean insert(User user) {
        if (user == null || user.getUsername() == null || user.getPassword() == null || user.getName() == null) {
            return false;
        }
        if (userDAO.existsByUsername(user.getUsername())) {
            return false;
        }
        return userDAO.insert(user) > 0;
    }
    
    public boolean update(User user) {
        if (user == null || user.getId() == null || user.getId() <= 0) {
            return false;
        }
        return userDAO.update(user) > 0;
    }
    
    public boolean delete(Integer id) {
        if (id == null || id <= 0) {
            return false;
        }
        return userDAO.delete(id) > 0;
    }

    public boolean updatePerms(Integer id, long perms) {
        if (id == null || id <= 0) {
            return false;
        }
        return userDAO.updatePerms(id, perms) > 0;
    }

    public boolean updateStatus(Integer id, int status) {
        if (id == null || id <= 0) {
            return false;
        }
        return userDAO.updateStatus(id, status) > 0;
    }

    public boolean updateTier(Integer id, int tierId) {
        if (id == null || id <= 0) {
            return false;
        }
        return userDAO.updateTier(id, tierId) > 0;
    }
}