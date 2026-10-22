package com.library.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/library/client/views/LoginView.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Quản Lý Thư Viện - Đăng Nhập & Đăng Ký");
        primaryStage.setScene(new Scene(root, 760, 620));
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(680);
        primaryStage.setMinHeight(520);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
