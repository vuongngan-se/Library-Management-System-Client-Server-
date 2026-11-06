package com.library.server.net;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.common.model.Book;
import com.library.common.model.Borrowing;
import com.library.common.model.Reader;
import com.library.common.protocol.NetworkUtils;
import com.library.server.dao.InMemoryStore;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Embedded HTTP Server (Cổng 8080): Cung cấp Mini Web Portal cho điện thoại di động
 * và REST API tra cứu dữ liệu thư viện mà không cần cài ứng dụng Client.
 */
public class EmbeddedHttpServer {
    public static final int HTTP_PORT = 8080;
    private static final ObjectMapper mapper = NetworkUtils.getMapper();
    private HttpServer server;
    private volatile boolean running = false;

    public synchronized void start() {
        if (running) return;
        try {
            server = HttpServer.create(new InetSocketAddress(HTTP_PORT), 0);
            server.setExecutor(Executors.newFixedThreadPool(6));

            // Đăng ký các Web Handlers
            server.createContext("/", new RootWebHandler());
            server.createContext("/api/stats", new StatsApiHandler());
            server.createContext("/api/books", new BooksApiHandler());
            server.createContext("/api/sessions", new SessionsApiHandler());

            server.start();
            running = true;
            System.out.println("[HTTP Web Server] Đã kích hoạt Cổng Web Di Động tại http://localhost:" + HTTP_PORT + " (Cổng HTTP: " + HTTP_PORT + ")");
        } catch (IOException e) {
            System.err.println("[HTTP Web Server] Không thể khởi động cổng HTTP " + HTTP_PORT + ": " + e.getMessage());
        }
    }

    public synchronized void stop() {
        if (!running || server == null) return;
        server.stop(1);
        running = false;
        System.out.println("[HTTP Web Server] Đã dừng Cổng Web Di Động.");
    }

    public static String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) continue;
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (!addr.isLoopbackAddress() && addr.getHostAddress().indexOf(':') == -1) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) {}
        return "127.0.0.1";
    }

    // ==========================================
    // HANDLERS
    // ==========================================

    /**
     * Handler trả về trang Web HTML5 Responsive dành cho Mobile & Desktop
     */
    private static class RootWebHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (!"/".equals(path) && !"/index.html".equals(path)) {
                sendResponse(exchange, 404, "text/plain", "404 Not Found");
                return;
            }

            String html = generateMobilePortalHtml();
            sendResponse(exchange, 200, "text/html; charset=UTF-8", html);
        }
    }

    /**
     * REST API trả về JSON thống kê
     */
    private static class StatsApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");

            List<Book> books = InMemoryStore.getAllBooks();
            List<Reader> readers = InMemoryStore.getAllReaders();
            List<Borrowing> borrowings = InMemoryStore.getAllBorrowings();

            int totalBooks = books.stream().mapToInt(Book::getQuantity).sum();
            int availableBooks = books.stream().mapToInt(Book::getAvailableQuantity).sum();
            long activeBorrowings = borrowings.stream().filter(b -> "BORROWED".equals(b.getStatus())).count();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalBooks", totalBooks);
            stats.put("availableBooks", availableBooks);
            stats.put("totalReaders", readers.size());
            stats.put("activeBorrowings", activeBorrowings);
            stats.put("activeSockets", ClientSessionManager.getActiveSessionCount());
            stats.put("serverTime", LocalDate.now().toString());

            String json = mapper.writeValueAsString(stats);
            sendResponse(exchange, 200, "application/json; charset=UTF-8", json);
        }
    }

    /**
     * REST API tìm kiếm và lấy danh sách sách
     */
    private static class BooksApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");

            String query = exchange.getRequestURI().getQuery();
            String search = "";
            if (query != null && query.contains("search=")) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length == 2 && "search".equals(pair[0])) {
                        try {
                            search = java.net.URLDecoder.decode(pair[1], StandardCharsets.UTF_8).trim().toLowerCase();
                        } catch (Exception ignored) {}
                    }
                }
            }

            List<Book> list = InMemoryStore.getAllBooks();
            if (!search.isEmpty()) {
                final String q = search;
                list = list.stream().filter(b ->
                        (b.getTitle() != null && b.getTitle().toLowerCase().contains(q)) ||
                        (b.getAuthor() != null && b.getAuthor().toLowerCase().contains(q)) ||
                        (b.getCategoryName() != null && b.getCategoryName().toLowerCase().contains(q))
                ).collect(Collectors.toList());
            }

            String json = mapper.writeValueAsString(list);
            sendResponse(exchange, 200, "application/json; charset=UTF-8", json);
        }
    }

    /**
     * REST API lấy danh sách phiên socket
     */
    private static class SessionsApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");

            Map<String, Object> data = new HashMap<>();
            data.put("activeCount", ClientSessionManager.getActiveSessionCount());
            data.put("totalRequests", ClientSessionManager.getTotalRequests());
            data.put("sessions", ClientSessionManager.getAllSessions());

            String json = mapper.writeValueAsString(data);
            sendResponse(exchange, 200, "application/json; charset=UTF-8", json);
        }
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    /**
     * Sinh mã HTML/CSS/JS cho trang Web Portal Di Động
     */
    private static String generateMobilePortalHtml() {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"vi\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "  <title>SmartLib Mobile Portal - Quản Lý Thư Viện</title>\n" +
                "  <style>\n" +
                "    * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; }\n" +
                "    body { background-color: #f1f5f9; color: #1e293b; padding-bottom: 40px; }\n" +
                "    header { background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%); color: white; padding: 24px 16px; text-align: center; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }\n" +
                "    header h1 { font-size: 22px; margin-bottom: 6px; }\n" +
                "    header p { font-size: 13px; opacity: 0.9; }\n" +
                "    .container { max-width: 800px; margin: 0 auto; padding: 16px; }\n" +
                "    .metrics-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; margin-bottom: 20px; }\n" +
                "    .metric-card { background: white; border-radius: 12px; padding: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); text-align: center; border-left: 4px solid #3b82f6; }\n" +
                "    .metric-card:nth-child(2) { border-left-color: #10b981; }\n" +
                "    .metric-card:nth-child(3) { border-left-color: #f59e0b; }\n" +
                "    .metric-card:nth-child(4) { border-left-color: #8b5cf6; }\n" +
                "    .metric-val { font-size: 24px; font-weight: bold; color: #0f172a; margin-top: 4px; }\n" +
                "    .metric-lbl { font-size: 12px; color: #64748b; font-weight: 600; text-transform: uppercase; }\n" +
                "    .search-box { position: relative; margin-bottom: 20px; }\n" +
                "    .search-box input { width: 100%; padding: 14px 18px; border-radius: 30px; border: 1px solid #cbd5e1; font-size: 15px; outline: none; box-shadow: 0 2px 6px rgba(0,0,0,0.04); transition: border-color 0.2s; }\n" +
                "    .search-box input:focus { border-color: #2563eb; }\n" +
                "    .section-title { font-size: 16px; font-weight: bold; color: #334155; margin-bottom: 12px; display: flex; justify-content: space-between; align-items: center; }\n" +
                "    .book-list { display: flex; flex-direction: column; gap: 12px; }\n" +
                "    .book-card { background: white; border-radius: 12px; padding: 14px 16px; box-shadow: 0 2px 6px rgba(0,0,0,0.04); display: flex; gap: 14px; align-items: center; }\n" +
                "    .book-icon { width: 44px; height: 44px; border-radius: 8px; background: #e0f2fe; color: #0284c7; display: flex; align-items: center; justify-content: center; font-size: 22px; flex-shrink: 0; }\n" +
                "    .book-info { flex: 1; min-width: 0; }\n" +
                "    .book-title { font-size: 15px; font-weight: 600; color: #0f172a; margin-bottom: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }\n" +
                "    .book-author { font-size: 13px; color: #64748b; margin-bottom: 4px; }\n" +
                "    .book-badge { display: inline-block; padding: 2px 8px; border-radius: 12px; font-size: 11px; font-weight: 600; }\n" +
                "    .badge-avail { background: #dcfce7; color: #166534; }\n" +
                "    .badge-empty { background: #fee2e2; color: #991b1b; }\n" +
                "    .footer-note { text-align: center; font-size: 12px; color: #94a3b8; margin-top: 24px; }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <header>\n" +
                "    <h1>📚 SMARTLIB MOBILE PORTAL</h1>\n" +
                "    <p>Hệ thống Thư viện Client-Server • Cổng Tra Cứu Di Động</p>\n" +
                "  </header>\n" +
                "  <div class=\"container\">\n" +
                "    <div class=\"metrics-grid\">\n" +
                "      <div class=\"metric-card\"><div class=\"metric-lbl\">Tổng Bản Sách</div><div class=\"metric-val\" id=\"mTotalBooks\">--</div></div>\n" +
                "      <div class=\"metric-card\"><div class=\"metric-lbl\">Sách Khả Dụng</div><div class=\"metric-val\" id=\"mAvailBooks\">--</div></div>\n" +
                "      <div class=\"metric-card\"><div class=\"metric-lbl\">Độc Giả Đăng Ký</div><div class=\"metric-val\" id=\"mReaders\">--</div></div>\n" +
                "      <div class=\"metric-card\"><div class=\"metric-lbl\">Socket Đang Online</div><div class=\"metric-val\" id=\"mSockets\">--</div></div>\n" +
                "    </div>\n" +
                "    <div class=\"search-box\">\n" +
                "      <input type=\"text\" id=\"txtSearch\" placeholder=\"🔍 Nhập tên sách, tác giả hoặc thể loại...\" oninput=\"filterBooks()\">\n" +
                "    </div>\n" +
                "    <div class=\"section-title\"><span>📖 Danh Mục Sách Thư Viện</span><span id=\"lblBookCount\" style=\"font-size: 13px; font-weight: normal; color: #64748b;\">0 cuốn</span></div>\n" +
                "    <div class=\"book-list\" id=\"bookList\"></div>\n" +
                "    <div class=\"footer-note\">Trang Web được cấp phát trực tiếp từ máy chủ Java Socket &amp; Embedded HTTP Server (Cổng 8080).</div>\n" +
                "  </div>\n" +
                "  <script>\n" +
                "    let allBooks = [];\n" +
                "    async function loadStats() {\n" +
                "      try {\n" +
                "        const res = await fetch('/api/stats');\n" +
                "        const data = await res.json();\n" +
                "        document.getElementById('mTotalBooks').innerText = data.totalBooks || 0;\n" +
                "        document.getElementById('mAvailBooks').innerText = data.availableBooks || 0;\n" +
                "        document.getElementById('mReaders').innerText = data.totalReaders || 0;\n" +
                "        document.getElementById('mSockets').innerText = data.activeSockets || 0;\n" +
                "      } catch(e){ console.error(e); }\n" +
                "    }\n" +
                "    async function loadBooks() {\n" +
                "      try {\n" +
                "        const res = await fetch('/api/books');\n" +
                "        allBooks = await res.json();\n" +
                "        renderBooks(allBooks);\n" +
                "      } catch(e){ console.error(e); }\n" +
                "    }\n" +
                "    function renderBooks(list) {\n" +
                "      const el = document.getElementById('bookList');\n" +
                "      document.getElementById('lblBookCount').innerText = list.length + ' đầu sách';\n" +
                "      if(list.length === 0) {\n" +
                "        el.innerHTML = '<div style=\"text-align:center; padding: 30px; color: #94a3b8;\">Không tìm thấy sách nào phù hợp.</div>';\n" +
                "        return;\n" +
                "      }\n" +
                "      el.innerHTML = list.map(b => {\n" +
                "        const avail = b.availableQuantity > 0;\n" +
                "        const badge = avail ? `<span class=\"book-badge badge-avail\">Còn ${b.availableQuantity}/${b.quantity} bản</span>` : '<span class=\"book-badge badge-empty\">Đã hết</span>';\n" +
                "        return `<div class=\"book-card\">\n" +
                "          <div class=\"book-icon\">📖</div>\n" +
                "          <div class=\"book-info\">\n" +
                "            <div class=\"book-title\" title=\"${b.title}\">${b.title}</div>\n" +
                "            <div class=\"book-author\">Tác giả: ${b.author || 'N/A'} • ${b.categoryName || 'Chung'}</div>\n" +
                "            <div>${badge}</div>\n" +
                "          </div>\n" +
                "        </div>`;\n" +
                "      }).join('');\n" +
                "    }\n" +
                "    function filterBooks() {\n" +
                "      const q = document.getElementById('txtSearch').value.toLowerCase().trim();\n" +
                "      if(!q) { renderBooks(allBooks); return; }\n" +
                "      const filtered = allBooks.filter(b => \n" +
                "        (b.title && b.title.toLowerCase().includes(q)) ||\n" +
                "        (b.author && b.author.toLowerCase().includes(q)) ||\n" +
                "        (b.categoryName && b.categoryName.toLowerCase().includes(q))\n" +
                "      );\n" +
                "      renderBooks(filtered);\n" +
                "    }\n" +
                "    loadStats();\n" +
                "    loadBooks();\n" +
                "    setInterval(loadStats, 5000);\n" +
                "  </script>\n" +
                "</body>\n" +
                "</html>";
    }
}

