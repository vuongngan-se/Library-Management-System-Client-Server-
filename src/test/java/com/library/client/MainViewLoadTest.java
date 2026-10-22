package com.library.client;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class MainViewLoadTest {

    @Test
    public void testLoadMainView() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();

        try {
            Platform.startup(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/library/client/views/MainView.fxml"));
                    loader.load();
                } catch (Throwable t) {
                    error.set(t);
                    t.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        } catch (IllegalStateException e) {
            // Already started
            Platform.runLater(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/library/client/views/MainView.fxml"));
                    loader.load();
                } catch (Throwable t) {
                    error.set(t);
                    t.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        if (error.get() != null) {
            throw new RuntimeException("Failed to load MainView.fxml", error.get());
        }
    }
}
