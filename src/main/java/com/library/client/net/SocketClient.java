package com.library.client.net;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.common.protocol.CryptoUtils;
import com.library.common.protocol.NetworkUtils;
import com.library.common.protocol.Request;
import com.library.common.protocol.Response;

import javax.crypto.SecretKey;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.PublicKey;

public class SocketClient {
    private static SocketClient instance;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String serverHost = "localhost";
    private int serverPort = 8888;
    private boolean connected = false;

    // Chế độ Mã hóa Đường truyền Socket (Hybrid RSA-2048 + AES-256)
    private boolean encryptionEnabled = false;
    private SecretKey aesKey = null;

    private SocketClient() {}

    public static synchronized SocketClient getInstance() {
        if (instance == null) {
            instance = new SocketClient();
        }
        return instance;
    }

    public synchronized boolean connect(String host, int port) {
        this.serverHost = host;
        this.serverPort = port;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            socket = new Socket(serverHost, serverPort);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            connected = true;
            return true;
        } catch (IOException e) {
            connected = false;
            return false;
        }
    }

    public boolean isEncryptionEnabled() {
        return encryptionEnabled && aesKey != null;
    }

    /**
     * Bắt tay trao đổi khóa bí mật RSA-2048 + AES-256 để kích hoạt mã hóa đường truyền.
     */
    public synchronized boolean enableEncryption() throws IOException {
        if (!connected || socket == null || socket.isClosed()) {
            boolean ok = connect(serverHost, serverPort);
            if (!ok) {
                throw new IOException("Không thể kết nối đến Máy chủ để bắt tay bảo mật.");
            }
        }
        try {
            // Bước 1: Xin RSA Public Key của Server
            Request initReq = new Request("HANDSHAKE_INIT", null);
            String initJson = NetworkUtils.getMapper().writeValueAsString(initReq);
            PacketInspector.getInstance().recordTcpOut(serverHost, serverPort, "HANDSHAKE_INIT (RSA-2048)", initJson);
            NetworkUtils.sendJson(dos, initReq);

            Response initResp = NetworkUtils.readJson(dis, Response.class);
            String initRespJson = NetworkUtils.getMapper().writeValueAsString(initResp);
            PacketInspector.getInstance().recordTcpIn(serverHost, serverPort, "HANDSHAKE_RESP (RSA_PUBLIC_KEY)", initRespJson);

            if (!"SUCCESS".equals(initResp.getStatus()) || initResp.getData() == null) {
                throw new IOException("Máy chủ từ chối cấp RSA Public Key: " + initResp.getMessage());
            }

            String serverPubKeyBase64 = String.valueOf(initResp.getData());
            PublicKey serverPubKey = CryptoUtils.decodeRsaPublicKey(serverPubKeyBase64);

            // Bước 2: Sinh khóa phiên đối xứng AES-256 ngẫu nhiên
            SecretKey newAesKey = CryptoUtils.generateAesKey();

            // Bước 3: Mã hóa khóa AES bằng RSA Public Key của Server
            String encAesBase64 = CryptoUtils.encryptWithRsa(newAesKey.getEncoded(), serverPubKey);

            // Bước 4: Gửi khóa AES đã mã hóa về Server
            Request exchangeReq = new Request("HANDSHAKE_EXCHANGE", encAesBase64);
            String exJson = NetworkUtils.getMapper().writeValueAsString(exchangeReq);
            PacketInspector.getInstance().recordTcpOut(serverHost, serverPort, "HANDSHAKE_EXCHANGE (AES-256 Key)", exJson);
            NetworkUtils.sendJson(dos, exchangeReq);

            Response exchangeResp = NetworkUtils.readJson(dis, Response.class);
            String exRespJson = NetworkUtils.getMapper().writeValueAsString(exchangeResp);
            PacketInspector.getInstance().recordTcpIn(serverHost, serverPort, "HANDSHAKE_ESTABLISHED", exRespJson);

            if ("SUCCESS".equals(exchangeResp.getStatus())) {
                this.aesKey = newAesKey;
                this.encryptionEnabled = true;
                System.out.println("[SocketClient] Bắt tay thành công. Toàn bộ gói tin Socket từ giờ sẽ được mã hóa AES-256!");
                return true;
            } else {
                throw new IOException("Máy chủ không chấp nhận bắt tay AES: " + exchangeResp.getMessage());
            }
        } catch (Exception e) {
            this.encryptionEnabled = false;
            this.aesKey = null;
            throw new IOException("Lỗi trong quá trình bắt tay RSA/AES: " + e.getMessage(), e);
        }
    }

    /**
     * Tắt chế độ mã hóa, quay lại gửi tin Plaintext.
     */
    public synchronized void disableEncryption() {
        this.encryptionEnabled = false;
        this.aesKey = null;
        System.out.println("[SocketClient] Đã tắt mã hóa socket. Chế độ Plaintext được kích hoạt.");
    }

    public synchronized Response sendRequest(Request req) throws IOException {
        if (!connected || socket == null || socket.isClosed()) {
            boolean ok = connect(serverHost, serverPort);
            if (!ok) {
                throw new IOException("Không thể kết nối đến Máy chủ " + serverHost + ":" + serverPort);
            }
            if (encryptionEnabled && aesKey != null) {
                enableEncryption();
            }
        }

        // Nếu chế độ mã hóa đang BẬT (trừ các lệnh Handshake)
        if (encryptionEnabled && aesKey != null && !"HANDSHAKE_INIT".equals(req.getAction()) && !"HANDSHAKE_EXCHANGE".equals(req.getAction())) {
            try {
                String innerReqJson = NetworkUtils.getMapper().writeValueAsString(req);
                String cipherText = CryptoUtils.encryptAes(innerReqJson, aesKey);
                Request outerReq = new Request("ENCRYPTED", cipherText);

                // Ghi nhận gói tin mã hóa ra Packet Inspector
                String wireReqJson = NetworkUtils.getMapper().writeValueAsString(outerReq);
                PacketInspector.getInstance().recordTcpOut(serverHost, serverPort, "🔒 ENCRYPTED [" + req.getAction() + "]", wireReqJson);

                NetworkUtils.sendJson(dos, outerReq);
                Response outerResp = NetworkUtils.readJson(dis, Response.class);

                String wireRespJson = NetworkUtils.getMapper().writeValueAsString(outerResp);
                PacketInspector.getInstance().recordTcpIn(serverHost, serverPort, "🔒 ENCRYPTED [" + outerResp.getStatus() + "]", wireRespJson);

                // Giải mã dữ liệu phản hồi
                if ("ENCRYPTED".equals(outerResp.getMessage()) && outerResp.getData() != null) {
                    String decryptedJson = CryptoUtils.decryptAes(outerResp.getData().toString(), aesKey);
                    return NetworkUtils.getMapper().readValue(decryptedJson, Response.class);
                }
                return outerResp;
            } catch (Exception e) {
                throw new IOException("Lỗi xử lý mã hóa đường truyền: " + e.getMessage(), e);
            }
        }

        // Gửi gói tin TCP dạng Plaintext thông thường
        try {
            String reqJson = NetworkUtils.getMapper().writeValueAsString(req);
            PacketInspector.getInstance().recordTcpOut(serverHost, serverPort, req.getAction(), reqJson);
        } catch (Exception ignored) {}

        NetworkUtils.sendJson(dos, req);
        Response resp = NetworkUtils.readJson(dis, Response.class);

        // Ghi nhận gói tin TCP nhận về (RX) cho Packet Inspector
        try {
            String respJson = NetworkUtils.getMapper().writeValueAsString(resp);
            PacketInspector.getInstance().recordTcpIn(serverHost, serverPort, resp.getStatus(), respJson);
        } catch (Exception ignored) {}

        return resp;
    }

    /**
     * Đo độ trễ mạng khứ hồi (Round-Trip Time - RTT) tới Server bằng gói tin PING/PONG.
     * @return Thời gian khứ hồi tính bằng mili-giây (ms).
     */
    public synchronized long measurePing() throws IOException {
        long start = System.currentTimeMillis();
        java.util.Map<String, Object> pingData = new java.util.HashMap<>();
        pingData.put("clientTime", start);
        Response resp = sendRequest(new Request("PING", pingData));
        if ("SUCCESS".equals(resp.getStatus())) {
            return System.currentTimeMillis() - start;
        }
        throw new IOException("Máy chủ không phản hồi PONG");
    }

    public <T> T convertData(Object data, Class<T> clazz) {
        return NetworkUtils.getMapper().convertValue(data, clazz);
    }

    public <T> T convertData(Object data, TypeReference<T> typeRef) {
        return NetworkUtils.getMapper().convertValue(data, typeRef);
    }

    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }

    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
        connected = false;
    }

    public String getServerHost() {
        return serverHost;
    }

    public int getServerPort() {
        return serverPort;
    }
}
