package com.library.common.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private String id;
    private String sender;
    private String senderName;
    private String senderRole;
    private String message;
    private String timestamp;
    private String type; // "CHAT", "SYSTEM", "ALERT"

    public ChatMessage() {
        this.id = "MSG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.timestamp = LocalDateTime.now().format(TIME_FORMAT);
        this.type = "CHAT";
    }

    public ChatMessage(String sender, String senderName, String senderRole, String message) {
        this();
        this.sender = sender;
        this.senderName = senderName;
        this.senderRole = senderRole;
        this.message = message;
        this.type = "CHAT";
    }

    public ChatMessage(String sender, String senderName, String senderRole, String message, String type) {
        this(sender, senderName, senderRole, message);
        this.type = type != null ? type : "CHAT";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderRole() {
        return senderRole;
    }

    public void setSenderRole(String senderRole) {
        this.senderRole = senderRole;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + senderName + " (" + senderRole + "): " + message;
    }
}
