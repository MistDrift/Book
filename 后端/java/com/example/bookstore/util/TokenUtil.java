package com.example.bookstore.util;

import com.example.bookstore.entity.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TokenUtil {

    private static final Map<String, User> TOKENS = new ConcurrentHashMap<>();

    public static String create(User user) {
        String token = UUID.randomUUID().toString();
        TOKENS.put(token, user);
        return token;
    }

    public static User get(String token) {
        return token == null ? null : TOKENS.get(token);
    }

    public static void remove(String token) {
        if (token != null) {
            TOKENS.remove(token);
        }
    }

    public static User currentUser(HttpServletRequest req) {
        String auth = req.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            User user = get(auth.substring(7).trim());
            if (user != null) {
                return user;
            }
        }
        HttpSession session = req.getSession(false);
        return session != null ? (User) session.getAttribute("user") : null;
    }
}
