package com.library.common.protocol;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Tiện ích Mật mã học (Hybrid Cryptography):
 * - RSA 2048-bit: Dùng để xác thực và bắt tay trao đổi khóa bí mật (Handshake Key Exchange).
 * - AES-256 CBC: Dùng để mã hóa luồng dữ liệu gói tin Socket tốc độ cao (Payload Encryption).
 */
public class CryptoUtils {
    private static final String RSA_ALGORITHM = "RSA";
    private static final String RSA_TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int AES_KEY_SIZE = 256;
    private static final int IV_SIZE = 16;

    private static final KeyPair SERVER_KEY_PAIR;

    static {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(RSA_ALGORITHM);
            kpg.initialize(2048);
            SERVER_KEY_PAIR = kpg.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Không thể khởi tạo cặp khóa RSA cho Server", e);
        }
    }

    /**
     * Lấy chuỗi Base64 của Khóa công khai (Public Key) của Server.
     */
    public static String getServerPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(SERVER_KEY_PAIR.getPublic().getEncoded());
    }

    /**
     * Chuyển chuỗi Base64 thành đối tượng PublicKey (RSA).
     */
    public static PublicKey decodeRsaPublicKey(String base64PublicKey) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64PublicKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance(RSA_ALGORITHM);
        return kf.generatePublic(spec);
    }

    /**
     * Mã hóa dữ liệu byte (ví dụ khóa AES) bằng RSA Public Key.
     */
    public static String encryptWithRsa(byte[] data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encrypted = cipher.doFinal(data);
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * Server giải mã chuỗi khóa AES mã hóa bằng RSA Private Key.
     */
    public static SecretKey decryptAesKeyWithServerRsa(String base64EncryptedAesKey) throws Exception {
        byte[] encryptedBytes = Base64.getDecoder().decode(base64EncryptedAesKey);
        Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, SERVER_KEY_PAIR.getPrivate());
        byte[] decryptedKeyBytes = cipher.doFinal(encryptedBytes);
        return new SecretKeySpec(decryptedKeyBytes, AES_ALGORITHM);
    }

    /**
     * Sinh khóa đối xứng ngẫu nhiên AES-256 bit an toàn.
     */
    public static SecretKey generateAesKey() {
        try {
            KeyGenerator kg = KeyGenerator.getInstance(AES_ALGORITHM);
            kg.init(AES_KEY_SIZE);
            return kg.generateKey();
        } catch (NoSuchAlgorithmException e) {
            try {
                // Fallback nếu máy không hỗ trợ 256-bit
                KeyGenerator kg = KeyGenerator.getInstance(AES_ALGORITHM);
                kg.init(128);
                return kg.generateKey();
            } catch (Exception ex) {
                throw new RuntimeException("Không thể khởi tạo khóa AES", ex);
            }
        }
    }

    /**
     * Mã hóa chuỗi văn bản bằng AES-CBC với IV ngẫu nhiên 16 bytes.
     * Chuỗi trả về là Base64 của [IV 16 bytes + CipherText].
     */
    public static String encryptAes(String plainText, SecretKey aesKey) throws Exception {
        if (plainText == null) plainText = "";
        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);
        byte[] cipherBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        byte[] combined = new byte[IV_SIZE + cipherBytes.length];
        System.arraycopy(iv, 0, combined, 0, IV_SIZE);
        System.arraycopy(cipherBytes, 0, combined, IV_SIZE, cipherBytes.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    /**
     * Giải mã chuỗi Base64 [IV + CipherText] bằng khóa AES.
     */
    public static String decryptAes(String base64Combined, SecretKey aesKey) throws Exception {
        if (base64Combined == null || base64Combined.isEmpty()) return "";
        byte[] combined = Base64.getDecoder().decode(base64Combined);
        if (combined.length < IV_SIZE) {
            throw new IllegalArgumentException("Dữ liệu bản mã không hợp lệ (ngắn hơn IV)");
        }

        byte[] iv = new byte[IV_SIZE];
        System.arraycopy(combined, 0, iv, 0, IV_SIZE);
        int cipherLen = combined.length - IV_SIZE;
        byte[] cipherBytes = new byte[cipherLen];
        System.arraycopy(combined, 0 + IV_SIZE, cipherBytes, 0, cipherLen);

        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, aesKey, ivSpec);
        byte[] plainBytes = cipher.doFinal(cipherBytes);

        return new String(plainBytes, StandardCharsets.UTF_8);
    }
}
