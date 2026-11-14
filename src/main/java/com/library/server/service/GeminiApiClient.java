package com.library.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

/**
 * Client kết nối Google Gemini API (REST v1beta).
 * Hỗ trợ các model hiện đại: gemini-1.5-flash, gemini-2.0-flash.
 * Cung cấp cơ chế tự động tìm nạp API Key từ:
 *  1. Biến nhớ runtime (setApiKey)
 *  2. File cấu hình gemini.key tại thư mục gốc
 *  3. Biến môi trường GEMINI_API_KEY
 *  4. System property: -Dgemini.api.key
 */
public class GeminiApiClient {

    private static final String[] CANDIDATE_MODELS = {
            "gemini-3.5-flash-lite",
            "gemini-3.5-flash",
            "gemini-2.5-flash",
            "gemini-1.5-flash"
    };
    private static final String KEY_FILE = "gemini.key";
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static String cachedApiKey = null;

    /**
     * Lấy API Key hiện hành.
     */
    public static synchronized String getApiKey() {
        if (cachedApiKey != null && !cachedApiKey.trim().isEmpty()) {
            return cachedApiKey.trim();
        }

        // 1. Đọc từ file gemini.key hoặc gemeni.key tại thư mục chạy
        for (String fileName : new String[]{"gemini.key", "gemeni.key"}) {
            try {
                File file = new File(fileName);
                if (file.exists() && file.isFile()) {
                    String content = Files.readString(file.toPath(), StandardCharsets.UTF_8).trim();
                    if (!content.isEmpty()) {
                        cachedApiKey = content;
                        return cachedApiKey;
                    }
                }
            } catch (Exception ignored) {}
        }

        // 2. Đọc từ System Property
        String propKey = System.getProperty("gemini.api.key");
        if (propKey != null && !propKey.trim().isEmpty()) {
            cachedApiKey = propKey.trim();
            return cachedApiKey;
        }

        // 3. Đọc từ Environment Variable
        String envKey = System.getenv("GEMINI_API_KEY");
        if (envKey != null && !envKey.trim().isEmpty()) {
            cachedApiKey = envKey.trim();
            return cachedApiKey;
        }

        return null;
    }

    /**
     * Cập nhật API Key mới và ghi đè vào file gemini.key (và gemeni.key)
     */
    public static synchronized boolean setApiKey(String key) {
        if (key == null) key = "";
        key = key.trim();
        cachedApiKey = key;
        try {
            if (key.isEmpty()) {
                File f1 = new File("gemini.key");
                if (f1.exists()) f1.delete();
                File f2 = new File("gemeni.key");
                if (f2.exists()) f2.delete();
            } else {
                Files.writeString(Paths.get("gemini.key"), key, StandardCharsets.UTF_8);
                Files.writeString(Paths.get("gemeni.key"), key, StandardCharsets.UTF_8);
            }
            return true;
        } catch (IOException e) {
            System.err.println("[GeminiApiClient] Không thể ghi file gemini.key: " + e.getMessage());
            return false;
        }
    }

    /**
     * Kiểm tra xem Gemini API đã được cấu hình key hay chưa
     */
    public static boolean isConfigured() {
        String key = getApiKey();
        return key != null && !key.trim().isEmpty();
    }

    /**
     * Gửi yêu cầu sinh nội dung tới Gemini API với System Context và Prompt người dùng.
     * @param prompt Câu hỏi hoặc yêu cầu từ người dùng
     * @param systemContext Ngữ cảnh hệ thống (Dữ liệu CSDL sách thực tế + nguyên tắc phản hồi)
     * @return Câu trả lời từ Gemini hoặc null nếu gọi thất bại
     */
    public static String generateContent(String prompt, String systemContext) {
        String apiKey = getApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return null;
        }

        for (String model : CANDIDATE_MODELS) {
            String res = callGeminiEndpoint(model, apiKey, prompt, systemContext);
            if (res != null && !res.isBlank()) {
                return res.trim();
            }
        }
        return null;
    }

    private static String callGeminiEndpoint(String model, String apiKey, String prompt, String systemContext) {
        try {
            String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;

            // Xây dựng JSON Payload
            ObjectNode root = mapper.createObjectNode();

            // 1. System Instruction
            if (systemContext != null && !systemContext.trim().isEmpty()) {
                ObjectNode sysInstruction = mapper.createObjectNode();
                ArrayNode sysParts = mapper.createArrayNode();
                ObjectNode sysTextPart = mapper.createObjectNode();
                sysTextPart.put("text", systemContext);
                sysParts.add(sysTextPart);
                sysInstruction.set("parts", sysParts);
                root.set("systemInstruction", sysInstruction);
            }

            // 2. Contents
            ArrayNode contents = mapper.createArrayNode();
            ObjectNode userContent = mapper.createObjectNode();
            userContent.put("role", "user");
            ArrayNode userParts = mapper.createArrayNode();
            ObjectNode textPart = mapper.createObjectNode();
            textPart.put("text", prompt);
            userParts.add(textPart);
            userContent.set("parts", userParts);
            contents.add(userContent);
            root.set("contents", contents);

            // 3. Generation Config
            ObjectNode genConfig = mapper.createObjectNode();
            genConfig.put("temperature", 0.7);
            genConfig.put("maxOutputTokens", 1200);
            root.set("generationConfig", genConfig);

            String requestBody = mapper.writeValueAsString(root);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .timeout(Duration.ofSeconds(12))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() == 200) {
                JsonNode resJson = mapper.readTree(response.body());
                JsonNode candidates = resJson.path("candidates");
                if (candidates.isArray() && candidates.size() > 0) {
                    JsonNode parts = candidates.get(0).path("content").path("parts");
                    if (parts.isArray() && parts.size() > 0) {
                        String text = parts.get(0).path("text").asText();
                        if (text != null && !text.isBlank()) {
                            return text.trim();
                        }
                    }
                }
            } else {
                System.err.println("[GeminiApiClient] Lỗi HTTP " + response.statusCode() + " từ model " + model + ": " + response.body());
            }
        } catch (Exception e) {
            System.err.println("[GeminiApiClient] Lỗi khi gọi Gemini (" + model + "): " + e.getMessage());
        }
        return null;
    }
}
