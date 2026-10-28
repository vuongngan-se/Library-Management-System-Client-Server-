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
        NetworkUtils.sendJson(dos, req);
        return NetworkUtils.readJson(dis, Response.class);
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
