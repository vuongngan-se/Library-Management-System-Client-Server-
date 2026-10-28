package com.library.common.model;

import java.io.Serializable;

public class ClientSession implements Serializable {
    private String sessionId;
    private String ipAddress;
    private int port;
    private String username;
    private String fullName;
    private String role;
    private String connectedAt;
    private int requestCount;
    private String lastAction;
    private String lastActiveAt;

    public ClientSession() {}

    public ClientSession(String sessionId, String ipAddress, int port, String username, String fullName, String role, String connectedAt) {
        this.sessionId = sessionId;
        this.ipAddress = ipAddress;
        this.port = port;
        this.username = username != null ? username : "Chưa đăng nhập";
        this.fullName = fullName != null ? fullName : "N/A";
        this.role = role != null ? role : "GUEST";
        this.connectedAt = connectedAt;
        this.requestCount = 0;
        this.lastAction = "CONNECT";
        this.lastActiveAt = connectedAt;
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getConnectedAt() { return connectedAt; }
    public void setConnectedAt(String connectedAt) { this.connectedAt = connectedAt; }

    public int getRequestCount() { return requestCount; }
    public void setRequestCount(int requestCount) { this.requestCount = requestCount; }

    public String getLastAction() { return lastAction; }
    public void setLastAction(String lastAction) { this.lastAction = lastAction; }

    public String getLastActiveAt() { return lastActiveAt; }
    public void setLastActiveAt(String lastActiveAt) { this.lastActiveAt = lastActiveAt; }
}
