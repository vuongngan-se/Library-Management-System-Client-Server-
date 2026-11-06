package com.library.server;

import com.library.server.config.DatabaseConfig;
import com.library.server.net.ClientHandler;
import com.library.server.net.DiscoveryServer;
import com.library.server.net.EmbeddedHttpServer;
import com.library.server.service.LibraryService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerApp {
    private static final int PORT = 8888;
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();
    private static DiscoveryServer discoveryServer;
    private static EmbeddedHttpServer httpServer;

    public static void main(String[] args) {
        System.out.println("=== HỆ THỐNG MÁY CHỦ QUẢN LÝ THƯ VIỆN (TCP SERVER) ===");
        
        // Connect Database
        String jdbcUrl = System.getProperty("db.url", "jdbc:mysql://localhost:3306/library_db?useSSL=false&serverTimezone=UTC");
        String user = System.getProperty("db.user", "root");
        String pass = System.getProperty("db.pass", "123456");

        DatabaseConfig.init(jdbcUrl, user, pass);
        LibraryService service = new LibraryService();

        // 1. Kích hoạt dịch vụ UDP Discovery tự động dò tìm máy chủ trong mạng LAN (Cổng UDP 8889)
        discoveryServer = new DiscoveryServer(PORT, "SmartLib Server");
        discoveryServer.start();

        // 2. Kích hoạt Mini Web Portal cho Điện thoại di động & REST API (Cổng HTTP 8080)
        httpServer = new EmbeddedHttpServer();
        httpServer.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (discoveryServer != null) {
                discoveryServer.stop();
            }
            if (httpServer != null) {
                httpServer.stop();
            }
        }));

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server đã sẵn sàng và đang lắng nghe tại cổng TCP: " + PORT);
            System.out.println(">> Web Portal di động: http://" + EmbeddedHttpServer.getLocalIpAddress() + ":8080");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(new ClientHandler(clientSocket, service));
            }
        } catch (IOException e) {
            System.err.println("Lỗi vận hành ServerSocket: " + e.getMessage());
        } finally {
            if (discoveryServer != null) {
                discoveryServer.stop();
            }
            if (httpServer != null) {
                httpServer.stop();
            }
            threadPool.shutdown();
        }
    }
}
