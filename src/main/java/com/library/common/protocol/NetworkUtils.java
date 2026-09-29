package com.library.common.protocol;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class NetworkUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void sendJson(DataOutputStream dos, Object object) throws IOException {
        String json = mapper.writeValueAsString(object);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        dos.writeInt(bytes.length);
        dos.write(bytes);
        dos.flush();
    }

    public static <T> T readJson(DataInputStream dis, Class<T> clazz) throws IOException {
        int length = dis.readInt();
        byte[] bytes = new byte[length];
        dis.readFully(bytes);
        String json = new String(bytes, StandardCharsets.UTF_8);
        return mapper.readValue(json, clazz);
    }

    public static ObjectMapper getMapper() {
        return mapper;
    }
}
