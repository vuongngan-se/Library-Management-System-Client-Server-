package com.library.client.controller;

import com.library.client.net.SocketClient;
import com.library.common.model.User;
import com.library.common.protocol.Request;
import com.library.common.protocol.Response;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoginController {

    @FXML private TextField txtHost;
    @FXML private TextField txtPort;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;
    @FXML private Button btnLogin;

    @FXML
    public void handleLogin() {
        String host = txtHost.getText().trim();
        String portStr = txtPort.getText().trim();
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (host.isEmpty() || portStr.isEmpty() || username.isEmpty() || password.isEmpty()) {
            lblError.setText("Vui lòng điền đầy đủ thông tin đăng nhập.");
            return;
        }

        try {
            int port = Integer.parseInt(portStr);
            SocketClient client = SocketClient.getInstance();

            boolean connected = client.connect(host, port);
            if (!connected) {
                lblError.setText("Không thể kết nối đến Server " + host + ":" + port);
                return;
            }

            Map<String, String> creds = new HashMap<>();
            creds.put("username", username);
            creds.put("password", password);

            Request request = new Request("LOGIN", creds);
            Response response = client.sendRequest(request);

            if ("SUCCESS".equals(response.getStatus())) {
                User user = client.convertData(response.getData(), User.class);
                openMainWindow(user);
            } else {
                lblError.setText(response.getMessage());
            }

        } catch (NumberFormatException e) {
            lblError.setText("Cổng (Port) phải là số nguyên.");
        } catch (IOException e) {
            lblError.setText("Lỗi kết nối Socket: " + e.getMessage());
        }
    }

    private void openMainWindow(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/library/client/views/MainView.fxml"));
            Parent root = loader.load();

            MainController controller = loader.getController();
            controller.initUser(user);

            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setTitle("Quản Lý Thư Viện - " + user.getFullName() + " (" + user.getRole() + ")");
            stage.setScene(new Scene(root, 1100, 720));
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            lblError.setText("Lỗi khởi tạo giao diện chính: " + e.getMessage());
        }
    }
}
