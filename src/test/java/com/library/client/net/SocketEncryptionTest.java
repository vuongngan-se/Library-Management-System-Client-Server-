package com.library.client.net;

import com.library.common.protocol.Request;
import com.library.common.protocol.Response;
import com.library.server.net.ClientHandler;
import com.library.server.service.LibraryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

public class SocketEncryptionTest {

    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private int testPort;
    private SocketClient client;

    @BeforeEach
    void setUp() throws IOException {
        serverSocket = new ServerSocket(0); // Cổng ngẫu nhiên
        testPort = serverSocket.getLocalPort();
        threadPool = Executors.newCachedThreadPool();

        LibraryService service = new LibraryService();
        threadPool.execute(() -> {
            try {
                while (!serverSocket.isClosed()) {
                    Socket sock = serverSocket.accept();
                    threadPool.execute(new ClientHandler(sock, service));
                }
            } catch (IOException ignored) {}
        });

        client = SocketClient.getInstance();
        client.disconnect();
        client.disableEncryption();
    }

    @AfterEach
    void tearDown() {
        if (client != null) {
            client.disableEncryption();
            client.disconnect();
        }
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {}
        }
        if (threadPool != null) {
            threadPool.shutdownNow();
        }
    }

    @Test
    void testPlainAndEncryptedSocketCommunication() throws Exception {
        boolean connected = client.connect("127.0.0.1", testPort);
        assertTrue(connected, "Client phải kết nối thành công tới Server");

        // 1. Gửi request dạng Plaintext trước khi bật mã hóa
        assertFalse(client.isEncryptionEnabled());
        Response plainResp = client.sendRequest(new Request("PING", null));
        assertEquals("SUCCESS", plainResp.getStatus());

        // 2. Kích hoạt quy trình Handshake RSA-2048 + AES-256
        boolean handshakeOk = client.enableEncryption();
        assertTrue(handshakeOk, "Quy trình Bắt tay Handshake phải thành công");
        assertTrue(client.isEncryptionEnabled(), "Chế độ mã hóa phải ở trạng thái BẬT");

        // 3. Gửi request khi đang bật mã hóa (dữ liệu bay qua socket là Ciphertext)
        Response encResp = client.sendRequest(new Request("PING", null));
        assertEquals("SUCCESS", encResp.getStatus());
        assertEquals("PONG", encResp.getMessage());

        // 4. Kiểm tra Packet Inspector đã ghi lại gói tin ENCRYPTED
        boolean hasEncryptedPacket = PacketInspector.getInstance().getInternalList().stream()
                .anyMatch(p -> p.getSummary().contains("ENCRYPTED"));
        assertTrue(hasEncryptedPacket, "Packet Inspector phải ghi nhận được gói tin có gắn cờ ENCRYPTED");

        // 5. Thử tắt mã hóa, trở về Plaintext
        client.disableEncryption();
        assertFalse(client.isEncryptionEnabled());
        Response backToPlainResp = client.sendRequest(new Request("PING", null));
        assertEquals("SUCCESS", backToPlainResp.getStatus());
    }
}
