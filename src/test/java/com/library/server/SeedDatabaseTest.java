package com.library.server;

import com.library.server.config.DatabaseConfig;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;

public class SeedDatabaseTest {

    @Test
    public void testConnectAndSeed() {
        System.out.println("Kiểm tra kết nối MySQL...");
        DatabaseConfig.init("jdbc:mysql://localhost:3306/library_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8", "root", "123456");
        System.out.println("Chế độ InMemory? " + DatabaseConfig.isInMemoryMode());
        if (!DatabaseConfig.isInMemoryMode()) {
            try (Connection conn = DatabaseConfig.getConnection()) {
                System.out.println("Kết nối MySQL thành công! Đang thực thi seed schema.sql...");
                String sql = new String(Files.readAllBytes(Paths.get("sql/schema.sql")), "UTF-8");
                String[] statements = sql.split(";\\r?\\n");
                Statement stmt = conn.createStatement();
                for (String s : statements) {
                    s = s.trim();
                    if (!s.isEmpty() && !s.startsWith("--")) {
                        try {
                            stmt.execute(s);
                        } catch (Exception ex) {
                            System.err.println("Lỗi khi chạy lệnh: " + ex.getMessage());
                        }
                    }
                }
                System.out.println("Hoàn tất nạp seed dữ liệu vào MySQL!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
