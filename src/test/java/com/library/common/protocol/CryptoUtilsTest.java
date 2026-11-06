package com.library.common.protocol;

import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.*;

public class CryptoUtilsTest {

    @Test
    void testRsaHandshakeAndAesKeyExchange() throws Exception {
        // 1. Server cấp RSA Public Key dạng Base64
        String serverPubKeyBase64 = CryptoUtils.getServerPublicKeyBase64();
        assertNotNull(serverPubKeyBase64);
        assertFalse(serverPubKeyBase64.isEmpty());

        // 2. Client giải mã Public Key của Server
        PublicKey serverPubKey = CryptoUtils.decodeRsaPublicKey(serverPubKeyBase64);
        assertNotNull(serverPubKey);

        // 3. Client sinh khóa AES-256 ngẫu nhiên
        SecretKey clientAesKey = CryptoUtils.generateAesKey();
        assertNotNull(clientAesKey);

        // 4. Client mã hóa khóa AES bằng RSA Public Key của Server
        String encAesKeyBase64 = CryptoUtils.encryptWithRsa(clientAesKey.getEncoded(), serverPubKey);
        assertNotNull(encAesKeyBase64);

        // 5. Server giải mã khóa AES bằng RSA Private Key của Server
        SecretKey serverDecryptedAesKey = CryptoUtils.decryptAesKeyWithServerRsa(encAesKeyBase64);
        assertNotNull(serverDecryptedAesKey);

        // Xác nhận hai bên nắm giữ cùng một khóa đối xứng
        assertArrayEquals(clientAesKey.getEncoded(), serverDecryptedAesKey.getEncoded());
    }

    @Test
    void testAesEncryptDecryptPayload() throws Exception {
        SecretKey key = CryptoUtils.generateAesKey();
        String originalJson = "{\"action\":\"LOGIN\",\"data\":{\"username\":\"admin\",\"password\":\"secret123\"}}";

        // Mã hóa
        String cipherBase64 = CryptoUtils.encryptAes(originalJson, key);
        assertNotNull(cipherBase64);
        assertNotEquals(originalJson, cipherBase64);

        // Giải mã
        String decryptedJson = CryptoUtils.decryptAes(cipherBase64, key);
        assertEquals(originalJson, decryptedJson);
    }

    @Test
    void testUniqueIvPerEncryption() throws Exception {
        SecretKey key = CryptoUtils.generateAesKey();
        String message = "Xin chào các bạn!";

        String cipher1 = CryptoUtils.encryptAes(message, key);
        String cipher2 = CryptoUtils.encryptAes(message, key);

        // Cùng một nội dung nhưng 2 lần mã hóa phải sinh bản mã khác nhau nhờ IV ngẫu nhiên
        assertNotEquals(cipher1, cipher2);

        // Cả 2 đều phải giải mã ra đúng nội dung gốc
        assertEquals(message, CryptoUtils.decryptAes(cipher1, key));
        assertEquals(message, CryptoUtils.decryptAes(cipher2, key));
    }
}

