package com.library.common.model;

import java.io.Serializable;

/**
 * Đại diện cho một gói tin mạng được ghi nhận bởi Packet Inspector (Mini-Wireshark).
 */
public class NetworkPacket implements Serializable {
    private String timestamp;
    private String protocol; // TCP, UDP, HTTP
    private String direction; // TX (Gửi đi ->), RX (Nhận về <-)
    private String endpoint; // 192.168.1.15:8888, 255.255.255.255:8889, ...
    private String summary; // Action: BORROW_BOOKS, Status: SUCCESS, ...
    private int sizeBytes;
    private String payload; // Chi tiết nội dung gói tin

    public NetworkPacket() {}

    public NetworkPacket(String timestamp, String protocol, String direction, String endpoint, String summary, int sizeBytes, String payload) {
        this.timestamp = timestamp;
        this.protocol = protocol;
        this.direction = direction;
        this.endpoint = endpoint;
        this.summary = summary;
        this.sizeBytes = sizeBytes;
        this.payload = payload;
    }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { this.protocol = protocol; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public int getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(int sizeBytes) { this.sizeBytes = sizeBytes; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
}

