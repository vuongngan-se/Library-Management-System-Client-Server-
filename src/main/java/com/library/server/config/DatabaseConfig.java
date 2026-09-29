package com.library.server.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConfig {
    private static HikariDataSource dataSource;
    private static boolean inMemoryMode = false;

    public static void init(String url, String user, String password) {
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

            dataSource = new HikariDataSource(config);
            // Test connection
            try (Connection conn = dataSource.getConnection()) {
                System.out.println("MySQL database connected successfully.");
            }
        } catch (Exception e) {
            System.err.println("Failed to connect to MySQL database (" + e.getMessage() + "). Falling back to memory mode for server logic.");
            inMemoryMode = true;
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
