package com.library.server.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AiChatbotServiceTest {

    @Test
    void testGreetingAndHelpQuery() {
        String resp1 = AiChatbotService.answerQuery("Xin chào trợ lý AI!");
        assertNotNull(resp1);
        assertTrue(resp1.contains("Trợ Lý AI") || resp1.contains("SmartLib"));

        String resp2 = AiChatbotService.answerQuery("Bạn làm được gì?");
        assertTrue(resp2.contains("Tra cứu sách") || resp2.contains("Kiểm tra tồn kho"));
    }

    @Test
    void testSearchSpecificBookTitle() {
        String resp = AiChatbotService.answerQuery("Thư viện có sách Clean Code không?");
        assertNotNull(resp);
        assertTrue(resp.contains("Clean Code"));
        assertTrue(resp.contains("Robert C. Martin"));
        assertTrue(resp.contains("Tình trạng:") || resp.contains("CÒN SÁCH"));
    }

    @Test
    void testCheckAvailabilityQuery() {
        String resp = AiChatbotService.answerQuery("Sách Lập trình Java Socket còn bao nhiêu cuốn?");
        assertNotNull(resp);
        assertTrue(resp.contains("Java Socket"));
        assertTrue(resp.contains("Khả dụng:") || resp.contains("cuốn"));
    }

    @Test
    void testSearchByAuthor() {
        String resp = AiChatbotService.answerQuery("Sách của tác giả Robert C. Martin");
        assertNotNull(resp);
        assertTrue(resp.contains("Robert C. Martin"));
        assertTrue(resp.contains("Clean Code"));
    }

    @Test
    void testSearchByCategory() {
        String resp = AiChatbotService.answerQuery("Có sách nào về Trí tuệ nhân tạo không?");
        assertNotNull(resp);
        assertTrue(resp.contains("Trí tuệ nhân tạo") || resp.contains("AI"));
    }

    @Test
    void testLibraryRulesQuery() {
        String resp = AiChatbotService.answerQuery("Thời hạn mượn và tiền phạt trễ là bao nhiêu?");
        assertNotNull(resp);
        assertTrue(resp.contains("14 ngày"));
        assertTrue(resp.contains("5.000 VNĐ"));
    }

    @Test
    void testNonExistentBookQuery() {
        String resp = AiChatbotService.answerQuery("Thư viện có cuốn Bí Kíp Võ Công Thiên Hạ Đệ Nhất không?");
        assertNotNull(resp);
        assertTrue(resp.contains("chưa tìm thấy") || resp.contains("Rất tiếc"));
    }
}
