package com.library.server.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class GeminiApiClientTest {

    private static final String TEST_KEY_FILE = "gemini.key";
    private String originalKey;

    @BeforeEach
    void setUp() {
        originalKey = GeminiApiClient.getApiKey();
        GeminiApiClient.setApiKey("");
    }

    @AfterEach
    void tearDown() {
        if (originalKey != null && !originalKey.isEmpty()) {
            GeminiApiClient.setApiKey(originalKey);
        } else {
            GeminiApiClient.setApiKey("");
            File f = new File(TEST_KEY_FILE);
            if (f.exists()) {
                f.delete();
            }
        }
    }

    @Test
    void testSetAndGetApiKey() {
        assertFalse(GeminiApiClient.isConfigured(), "Ban đầu khi key rỗng thì isConfigured phải là false");

        boolean ok = GeminiApiClient.setApiKey("AIzaSyTestMockKey12345");
        assertTrue(ok);
        assertTrue(GeminiApiClient.isConfigured());
        assertEquals("AIzaSyTestMockKey12345", GeminiApiClient.getApiKey());
    }

    @Test
    void testGenerateContentGracefulHandling() {
        GeminiApiClient.setApiKey("");
        assertNull(GeminiApiClient.generateContent("Test prompt", "Test context"),
                "Khi chưa có API Key thì generateContent phải trả về null an toàn");
    }

    @Test
    void testAiChatbotFallbackWhenGeminiFails() {
        // Cấu hình một fake key không kết nối được
        GeminiApiClient.setApiKey("AIzaSyFakeKeyInvalidNetwork");
        
        // Gọi câu hỏi sách qua AiChatbotService -> Phải tự động fallback sang Local Engine
        String answer = AiChatbotService.answerQuery("Thư viện còn sách Clean Code không?");
        assertNotNull(answer);
        assertTrue(answer.contains("Clean Code"), "Phải fallback trả về kết quả từ Local NLP Engine");
    }
}
