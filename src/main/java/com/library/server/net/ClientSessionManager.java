package com.library.server.net;

import com.library.common.model.ClientSession;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClientSessionManager {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Map<String, ClientSession> sessionInfoMap = new ConcurrentHashMap<>();
    private static final Map<String, ClientHandler> sessionHandlerMap = new ConcurrentHashMap<>();

    public static void register(String sessionId, String ip, int port, ClientHandler handler) {
        String now = LocalDateTime.now().format(FORMATTER);
        ClientSession session = new ClientSession(sessionId, ip, port, "Chưa đăng nhập", "N/A", "GUEST", now);
        sessionInfoMap.put(sessionId, session);
        if (handler != null) {
            sessionHandlerMap.put(sessionId, handler);
        }
    }

    public static void unregister(String sessionId) {
        sessionInfoMap.remove(sessionId);
        sessionHandlerMap.remove(sessionId);
    }

    public static void updateUser(String sessionId, String username, String fullName, String role) {
        ClientSession session = sessionInfoMap.get(sessionId);
        if (session != null) {
            session.setUsername(username);
            session.setFullName(fullName);
            session.setRole(role);
        }
    }

    public static void recordRequest(String sessionId, String action) {
        ClientSession session = sessionInfoMap.get(sessionId);
        if (session != null) {
            session.setRequestCount(session.getRequestCount() + 1);
            session.setLastAction(action);
            session.setLastActiveAt(LocalDateTime.now().format(FORMATTER));
        }
    }

    public static List<ClientSession> getAllSessions() {
        return new ArrayList<>(sessionInfoMap.values());
    }

    public static int getActiveSessionCount() {
        return sessionInfoMap.size();
    }

    public static int getTotalRequests() {
        return sessionInfoMap.values().stream().mapToInt(ClientSession::getRequestCount).sum();
    }

    public static boolean kickSession(String sessionId) {
        ClientHandler handler = sessionHandlerMap.get(sessionId);
        if (handler != null) {
            handler.disconnect();
            unregister(sessionId);
            return true;
        }
        if (sessionInfoMap.containsKey(sessionId)) {
            sessionInfoMap.remove(sessionId);
            return true;
        }
        return false;
    }
}
