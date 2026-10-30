import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.sun.net.httpserver.*;

public class SimpleHttpServer {
    private static final String WEB_ROOT = "src/main/webapp";
    
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        server.createContext("/", new FileHandler());
        
        server.setExecutor(null);
        server.start();
        
        System.out.println("Server started on http://localhost:8080");
        System.out.println("Press Enter to stop...");
        System.in.read();
        server.stop(0);
    }
    
    static class FileHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }
            
            Path filePath = Paths.get(WEB_ROOT, path);
            
            if (!Files.exists(filePath)) {
                String response = "404 Not Found";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }
            
            byte[] content = Files.readAllBytes(filePath);
            String contentType = getContentType(path);
            
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, content.length);
            OutputStream os = exchange.getResponseBody();
            os.write(content);
            os.close();
        }
        
        private String getContentType(String path) {
            if (path.endsWith(".html")) return "text/html;charset=UTF-8";
            if (path.endsWith(".css")) return "text/css";
            if (path.endsWith(".js")) return "application/javascript";
            if (path.endsWith(".json")) return "application/json";
            return "text/plain";
        }
    }
}
