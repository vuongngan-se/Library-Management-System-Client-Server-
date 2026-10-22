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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoginController {

    @FXML private TextField txtHost;
    @FXML private TextField txtPort;

    // Login Form Elements
    @FXML private VBox vboxLogin;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Button btnSwitchLogin;

    // Register Form Elements
    @FXML private VBox vboxRegister;
    @FXML private TextField txtRegFullName;
    @FXML private TextField txtRegUsername;
    @FXML private PasswordField txtRegPassword;
    @FXML private PasswordField txtRegConfirmPassword;
    @FXML private Button btnRegister;
    @FXML private Button btnSwitchRegister;

    // Feedback message
    @FXML private Label lblError;

    @FXML
    public void showLoginForm() {
        vboxLogin.setVisible(true);
        vboxLogin.setManaged(true);
        vboxRegister.setVisible(false);
        vboxRegister.setManaged(false);

        btnSwitchLogin.getStyleClass().removeAll("auth-tab-btn-active");
        btnSwitchRegister.getStyleClass().removeAll("auth-tab-btn-active");
        btnSwitchLogin.getStyleClass().add("auth-tab-btn-active");

        lblError.setText("");
    }

    @FXML
    public void showRegisterForm() {
        vboxLogin.setVisible(false);
        vboxLogin.setManaged(false);
        vboxRegister.setVisible(true);
        vboxRegister.setManaged(true);

        btnSwitchLogin.getStyleClass().removeAll("auth-tab-btn-active");
        btnSwitchRegister.getStyleClass().removeAll("auth-tab-btn-active");
        btnSwitchRegister.getStyleClass().add("auth-tab-btn-active");

        lblError.setText("");
    }

    @FXML
    public void handleLogin() {
        String host = txtHost.getText().trim();
        String portStr = txtPort.getText().trim();
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (host.isEmpty() || portStr.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showError("Vui lòng điền đầy đủ thông tin đăng nhập.");
            return;
        }

        try {
            int port = Integer.parseInt(portStr);
            SocketClient client = SocketClient.getInstance();

            boolean connected = client.connect(host, port);
            if (!connected) {
                showError("Không thể kết nối đến Server " + host + ":" + port);
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
                showError(response.getMessage());
            }

        } catch (NumberFormatException e) {
            showError("Cổng (Port) phải là số nguyên.");
        } catch (IOException e) {
            showError("Lỗi kết nối Socket: " + e.getMessage());
        }
    }

    @FXML
    public void handleRegister() {
        String host = txtHost.getText().trim();
        String portStr = txtPort.getText().trim();
        String fullName = txtRegFullName.getText().trim();
        String username = txtRegUsername.getText().trim();
        String password = txtRegPassword.getText().trim();
        String confirmPassword = txtRegConfirmPassword.getText().trim();

        if (host.isEmpty() || portStr.isEmpty() || fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showError("Vui lòng điền đầy đủ các thông tin đăng ký.");
            return;
        }

        if (username.length() < 3) {
            showError("Tên đăng nhập phải có ít nhất 3 ký tự.");
            return;
        }

        if (password.length() < 6) {
            showError("Mật khẩu phải có ít nhất 6 ký tự.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Mật khẩu xác nhận không khớp.");
            return;
        }

        try {
            int port = Integer.parseInt(portStr);
            SocketClient client = SocketClient.getInstance();

            boolean connected = client.connect(host, port);
            if (!connected) {
                showError("Không thể kết nối đến Server " + host + ":" + port);
                return;
            }

            Map<String, String> regData = new HashMap<>();
            regData.put("fullName", fullName);
            regData.put("username", username);
            regData.put("password", password);
            regData.put("role", "STAFF");

            Request request = new Request("REGISTER", regData);
            Response response = client.sendRequest(request);

            if ("SUCCESS".equals(response.getStatus())) {
                txtUsername.setText(username);
                txtPassword.clear();
                txtRegFullName.clear();
                txtRegUsername.clear();
                txtRegPassword.clear();
                txtRegConfirmPassword.clear();

                showLoginForm();
                showSuccess("Đăng ký thành công! Hãy nhập mật khẩu để đăng nhập.");
            } else {
                showError(response.getMessage());
            }

        } catch (NumberFormatException e) {
            showError("Cổng (Port) phải là số nguyên.");
        } catch (IOException e) {
            showError("Lỗi kết nối Socket: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        lblError.setStyle("-fx-text-fill: #e74c3c;");
        lblError.setText(msg);
    }

    private void showSuccess(String msg) {
        lblError.setStyle("-fx-text-fill: #27ae60;");
        lblError.setText(msg);
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
            stage.setResizable(true);
            stage.setMinWidth(1000);
            stage.setMinHeight(650);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Lỗi khởi tạo giao diện chính: " + e.getMessage());
        }
    }
}
