package com.library.client.net;

import com.library.common.model.NetworkPacket;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Packet Inspector (Mini-Wireshark): Bộ lắng nghe và ghi nhận các gói tin mạng
 * theo thời gian thực (TCP, UDP, HTTP) trong ứng dụng.
 */
public class PacketInspector {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final int MAX_PACKETS = 300;
    private static PacketInspector instance;

    private final List<NetworkPacket> internalList = new CopyOnWriteArrayList<>();
    private final ObservableList<NetworkPacket> packets = FXCollections.observableArrayList();
    private volatile boolean paused = false;
    private final List<Consumer<NetworkPacket>> packetListeners = new CopyOnWriteArrayList<>();

    private PacketInspector() {}

    public static synchronized PacketInspector getInstance() {
        if (instance == null) {
            instance = new PacketInspector();
        }
        return instance;
    }

    public ObservableList<NetworkPacket> getPackets() {
        return packets;
    }

    public List<NetworkPacket> getInternalList() {
        return internalList;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public void clear() {
        internalList.clear();
        try {
            if (Platform.isFxApplicationThread()) {
                packets.clear();
            } else {
                Platform.runLater(packets::clear);
            }
        } catch (IllegalStateException e) {
            packets.clear();
        }
    }

    public void addPacketListener(Consumer<NetworkPacket> listener) {
        packetListeners.add(listener);
    }

    /**
     * Ghi nhận một gói tin mạng mới.
     */
    public void record(String protocol, String direction, String endpoint, String summary, int sizeBytes, String payload) {
        if (paused) return;

        String timestamp = LocalTime.now().format(TIME_FORMATTER);
        NetworkPacket packet = new NetworkPacket(
                timestamp,
                protocol,
                direction,
                endpoint,
                summary,
                sizeBytes,
                payload != null ? payload : ""
        );

        if (internalList.size() >= MAX_PACKETS) {
            internalList.remove(0);
        }
        internalList.add(packet);

        // Kích hoạt listeners ngay lập tức
        for (Consumer<NetworkPacket> listener : packetListeners) {
            try {
                listener.accept(packet);
            } catch (Exception ignored) {}
        }

        // Cập nhật ObservableList cho JavaFX UI
        Runnable fxUpdate = () -> {
            if (packets.size() >= MAX_PACKETS) {
                packets.remove(0);
            }
            packets.add(packet);
        };

        try {
            if (Platform.isFxApplicationThread()) {
                fxUpdate.run();
            } else {
                Platform.runLater(fxUpdate);
            }
        } catch (IllegalStateException e) {
            fxUpdate.run();
        }
    }

    // Tiện ích ghi nhanh cho TCP
    public void recordTcpOut(String host, int port, String action, String jsonPayload) {
        int size = jsonPayload != null ? jsonPayload.getBytes(java.nio.charset.StandardCharsets.UTF_8).length : 0;
        record("TCP", "TX ➔", host + ":" + port, "Lệnh: " + action, size, jsonPayload);
    }

    public void recordTcpIn(String host, int port, String status, String jsonPayload) {
        int size = jsonPayload != null ? jsonPayload.getBytes(java.nio.charset.StandardCharsets.UTF_8).length : 0;
        record("TCP", "RX ⬅", host + ":" + port, "Phản hồi: " + status, size, jsonPayload);
    }

    // Tiện ích ghi nhanh cho UDP
    public void recordUdpOut(String endpoint, String message) {
        int size = message != null ? message.getBytes(java.nio.charset.StandardCharsets.UTF_8).length : 0;
        record("UDP", "TX ➔", endpoint, "Broadcast: " + message, size, message);
    }

    public void recordUdpIn(String endpoint, String message) {
        int size = message != null ? message.getBytes(java.nio.charset.StandardCharsets.UTF_8).length : 0;
        record("UDP", "RX ⬅", endpoint, "Nhận: " + message, size, message);
    }

    // Tiện ích ghi nhanh cho HTTP
    public void recordHttp(String method, String url, int statusCode, String responseSummary) {
        record("HTTP", "RX ⬅", url, method + " ➔ " + statusCode, responseSummary != null ? responseSummary.length() : 0, responseSummary);
    }
}
