package com.example.bookstore.servlet;

import com.example.bookstore.dao.BorrowDAO;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.User;
import com.example.bookstore.service.BookService;
import com.example.bookstore.util.PermUtil;
import com.example.bookstore.util.Roles;
import com.example.bookstore.util.TokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/book/*")
public class BookServlet extends HttpServlet {

    private BookService bookService = new BookService();
    private BorrowDAO borrowDAO = new BorrowDAO();
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        String pathInfo = request.getPathInfo();
        PrintWriter out = response.getWriter();

        User user = TokenUtil.currentUser(request);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "未登录");
            out.write(objectMapper.writeValueAsString(result));
            out.flush();
            out.close();
            return;
        }

        if (pathInfo == null || pathInfo.equals("/")) {
            String keyword = request.getParameter("keyword");
            String category = request.getParameter("category");

            List<Book> books;
            if (keyword != null && !keyword.trim().isEmpty()) {
                books = bookService.search(keyword);
            } else if (category != null && !category.trim().isEmpty()) {
                books = bookService.findByCategory(category);
            } else {
                books = bookService.findAll();
            }

            out.write(objectMapper.writeValueAsString(books));
        } else if (pathInfo.matches("/\\d+")) {
            try {
                Integer id = Integer.parseInt(pathInfo.substring(1));
                Book book = bookService.findById(id);
                if (book != null) {
                    out.write(objectMapper.writeValueAsString(book));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", false);
                    result.put("message", "图书不存在");
                    out.write(objectMapper.writeValueAsString(result));
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "无效的图书ID");
                out.write(objectMapper.writeValueAsString(result));
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "无效的请求路径");
            out.write(objectMapper.writeValueAsString(result));
        }

        out.flush();
        out.close();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> result = new HashMap<>();
        PrintWriter out = response.getWriter();

        User user = TokenUtil.currentUser(request);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            result.put("success", false);
            result.put("message", "未登录");
            out.write(objectMapper.writeValueAsString(result));
            out.flush();
            out.close();
            return;
        }
        if (user.getRole() == null || user.getRole() != Roles.ADMIN || !PermUtil.hasPerm(user, "book.edit")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            result.put("success", false);
            result.put("message", "无权限");
            out.write(objectMapper.writeValueAsString(result));
            out.flush();
            out.close();
            return;
        }

        try {
            String isbn = request.getParameter("isbn");
            String title = request.getParameter("title");
            String author = request.getParameter("author");
            String publisher = request.getParameter("publisher");
            String publishDate = request.getParameter("publishDate");
            String category = request.getParameter("category");
            String price = request.getParameter("price");
            String stock = request.getParameter("stock");
            String description = request.getParameter("description");
            String image = request.getParameter("image");
            String location = request.getParameter("location");
            String collStatus = request.getParameter("collStatus");

            Book book = new Book();
            book.setIsbn(isbn);
            book.setTitle(title);
            book.setAuthor(author);
            book.setPublisher(publisher);
            book.setPublishDate(Date.valueOf(publishDate));
            book.setCategory(category);
            book.setPrice(new BigDecimal(price));
            book.setStock(Integer.parseInt(stock));
            book.setDescription(description);
            book.setImage(image);
            book.setLocation(location);
            if (collStatus != null && !collStatus.isEmpty()) book.setCollStatus(Integer.parseInt(collStatus));
            
            if (bookService.insert(book)) {
                result.put("success", true);
                result.put("message", "添加成功");
            } else {
                result.put("success", false);
                result.put("message", "添加失败，ISBN可能已存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "添加失败：" + e.getMessage());
        }

        out.write(objectMapper.writeValueAsString(result));
        out.flush();
        out.close();
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> result = new HashMap<>();
        PrintWriter out = response.getWriter();

        User user = TokenUtil.currentUser(request);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            result.put("success", false);
            result.put("message", "未登录");
            out.write(objectMapper.writeValueAsString(result));
            out.flush();
            out.close();
            return;
        }
        if (user.getRole() == null || user.getRole() != Roles.ADMIN || !PermUtil.hasPerm(user, "book.edit")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            result.put("success", false);
            result.put("message", "无权限");
            out.write(objectMapper.writeValueAsString(result));
            out.flush();
            out.close();
            return;
        }

        try {
            String id = request.getParameter("id");
            String isbn = request.getParameter("isbn");
            String title = request.getParameter("title");
            String author = request.getParameter("author");
            String publisher = request.getParameter("publisher");
            String publishDate = request.getParameter("publishDate");
            String category = request.getParameter("category");
            String price = request.getParameter("price");
            String stock = request.getParameter("stock");
            String description = request.getParameter("description");
            String image = request.getParameter("image");
            String location = request.getParameter("location");
            String collStatus = request.getParameter("collStatus");

            Book book = new Book();
            book.setId(Integer.parseInt(id));
            book.setIsbn(isbn);
            book.setTitle(title);
            book.setAuthor(author);
            book.setPublisher(publisher);
            book.setPublishDate(Date.valueOf(publishDate));
            book.setCategory(category);
            book.setPrice(new BigDecimal(price));
            book.setStock(Integer.parseInt(stock));
            book.setDescription(description);
            book.setImage(image);
            book.setLocation(location);
            if (collStatus != null && !collStatus.isEmpty()) book.setCollStatus(Integer.parseInt(collStatus));
            
            if (bookService.update(book)) {
                result.put("success", true);
                result.put("message", "更新成功");
            } else {
                result.put("success", false);
                result.put("message", "更新失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败：" + e.getMessage());
        }

        out.write(objectMapper.writeValueAsString(result));
        out.flush();
        out.close();
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> result = new HashMap<>();
        PrintWriter out = response.getWriter();

        User user = TokenUtil.currentUser(request);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            result.put("success", false);
            result.put("message", "未登录");
            out.write(objectMapper.writeValueAsString(result));
            out.flush();
            out.close();
            return;
        }
        if (user.getRole() == null || user.getRole() != Roles.ADMIN || !PermUtil.hasPerm(user, "book.delete")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            result.put("success", false);
            result.put("message", "无权限");
            out.write(objectMapper.writeValueAsString(result));
            out.flush();
            out.close();
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || !pathInfo.matches("/\\d+")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.put("success", false);
            result.put("message", "无效的请求路径");
            out.write(objectMapper.writeValueAsString(result));
            out.flush();
            out.close();
            return;
        }

        try {
            Integer id = Integer.parseInt(pathInfo.substring(1));

            Book book = bookService.findById(id);
            if (book == null) {
                result.put("success", false);
                result.put("message", "图书不存在");
                out.write(objectMapper.writeValueAsString(result));
                out.flush();
                out.close();
                return;
            }

            int borrowingCount = borrowDAO.countBorrowingByBookId(id);
            if (borrowingCount > 0) {
                result.put("success", false);
                result.put("message", "该书有" + borrowingCount + "条未归还的借阅记录，无法删除");
                out.write(objectMapper.writeValueAsString(result));
                out.flush();
                out.close();
                return;
            }

            if (bookService.delete(id)) {
                result.put("success", true);
                result.put("message", "删除成功");
            } else {
                result.put("success", false);
                result.put("message", "删除失败");
            }
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "无效的图书ID");
        }

        out.write(objectMapper.writeValueAsString(result));
        out.flush();
        out.close();
    }
}