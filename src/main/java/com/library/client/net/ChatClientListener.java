package com.library.client.net;

import com.library.common.model.ChatMessage;
import com.library.common.protocol.NetworkUtils;
import com.library.common.protocol.Request;
import com.library.common.protocol.Response;
import javafx.application.Platform;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class ChatClientListener {
    private static ChatClientListener instance;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private volatile boolean running = false;
    private Thread listenerThread;
    private final CopyOnWriteArrayList<Consumer<ChatMessage>> messageConsumers = new CopyOnWriteArrayList<>();

    private ChatClientListener() {}

    public static synchronized ChatClientListener getInstance() {
        if (instance == null) {
            instance = new ChatClientListener();
        }
        return instance;
    }

    public synchronized void addMessageListener(Consumer<ChatMessage> consumer) {
        if (consumer != null && !messageConsumers.contains(consumer)) {
            messageConsumers.add(consumer);
        }
    }

    public synchronized void removeMessageListener(Consumer<ChatMessage> consumer) {
        messageConsumers.remove(consumer);
    }

    public synchronized void start(String host, int port, String username) {
        if (running && socket != null && !socket.isClosed()) {
            return; // Already running
        }

        stop(); // Ensure any old connection is cleaned up

        running = true;
        listenerThread = new Thread(() -> {
            try {
                socket = new Socket(host, port);
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());

                // Register listener on server
                Request registerReq = new Request("REGISTER_CHAT_LISTENER", username);
                NetworkUtils.sendJson(dos, registerReq);
                Response ack = NetworkUtils.readJson(dis, Response.class);
                System.out.println("ChatClientListener: Đã kết nối kênh Broadcast [" + (ack != null ? ack.getStatus() : "OK") + "]");

                while (running && !socket.isClosed()) {
                    ChatMessage msg = NetworkUtils.readJson(dis, ChatMessage.class);
                    if (msg == null) break;

                    for (Consumer<ChatMessage> consumer : messageConsumers) {
                        try {
                            Platform.runLater(() -> consumer.accept(msg));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                if (running) {
                    System.out.println("ChatClientListener: Mất kết nối tới máy chủ (" + e.getMessage() + ")");
                }
            } finally {
                stop();
            }
        }, "Chat-Listener-Thread");

        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public synchronized void stop() {
        running = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {}
        socket = null;
        dis = null;
        dos = null;
    }

    public boolean isConnected() {
        return running && socket != null && !socket.isClosed();
    }
}
