package com.library.client.util;

import javafx.application.Platform;
import javafx.scene.image.Image;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class QRCodeUtilsTest {

    @BeforeAll
    public static void initJavaFX() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException e) {
            // Platform already initialized
            latch.countDown();
        }
        latch.await();
    }

    @Test
    @DisplayName("Test Generate QR Code Success")
    public void testGenerateQRCodeSuccess() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Image> resultRef = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                Image img = QRCodeUtils.generateQRCode("BOOK:101:978-0132350884:Clean Code", 150, 150);
                resultRef.set(img);
            } finally {
                latch.countDown();
            }
        });

        latch.await();
        Image img = resultRef.get();
        assertNotNull(img);
        assertEquals(150, (int) img.getWidth());
        assertEquals(150, (int) img.getHeight());
    }

    @Test
    @DisplayName("Test Generate QR Code With Empty or Null Content")
    public void testGenerateQRCodeEmpty() {
        assertNull(QRCodeUtils.generateQRCode(null, 100, 100));
        assertNull(QRCodeUtils.generateQRCode("   ", 100, 100));
    }
}
