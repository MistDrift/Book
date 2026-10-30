package com.example.bookstore.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@WebServlet("/api/upload")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 10,       // 10MB
    maxRequestSize = 1024 * 1024 * 50      // 50MB
)
public class UploadServlet extends HttpServlet {
    
    private ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            Part filePart = request.getPart("file");
            if (filePart == null || filePart.getSize() == 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write(objectMapper.writeValueAsString(createResult(false, "请选择要上传的文件")));
                return;
            }
            
            String fileName = filePart.getSubmittedFileName();
            String extension = fileName.substring(fileName.lastIndexOf("."));
            
            if (!extension.toLowerCase().matches("\\.(jpg|jpeg|png|gif|webp)")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write(objectMapper.writeValueAsString(createResult(false, "只支持图片格式：jpg, jpeg, png, gif, webp")));
                return;
            }
            
            String newFileName = UUID.randomUUID().toString() + extension;
            String uploadPath = getServletContext().getRealPath("/upload");
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            File saveFile = new File(uploadDir, newFileName);
            filePart.write(saveFile.getAbsolutePath());
            
            String fileUrl = "/upload/" + newFileName;
            out.write(objectMapper.writeValueAsString(createResult(true, "上传成功", fileUrl)));
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(objectMapper.writeValueAsString(createResult(false, "上传失败：" + e.getMessage())));
        }
        
        out.flush();
        out.close();
    }
    
    private Map<String, Object> createResult(boolean success, String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", message);
        return result;
    }
    
    private Map<String, Object> createResult(boolean success, String message, String url) {
        Map<String, Object> result = createResult(success, message);
        result.put("url", url);
        return result;
    }
}