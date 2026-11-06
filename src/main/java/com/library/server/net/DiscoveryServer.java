package com.library.server.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

/**
 * UDP Discovery Server: Lắng nghe các gói tin UDP Broadcast từ Client trong mạng LAN
 * để Client tự động phát hiện IP và Port của Máy chủ Thư viện mà không cần nhập thủ công.
 */
public class DiscoveryServer implements Runnable {
    public static final int DISCOVERY_PORT = 8889;
    public static final String DISCOVERY_REQUEST_PREFIX = "DISCOVER_LIBRARY_SERVER";
    public static final String DISCOVERY_RESPONSE_PREFIX = "LIBRARY_SERVER_HERE";

    private final int tcpPort;
    private final String serverName;
    private DatagramSocket socket;
    private volatile boolean running = false;
    private Thread listenerThread;

    public DiscoveryServer(int tcpPort, String serverName) {
        this.tcpPort = tcpPort;
        this.serverName = serverName;
    }

    public synchronized void start() {
        if (running) return;
        try {
            socket = new DatagramSocket(DISCOVERY_PORT);
            socket.setBroadcast(true);
            running = true;
            listenerThread = new Thread(this, "UDP-DiscoveryServer-Thread");
            listenerThread.setDaemon(true);
            listenerThread.start();
            System.out.println("[UDP Discovery] Đã kích hoạt dịch vụ tự động phát hiện máy chủ tại cổng UDP " + DISCOVERY_PORT);
        } catch (IOException e) {
            System.err.println("[UDP Discovery] Không thể mở cổng UDP " + DISCOVERY_PORT + " (có thể đã có server khác đang chạy): " + e.getMessage());
        }
    }

    public synchronized void stop() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        if (listenerThread != null) {
            listenerThread.interrupt();
        }
        System.out.println("[UDP Discovery] Đã dừng dịch vụ tự động phát hiện máy chủ.");
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];

        while (running && socket != null && !socket.isClosed()) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8).trim();
                if (message.startsWith(DISCOVERY_REQUEST_PREFIX)) {
                    InetAddress clientAddress = packet.getAddress();
                    int clientPort = packet.getPort();

                    String responseMessage = DISCOVERY_RESPONSE_PREFIX + ":" + tcpPort + ":" + serverName;
                    byte[] responseData = responseMessage.getBytes(StandardCharsets.UTF_8);

                    DatagramPacket responsePacket = new DatagramPacket(
                            responseData,
                            responseData.length,
                            clientAddress,
                            clientPort
                    );
                    socket.send(responsePacket);
                    System.out.println("[UDP Discovery] Phản hồi gói tin tìm kiếm từ Client: " + clientAddress.getHostAddress() + ":" + clientPort);
                }
            } catch (IOException e) {
                if (!running) {
                    break;
                }
                System.err.println("[UDP Discovery] Lỗi nhận/gửi gói tin UDP: " + e.getMessage());
            }
        }
    }
}
