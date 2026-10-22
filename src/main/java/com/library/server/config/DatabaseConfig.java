package com.library.server.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConfig {
    private static HikariDataSource dataSource;
    private static boolean inMemoryMode = false;

    public static void init(String url, String user, String password) {
        if (tryInit(url, user, password)) {
            return;
        }
        // If password was provided but failed, also try empty password ""
        if (password != null && !password.isEmpty()) {
            System.out.println("Thử kết nối MySQL với mật khẩu trống (empty password)...");
            if (tryInit(url, user, "")) {
                return;
            }
        }
        System.err.println("Không thể kết nối cơ sở dữ liệu MySQL. Chuyển sang chế độ In-Memory Mode.");
        inMemoryMode = true;
    }

    private static boolean tryInit(String url, String user, String password) {
        HikariDataSource ds = null;
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(user);
            config.setPassword(password);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(30000);
            config.setConnectionTimeout(3000);

            ds = new HikariDataSource(config);
            try (Connection conn = ds.getConnection()) {
                System.out.println("Kết nối cơ sở dữ liệu MySQL thành công (user: " + user + ").");
                dataSource = ds;
                inMemoryMode = false;
                return true;
            }
        } catch (Exception e) {
            System.err.println("Kết nối MySQL thất bại: " + e.getMessage());
            if (ds != null) {
                try {
                    ds.close();
                } catch (Exception ignored) {}
            }
            return false;
        }
    }

    public static Connection getConnection() throws SQLException {
        if (inMemoryMode || dataSource == null) {
            return null;
        }
        return dataSource.getConnection();
    }

    public static boolean isInMemoryMode() {
        return inMemoryMode;
    }
}
