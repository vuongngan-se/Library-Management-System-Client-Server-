package com.library.server.net;

import com.library.common.model.User;
import com.library.common.protocol.CryptoUtils;
import com.library.common.protocol.NetworkUtils;
import com.library.common.protocol.Request;
import com.library.common.protocol.Response;
import com.library.server.service.LibraryService;

import javax.crypto.SecretKey;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.UUID;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final LibraryService service;
    private final String clientAddress;
    private final String sessionId;
    private SecretKey sessionAesKey; // Khóa phiên đối xứng AES-256 sau khi Handshake RSA

    // Rate Limiting Window Tracking
    private long windowStartMillis = System.currentTimeMillis();
    private int requestCountInWindow = 0;

    public ClientHandler(Socket socket, LibraryService service) {
        this.socket = socket;
        this.service = service;
        this.clientAddress = socket.getRemoteSocketAddress() != null ? socket.getRemoteSocketAddress().toString() : "Unknown";
        this.sessionId = "SES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public String getSessionId() {
        return sessionId;
    }

    public void disconnect() {
        try {
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {}
    }

    @Override
    public void run() {
        String ip = "127.0.0.1";
        int port = 0;
        if (socket.getRemoteSocketAddress() instanceof InetSocketAddress) {
            InetSocketAddress isa = (InetSocketAddress) socket.getRemoteSocketAddress();
            ip = isa.getAddress() != null ? isa.getAddress().getHostAddress() : isa.getHostString();
            port = isa.getPort();
        }

        ClientSessionManager.register(sessionId, ip, port, this);
        System.out.println("Client đã kết nối [" + sessionId + "]: " + clientAddress);

        try (
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream())
        ) {
            while (!socket.isClosed()) {
                Request request = NetworkUtils.readJson(dis, Request.class);
                if (request == null) break;
                System.out.println("[" + sessionId + " - " + clientAddress + "] Lệnh nhận được: " + request.getAction());
                ClientSessionManager.recordRequest(sessionId, request.getAction());

                // Xử lý Bắt tay Mã hóa (Hybrid Cryptography Handshake)
                if ("HANDSHAKE_INIT".equals(request.getAction())) {
                    String pubKey = CryptoUtils.getServerPublicKeyBase64();
                    Response resp = Response.success("RSA_PUBLIC_KEY", pubKey);
                    NetworkUtils.sendJson(dos, resp);
                    continue;
                }

                if ("HANDSHAKE_EXCHANGE".equals(request.getAction())) {
                    try {
                        String encAes = String.valueOf(request.getData());
                        this.sessionAesKey = CryptoUtils.decryptAesKeyWithServerRsa(encAes);
                        System.out.println("[" + sessionId + "] Bắt tay RSA thành công! Kênh truyền bảo mật AES-256 đã kích hoạt.");
                        Response resp = Response.success("AES_SESSION_ESTABLISHED", "AES-256");
                        NetworkUtils.sendJson(dos, resp);
                    } catch (Exception e) {
                        System.err.println("[" + sessionId + "] Lỗi giải mã khóa AES: " + e.getMessage());
                        Response err = Response.error("Bắt tay trao đổi khóa thất bại: " + e.getMessage());
                        NetworkUtils.sendJson(dos, err);
                    }
                    continue;
                }

                // Kiểm tra gói tin mã hóa Envelope
                boolean wasEncrypted = false;
                if ("ENCRYPTED".equals(request.getAction())) {
                    if (sessionAesKey == null) {
                        Response err = Response.error("Chưa hoàn tất bắt tay (Handshake). Vui lòng trao đổi khóa RSA trước khi gửi gói tin mã hóa.");
                        NetworkUtils.sendJson(dos, err);
                        continue;
                    }
                    try {
                        String cipherText = String.valueOf(request.getData());
                        String plainJson = CryptoUtils.decryptAes(cipherText, sessionAesKey);
                        request = NetworkUtils.getMapper().readValue(plainJson, Request.class);
                        wasEncrypted = true;
                    } catch (Exception e) {
                        System.err.println("[" + sessionId + "] Lỗi giải mã gói tin: " + e.getMessage());
                        Response err = Response.error("Không thể giải mã gói tin: " + e.getMessage());
                        NetworkUtils.sendJson(dos, err);
                        continue;
                    }
                }

                // 1. Kiểm tra Rate Limiting & DoS Protection (Chống spam request)
                if (NetworkSimulationConfig.isRateLimitEnabled()) {
                    long now = System.currentTimeMillis();
                    if (now - windowStartMillis > 1000) {
                        windowStartMillis = now;
                        requestCountInWindow = 0;
                    }
                    requestCountInWindow++;
                    if (requestCountInWindow > NetworkSimulationConfig.getMaxRequestsPerSecond()) {
                        System.err.println("[SECURITY ALERT] Chặn Request Flooding từ " + clientAddress + " (" + requestCountInWindow + " req/s)!");
                        Response blockedResp = Response.error(
                                "🛡️ [SECURITY DEFENSE] Tần suất gửi yêu cầu quá nhanh (" + requestCountInWindow + " req/giây > " +
                                        NetworkSimulationConfig.getMaxRequestsPerSecond() + " req/s). Yêu cầu đã bị hệ thống phòng thủ DoS chặn tạm thời!"
                        );
                        if (wasEncrypted) {
                            try {
                                String bJson = NetworkUtils.getMapper().writeValueAsString(blockedResp);
                                String enc = CryptoUtils.encryptAes(bJson, sessionAesKey);
                                NetworkUtils.sendJson(dos, new Response("ERROR", "ENCRYPTED", enc));
                            } catch (Exception ex) {
                                NetworkUtils.sendJson(dos, blockedResp);
                            }
                        } else {
                            NetworkUtils.sendJson(dos, blockedResp);
                        }
                        continue;
                    }
                }

                // 2. Mô phỏng độ trễ mạng (Network Latency & Jitter Simulation)
                int latency = NetworkSimulationConfig.getSimulatedLatencyMs();
                if (latency > 0) {
                    if (NetworkSimulationConfig.isJitterEnabled()) {
                        int jitter = (int) ((Math.random() * 40) - 20); // Dao động ±20ms
                        latency = Math.max(0, latency + jitter);
                    }
                    try {
                        Thread.sleep(latency);
                    } catch (InterruptedException ignored) {}
                }

                Response response = service.handleRequest(request);

                // If login success, update user info in session
                if ("LOGIN".equals(request.getAction()) && "SUCCESS".equals(response.getStatus()) && response.getData() != null) {
                    try {
                        User user = NetworkUtils.getMapper().convertValue(response.getData(), User.class);
                        if (user != null) {
                            ClientSessionManager.updateUser(sessionId, user.getUsername(), user.getFullName(), user.getRole());
                        }
                    } catch (Exception ignored) {}
                }

                if ("REGISTER_CHAT_LISTENER".equals(request.getAction())) {
                    String username = request.getData() != null ? request.getData().toString() : "Anonymous";
                    com.library.common.model.ClientSession session = ClientSessionManager.getSession(sessionId);
                    String role = session != null ? session.getRole() : "STAFF";
                    if ("ADMIN".equalsIgnoreCase(role) || "STAFF".equalsIgnoreCase(role)) {
                        ChatManager.registerListener(sessionId, username, dos);
                        ClientSessionManager.updateUser(sessionId, username, "Chat Listener (" + username + ")", role);
                    }
                }

                // Phản hồi kết quả: Nếu Request đến được mã hóa thì Response gửi về cũng được mã hóa AES
                if (wasEncrypted) {
                    try {
                        String respJson = NetworkUtils.getMapper().writeValueAsString(response);
                        String encResp = CryptoUtils.encryptAes(respJson, sessionAesKey);
                        Response encEnvelope = new Response(response.getStatus(), "ENCRYPTED", encResp);
                        NetworkUtils.sendJson(dos, encEnvelope);
                    } catch (Exception e) {
                        System.err.println("[" + sessionId + "] Lỗi mã hóa phản hồi: " + e.getMessage());
                        NetworkUtils.sendJson(dos, Response.error("Lỗi mã hóa phản hồi"));
                    }
                } else {
                    NetworkUtils.sendJson(dos, response);
                }
            }
        } catch (IOException e) {
            System.out.println("Client ngắt kết nối [" + sessionId + "]: " + clientAddress);
        } finally {
            ChatManager.unregisterListener(sessionId);
            ClientSessionManager.unregister(sessionId);
            disconnect();
        }
    }
}
