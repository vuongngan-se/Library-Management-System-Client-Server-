package com.library.server.net;

import com.library.common.protocol.NetworkUtils;
import com.library.common.protocol.Request;
import com.library.common.protocol.Response;
import com.library.server.service.LibraryService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final LibraryService service;
    private final String clientAddress;

    public ClientHandler(Socket socket, LibraryService service) {
        this.socket = socket;
        this.service = service;
        this.clientAddress = socket.getRemoteSocketAddress().toString();
    }

    @Override
    public void run() {
        System.out.println("Client đã kết nối: " + clientAddress);
        try (
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream())
        ) {
            while (!socket.isClosed()) {
                Request request = NetworkUtils.readJson(dis, Request.class);
                System.out.println("[" + clientAddress + "] Lệnh nhận được: " + request.getAction());

                Response response = service.handleRequest(request);
                NetworkUtils.sendJson(dos, response);
            }
        } catch (IOException e) {
            System.out.println("Client ngắt kết nối: " + clientAddress);
        } finally {
            try {
                if (!socket.isClosed()) socket.close();
            } catch (IOException ignored) {}
        }
    }
}
