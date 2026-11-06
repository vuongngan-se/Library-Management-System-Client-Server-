package com.library.client.net;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * UDP Discovery Client: Tìm kiếm Máy chủ Thư viện tự động trong mạng LAN
 * bằng UDP Broadcast.
 */
public class DiscoveryClient {
    public static final int DISCOVERY_PORT = 8889;
    public static final String DISCOVERY_REQUEST = "DISCOVER_LIBRARY_SERVER";
    public static final String DISCOVERY_RESPONSE_PREFIX = "LIBRARY_SERVER_HERE";

    public static class DiscoveredServer {
        private final String host;
        private final int port;
        private final String serverName;

        public DiscoveredServer(String host, int port, String serverName) {
            this.host = host;
            this.port = port;
            this.serverName = serverName;
        }

        public String getHost() { return host; }
        public int getPort() { return port; }
        public String getServerName() { return serverName; }

        @Override
        public String toString() {
            return serverName + " (" + host + ":" + port + ")";
        }
    }

    /**
     * Dò tìm server đồng bộ với timeout (mili-giây).
     * Trả về DiscoveredServer nếu tìm thấy, hoặc null nếu hết thời gian chờ.
     */
    public static DiscoveredServer discoverServer(int timeoutMs) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            socket.setBroadcast(true);
            socket.setSoTimeout(timeoutMs);

            byte[] sendData = DISCOVERY_REQUEST.getBytes(StandardCharsets.UTF_8);
            PacketInspector.getInstance().recordUdpOut("255.255.255.255:" + DISCOVERY_PORT, DISCOVERY_REQUEST);

            // 1. Thử gửi tới địa chỉ broadcast chung
            try {
                DatagramPacket sendPacket = new DatagramPacket(
                        sendData,
                        sendData.length,
                        InetAddress.getByName("255.255.255.255"),
                        DISCOVERY_PORT
                );
                socket.send(sendPacket);
            } catch (Exception ignored) {}

            // 2. Gửi tới broadcast address của từng card mạng để tăng độ tin cậy
            List<InetAddress> broadcastAddresses = getBroadcastAddresses();
            for (InetAddress broadcastAddr : broadcastAddresses) {
                try {
                    DatagramPacket sendPacket = new DatagramPacket(
                            sendData,
                            sendData.length,
                            broadcastAddr,
                            DISCOVERY_PORT
                    );
                    socket.send(sendPacket);
                } catch (Exception ignored) {}
            }

            // 3. Gửi thêm tới localhost (dành cho trường hợp chạy cả client và server trên cùng 1 máy)
            try {
                DatagramPacket localPacket = new DatagramPacket(
                        sendData,
                        sendData.length,
                        InetAddress.getByName("127.0.0.1"),
                        DISCOVERY_PORT
                );
                socket.send(localPacket);
            } catch (Exception ignored) {}

            // 4. Lắng nghe phản hồi từ máy chủ
            byte[] recvBuf = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
            socket.receive(receivePacket);

            String message = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8).trim();
            PacketInspector.getInstance().recordUdpIn(receivePacket.getAddress().getHostAddress() + ":" + receivePacket.getPort(), message);
            if (message.startsWith(DISCOVERY_RESPONSE_PREFIX)) {
                String[] parts = message.split(":", 3);
                int tcpPort = 8888;
                String serverName = "Thư viện";
                if (parts.length >= 2) {
                    try {
                        tcpPort = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException ignored) {}
                }
                if (parts.length >= 3) {
                    serverName = parts[2];
                }

                String hostAddress = receivePacket.getAddress().getHostAddress();
                // Nếu là loopback IPv6 hoặc dạng 0.0.0.0, đổi về localhost hoặc 127.0.0.1
                if ("0:0:0:0:0:0:0:1".equals(hostAddress) || "::1".equals(hostAddress)) {
                    hostAddress = "127.0.0.1";
                }

                return new DiscoveredServer(hostAddress, tcpPort, serverName);
            }

        } catch (SocketTimeoutException e) {
            // Hết thời gian chờ mà không thấy server phản hồi
            return null;
        } catch (IOException e) {
            System.err.println("[UDP Discovery Client] Lỗi khi dò tìm server: " + e.getMessage());
            return null;
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
        return null;
    }

    /**
     * Dò tìm server bất đồng bộ (chạy trên background thread), không làm đơ giao diện JavaFX.
     */
    public static CompletableFuture<DiscoveredServer> discoverServerAsync(int timeoutMs) {
        return CompletableFuture.supplyAsync(() -> discoverServer(timeoutMs));
    }

    /**
     * Lấy danh sách địa chỉ Broadcast của các card mạng đang hoạt động.
     */
    private static List<InetAddress> getBroadcastAddresses() {
        List<InetAddress> broadcastList = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast != null) {
                        broadcastList.add(broadcast);
                    }
                }
            }
        } catch (Exception ignored) {}
        return broadcastList;
    }
}

