package com.library.server.net;

import com.library.client.net.DiscoveryClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DiscoveryNetTest {

    private DiscoveryServer discoveryServer;

    @BeforeEach
    void setUp() {
        discoveryServer = new DiscoveryServer(8888, "SmartLib Test Server");
        discoveryServer.start();
    }

    @AfterEach
    void tearDown() {
        if (discoveryServer != null) {
            discoveryServer.stop();
        }
    }

    @Test
    void testDiscoveryServerRespondsToClient() {
        // Cho server chút thời gian để bind socket
        try {
            Thread.sleep(150);
        } catch (InterruptedException ignored) {}

        DiscoveryClient.DiscoveredServer result = DiscoveryClient.discoverServer(1500);

        assertNotNull(result, "Phải tìm thấy Server qua UDP Broadcast");
        assertEquals(8888, result.getPort());
        assertTrue(result.getServerName().contains("SmartLib"), "Tên server phải chính xác");
        assertNotNull(result.getHost(), "Host IP không được rỗng");
    }
}

