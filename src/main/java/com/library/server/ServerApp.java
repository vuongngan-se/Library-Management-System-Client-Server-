package com.library.server;

import com.library.server.config.DatabaseConfig;
import com.library.server.net.ClientHandler;
import com.library.server.service.LibraryService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerApp {
    private static final int PORT = 8888;
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        System.out.println("=== HỆ THỐNG MÁY CHỦ QUẢN LÝ THƯ VIỆN (TCP SERVER) ===");
        
        // Connect Database
        String jdbcUrl = System.getProperty("db.url", "jdbc:mysql://localhost:3306/library_db?useSSL=false&serverTimezone=UTC");
        String user = System.getProperty("db.user", "root");
        String pass = System.getProperty("db.pass", "123456");

        DatabaseConfig.init(jdbcUrl, user, pass);
        LibraryService service = new LibraryService();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server đã sẵn sàng và đang lắng nghe tại cổng TCP: " + PORT);
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(new ClientHandler(clientSocket, service));
            }
        } catch (IOException e) {
            System.err.println("Lỗi vận hành ServerSocket: " + e.getMessage());
        } finally {
            threadPool.shutdown();
        }
    }
}
