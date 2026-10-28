package com.library.server.net;

import com.library.common.model.ChatMessage;
import com.library.common.protocol.NetworkUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ChatManager {
    private static final int MAX_HISTORY = 100;
    private static final ConcurrentLinkedDeque<ChatMessage> messageHistory = new ConcurrentLinkedDeque<>();
    private static final Map<String, ListenerSession> listeners = new ConcurrentHashMap<>();

    public static class ListenerSession {
        private final String sessionId;
        private final String username;
        private final DataOutputStream dos;

        public ListenerSession(String sessionId, String username, DataOutputStream dos) {
            this.sessionId = sessionId;
            this.username = username;
            this.dos = dos;
        }

        public String getSessionId() { return sessionId; }
        public String getUsername() { return username; }
        public DataOutputStream getDos() { return dos; }
    }

    public static void registerListener(String sessionId, String username, DataOutputStream dos) {
        listeners.put(sessionId, new ListenerSession(sessionId, username, dos));
        System.out.println("ChatManager: Đã đăng ký listener cho [" + username + " - Session " + sessionId + "]");
    }

    public static void unregisterListener(String sessionId) {
        ListenerSession removed = listeners.remove(sessionId);
        if (removed != null) {
            System.out.println("ChatManager: Đã hủy đăng ký listener [" + removed.getUsername() + "]");
        }
    }

    public static void broadcast(ChatMessage message) {
        if (message == null) return;

        // Save to in-memory history
        messageHistory.addLast(message);
        while (messageHistory.size() > MAX_HISTORY) {
            messageHistory.pollFirst();
        }

        System.out.println("ChatManager Broadcast -> " + message);

        List<String> failedSessions = new ArrayList<>();
        for (Map.Entry<String, ListenerSession> entry : listeners.entrySet()) {
            ListenerSession session = entry.getValue();
            try {
                synchronized (session.getDos()) {
                    NetworkUtils.sendJson(session.getDos(), message);
                }
            } catch (IOException e) {
                System.err.println("ChatManager: Lỗi gửi broadcast tới " + session.getUsername() + ": " + e.getMessage());
                failedSessions.add(entry.getKey());
            }
        }

        for (String id : failedSessions) {
            unregisterListener(id);
        }
    }

    public static List<ChatMessage> getHistory() {
        return new ArrayList<>(messageHistory);
    }

    public static void clearHistory() {
        messageHistory.clear();
        ChatMessage sysMsg = new ChatMessage("SYSTEM", "Hệ thống", "ADMIN", "Lịch sử trò chuyện đã được làm sạch.", "SYSTEM");
        broadcast(sysMsg);
    }

    public static int getActiveListenerCount() {
        return listeners.size();
    }
}
