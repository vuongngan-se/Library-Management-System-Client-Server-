package com.library.common.protocol;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class NetworkUtilsTest {

    @Test
    @DisplayName("Test serialize and deserialize Request via DataStream")
    public void testRequestStreamSerialization() throws IOException {
        Map<String, String> data = new HashMap<>();
        data.put("username", "admin");
        data.put("password", "admin123");
        Request originalReq = new Request("LOGIN", data);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        NetworkUtils.sendJson(dos, originalReq);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        DataInputStream dis = new DataInputStream(bais);

        Request deserializedReq = NetworkUtils.readJson(dis, Request.class);

        assertNotNull(deserializedReq);
        assertEquals("LOGIN", deserializedReq.getAction());
        assertNotNull(deserializedReq.getData());
    }

    @Test
    @DisplayName("Test Response creation and serialization")
    public void testResponseStreamSerialization() throws IOException {
        Response originalResp = Response.success("Đăng nhập thành công", "USER_DATA");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        NetworkUtils.sendJson(dos, originalResp);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        DataInputStream dis = new DataInputStream(bais);

        Response deserializedResp = NetworkUtils.readJson(dis, Response.class);

        assertNotNull(deserializedResp);
        assertEquals("SUCCESS", deserializedResp.getStatus());
        assertEquals("Đăng nhập thành công", deserializedResp.getMessage());
        assertEquals("USER_DATA", deserializedResp.getData());
    }
}
