package com.library.server.net;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class EmbeddedHttpServerTest {

    private EmbeddedHttpServer httpServer;
    private HttpClient httpClient;

    @BeforeEach
    void setUp() {
        httpServer = new EmbeddedHttpServer();
        httpServer.start();
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    @AfterEach
    void tearDown() {
        if (httpServer != null) {
            httpServer.stop();
        }
    }

    @Test
    void testRootWebPortalEndpoint() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("SMARTLIB MOBILE PORTAL"), "Trang Web phải chứa tiêu đề SMARTLIB");
        assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("text/html") || response.body().contains("<html"), "Content-type hoặc body phải là HTML");
    }

    @Test
    void testApiStatsEndpoint() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/stats"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("totalBooks"), "API Stats phải chứa trường totalBooks");
        assertTrue(response.body().contains("availableBooks"), "API Stats phải chứa trường availableBooks");
    }

    @Test
    void testApiBooksEndpoint() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/books?search=Java"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Java"), "API Books phải lọc ra sách chứa từ khóa Java");
    }
}

