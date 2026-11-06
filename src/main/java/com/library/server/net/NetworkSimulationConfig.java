package com.library.server.net;

/**
 * Cấu hình mô phỏng môi trường mạng & cơ chế bảo mật chống tấn công DoS/Spam.
 */
public class NetworkSimulationConfig {
    private static volatile int simulatedLatencyMs = 0;
    private static volatile boolean jitterEnabled = false;
    private static volatile boolean rateLimitEnabled = true;
    private static volatile int maxRequestsPerSecond = 15;

    public static int getSimulatedLatencyMs() {
        return simulatedLatencyMs;
    }

    public static void setSimulatedLatencyMs(int latencyMs) {
        simulatedLatencyMs = Math.max(0, Math.min(2000, latencyMs));
    }

    public static boolean isJitterEnabled() {
        return jitterEnabled;
    }

    public static void setJitterEnabled(boolean enabled) {
        jitterEnabled = enabled;
    }

    public static boolean isRateLimitEnabled() {
        return rateLimitEnabled;
    }

    public static void setRateLimitEnabled(boolean enabled) {
        rateLimitEnabled = enabled;
    }

    public static int getMaxRequestsPerSecond() {
        return maxRequestsPerSecond;
    }

    public static void setMaxRequestsPerSecond(int maxReq) {
        maxRequestsPerSecond = Math.max(1, maxReq);
    }
}
