package com.library.client.net;

import com.library.common.model.NetworkPacket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class PacketInspectorTest {

    private PacketInspector inspector;

    @BeforeEach
    void setUp() {
        inspector = PacketInspector.getInstance();
        inspector.setPaused(false);
        inspector.clear();
    }

    @Test
    void testRecordTcpPacket() {
        inspector.recordTcpOut("127.0.0.1", 8888, "LOGIN", "{\"username\":\"admin\"}");
        inspector.recordTcpIn("127.0.0.1", 8888, "SUCCESS", "{\"status\":\"SUCCESS\"}");

        assertEquals(2, inspector.getInternalList().size());
        NetworkPacket first = inspector.getInternalList().get(0);
        assertEquals("TCP", first.getProtocol());
        assertTrue(first.getSummary().contains("LOGIN"));
        assertEquals("TX ➔", first.getDirection());

        NetworkPacket second = inspector.getInternalList().get(1);
        assertEquals("TCP", second.getProtocol());
        assertEquals("RX ⬅", second.getDirection());
    }

    @Test
    void testPauseAndClear() {
        inspector.recordUdpOut("255.255.255.255:8889", "DISCOVER_LIBRARY_SERVER");
        assertEquals(1, inspector.getInternalList().size());

        inspector.setPaused(true);
        inspector.recordTcpOut("127.0.0.1", 8888, "PING", "{}");
        assertEquals(1, inspector.getInternalList().size(), "Khi tạm dừng, gói tin mới không được ghi");

        inspector.setPaused(false);
        inspector.clear();
        assertEquals(0, inspector.getInternalList().size(), "Sau khi clear, danh sách phải rỗng");
    }

    @Test
    void testPacketListener() {
        AtomicBoolean received = new AtomicBoolean(false);
        inspector.addPacketListener(pkt -> {
            if ("HTTP".equals(pkt.getProtocol())) {
                received.set(true);
            }
        });

        inspector.recordHttp("GET", "http://localhost:8080/api/books", 200, "[]");
        assertTrue(received.get(), "Listener phải nhận được sự kiện gói tin HTTP");
    }
}
