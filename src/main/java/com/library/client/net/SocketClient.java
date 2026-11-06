package com.library.client.net;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.common.protocol.NetworkUtils;
import com.library.common.protocol.Request;
import com.library.common.protocol.Response;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SocketClient {
    private static SocketClient instance;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String serverHost = "localhost";
    private int serverPort = 8888;
    private boolean connected = false;

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

    public synchronized Response sendRequest(Request req) throws IOException {
        if (!connected || socket == null || socket.isClosed()) {
            boolean ok = connect(serverHost, serverPort);
            if (!ok) {
                throw new IOException("Không thể kết nối đến Máy chủ " + serverHost + ":" + serverPort);
            }
        }

        // Ghi nhận gói tin TCP gửi đi (TX) cho Packet Inspector
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
