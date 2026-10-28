package com.library.client.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.library.client.net.ChatClientListener;
import com.library.client.net.SocketClient;
import com.library.common.model.*;
import com.library.common.protocol.Request;
import com.library.common.protocol.Response;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.util.Duration;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import com.library.client.util.QRCodeUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MainController {

    private User currentUser;
    private final SocketClient socketClient = SocketClient.getInstance();

    @FXML private Label lblUserInfo;
    @FXML private Button btnThemeToggle;
    private boolean isDarkMode = false;

    // Sidebar buttons
    @FXML private Button btnNavDashboard;
    @FXML private Button btnNavBooks;
    @FXML private Button btnNavReaders;
    @FXML private Button btnNavBorrow;
    @FXML private Button btnNavFines;
    @FXML private Button btnNavNetwork;
    @FXML private Button btnNavUsers;
    @FXML private Button btnNavChat;

    // Main Content Area & Views
    @FXML private StackPane contentArea;
    @FXML private VBox viewDashboard;
    @FXML private VBox viewBooks;
    @FXML private VBox viewReaders;
    @FXML private VBox viewBorrow;
    @FXML private VBox viewFines;
    @FXML private VBox viewNetwork;
    @FXML private VBox viewUsers;
    @FXML private VBox viewChat;

    // Live Chat Module
    @FXML private Label lblChatStatus;
    @FXML private Button btnClearChat;
    @FXML private ScrollPane scrollChat;
    @FXML private VBox vboxChatMessages;
    @FXML private TextField txtChatMessage;
    @FXML private Button btnSendChat;
    private int unreadChatCount = 0;

    // Dashboard Metrics & Charts
    @FXML private Label lblStatTotalBooks;
    @FXML private Label lblStatAvailableBooks;
    @FXML private Label lblStatTotalReaders;
    @FXML private Label lblStatActiveBorrowings;
    @FXML private PieChart chartCategory;
    @FXML private BarChart<String, Number> chartTopBooks;
    @FXML private CategoryAxis axisBookTitle;
    @FXML private NumberAxis axisBorrowCount;
    @FXML private TableView<Borrowing> tblDashboardBorrowings;
    @FXML private TableColumn<Borrowing, Integer> colDashId;
    @FXML private TableColumn<Borrowing, Integer> colDashReader;
    @FXML private TableColumn<Borrowing, String> colDashBorrowDate;
    @FXML private TableColumn<Borrowing, String> colDashDueDate;
    @FXML private TableColumn<Borrowing, String> colDashStatus;

    // Books Module
    @FXML private TableView<Book> tblBooks;
    @FXML private TableColumn<Book, Integer> colBookId;
    @FXML private TableColumn<Book, String> colBookTitle;
    @FXML private TableColumn<Book, String> colBookAuthor;
    @FXML private TableColumn<Book, String> colBookCategory;
    @FXML private TableColumn<Book, Integer> colBookQuantity;
    @FXML private TableColumn<Book, Integer> colBookAvailable;

    @FXML private TextField txtSearchBook;
    @FXML private TextField txtBookTitle;
    @FXML private TextField txtBookAuthor;
    @FXML private TextField txtBookPublisher;
    @FXML private TextField txtBookYear;
    @FXML private TextField txtBookQuantity;
    @FXML private TextField txtBookIsbn;

    // Readers Module
    @FXML private TableView<Reader> tblReaders;
    @FXML private TableColumn<Reader, Integer> colReaderId;
    @FXML private TableColumn<Reader, String> colReaderName;
    @FXML private TableColumn<Reader, String> colReaderPhone;
    @FXML private TableColumn<Reader, String> colReaderEmail;
    @FXML private TableColumn<Reader, String> colReaderStatus;

    @FXML private TextField txtReaderName;
    @FXML private TextField txtReaderPhone;
    @FXML private TextField txtReaderEmail;
    @FXML private TextField txtReaderAddress;

    // Borrow Module
    @FXML private TextField txtQuickScan;
    @FXML private TextField txtBorrowReaderId;
    @FXML private TextField txtBorrowBookIds;
    @FXML private TextField txtBorrowDays;
    @FXML private TableView<Borrowing> tblBorrowings;
    @FXML private TableColumn<Borrowing, Integer> colBorrowId;
    @FXML private TableColumn<Borrowing, Integer> colBorrowReader;
    @FXML private TableColumn<Borrowing, String> colBorrowDate;
    @FXML private TableColumn<Borrowing, String> colBorrowDue;
    @FXML private TableColumn<Borrowing, String> colBorrowStatus;
    @FXML private TableColumn<Borrowing, Double> colBorrowFine;

    // Fines Module
    @FXML private TableView<Fine> tblFines;
    @FXML private TableColumn<Fine, Integer> colFineId;
    @FXML private TableColumn<Fine, Integer> colFineBorrowId;
    @FXML private TableColumn<Fine, String> colFineReader;
    @FXML private TableColumn<Fine, Double> colFineAmount;
    @FXML private TableColumn<Fine, String> colFineStatus;
    @FXML private TableColumn<Fine, String> colFineReason;
    @FXML private TableColumn<Fine, String> colFineDate;

    // Network Monitor Module
    @FXML private Label lblStatActiveClients;
    @FXML private Label lblStatTotalRequests;
    @FXML private Label lblStatServerPort;
    @FXML private TableView<ClientSession> tblClientSessions;
    @FXML private TableColumn<ClientSession, String> colSessionId;
    @FXML private TableColumn<ClientSession, String> colSessionIp;
    @FXML private TableColumn<ClientSession, Integer> colSessionPort;
    @FXML private TableColumn<ClientSession, String> colSessionUser;
    @FXML private TableColumn<ClientSession, String> colSessionFullName;
    @FXML private TableColumn<ClientSession, String> colSessionRole;
    @FXML private TableColumn<ClientSession, String> colSessionConnectedAt;
    @FXML private TableColumn<ClientSession, Integer> colSessionRequests;
    @FXML private TableColumn<ClientSession, String> colSessionLastAction;

    // Users Module
    @FXML private TextField txtSearchUser;
    @FXML private TableView<User> tblUsers;
    @FXML private TableColumn<User, Integer> colUserId;
    @FXML private TableColumn<User, String> colUserUsername;
    @FXML private TableColumn<User, String> colUserFullName;
    @FXML private TableColumn<User, String> colUserRole;
    @FXML private TableColumn<User, String> colUserStatus;
    @FXML private TextField txtUserUsername;
    @FXML private PasswordField txtUserPassword;
    @FXML private TextField txtUserFullName;
    @FXML private ComboBox<String> cbUserRole;
    @FXML private ComboBox<String> cbUserStatus;

    private Book selectedBook;
    private Reader selectedReader;
    private User selectedUser;
    private List<User> allUsersList = new ArrayList<>();
    private List<Book> allBooksList = new ArrayList<>();
    private List<Reader> allReadersList = new ArrayList<>();

    @FXML
    public void initialize() {
        if (chartCategory != null) {
            chartCategory.setAnimated(false);
        }
        if (chartTopBooks != null) {
            chartTopBooks.setAnimated(false);
        }
        if (cbUserRole != null) {
            cbUserRole.setItems(FXCollections.observableArrayList("ADMIN", "STAFF"));
            cbUserRole.getSelectionModel().select("STAFF");
        }
        if (cbUserStatus != null) {
            cbUserStatus.setItems(FXCollections.observableArrayList("ACTIVE", "LOCKED"));
            cbUserStatus.getSelectionModel().select("ACTIVE");
        }
    }

    public void initUser(User user) {
        this.currentUser = user;
        lblUserInfo.setText("Xin chào, " + user.getFullName() + " (" + user.getRole() + ")");
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            btnNavNetwork.setVisible(false);
            btnNavNetwork.setManaged(false);
            btnNavUsers.setVisible(false);
            btnNavUsers.setManaged(false);
            if (btnClearChat != null) {
                btnClearChat.setVisible(false);
                btnClearChat.setManaged(false);
            }
        }
        setupTableColumns();
        setupChatListener();
        showDashboard();
    }

    private void setupChatListener() {
        if (currentUser == null) return;
        ChatClientListener chatListener = ChatClientListener.getInstance();
        chatListener.addMessageListener(this::onChatMessageReceived);
        chatListener.start(socketClient.getServerHost(), socketClient.getServerPort(), currentUser.getUsername());
    }

    private void onChatMessageReceived(ChatMessage msg) {
        if (msg == null) return;
        renderChatMessage(msg);

        // Check if message is a reactive real-time sync event
        if (msg.getType() != null && msg.getType().startsWith("SYNC_")) {
            handleRealtimeSyncEvent(msg);
            return;
        }

        if (viewChat != null && !viewChat.isVisible()) {
            unreadChatCount++;
            if (btnNavChat != null) {
                btnNavChat.setText("💬 Trò chuyện Nội bộ (" + unreadChatCount + ")");
            }
        }
    }

    private void handleRealtimeSyncEvent(ChatMessage msg) {
        String type = msg.getType();
        String text = msg.getMessage();

        if ("SYNC_BOOKS".equals(type)) {
            if (viewBooks != null && viewBooks.isVisible()) {
                loadBooksData();
            }
            if (viewDashboard != null && viewDashboard.isVisible()) {
                loadDashboardData();
            }
            if (viewBorrow != null && viewBorrow.isVisible()) {
                loadBorrowingsData();
            }
            showLiveToast(text);
        } else if ("SYNC_FINES".equals(type)) {
            if (viewFines != null && viewFines.isVisible()) {
                loadFinesData();
            }
            if (viewDashboard != null && viewDashboard.isVisible()) {
                loadDashboardData();
            }
            showLiveToast(text);
        } else if ("SYNC_READERS".equals(type)) {
            if (viewReaders != null && viewReaders.isVisible()) {
                loadReadersData();
            }
            showLiveToast(text);
        }
    }

    private void showLiveToast(String message) {
        if (contentArea == null) return;

        Platform.runLater(() -> {
            HBox toast = new HBox(10);
            toast.setAlignment(Pos.CENTER_LEFT);
            toast.setStyle("-fx-background-color: rgba(30, 41, 59, 0.95); -fx-background-radius: 8px; " +
                    "-fx-padding: 10px 16px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.25), 8, 0, 0, 3);");
            toast.setMaxWidth(460);
            toast.setMaxHeight(48);
            StackPane.setAlignment(toast, Pos.TOP_RIGHT);
            StackPane.setMargin(toast, new Insets(10, 15, 0, 0));

            Label icon = new Label("⚡");
            icon.setStyle("-fx-font-size: 16px;");

            Label lbl = new Label(message);
            lbl.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
            lbl.setWrapText(true);

            toast.getChildren().addAll(icon, lbl);
            toast.setOpacity(0.0);
            contentArea.getChildren().add(toast);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(250), toast);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            FadeTransition fadeOut = new FadeTransition(Duration.millis(350), toast);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setDelay(Duration.millis(2800));
            fadeOut.setOnFinished(e -> contentArea.getChildren().remove(toast));

            SequentialTransition seq = new SequentialTransition(fadeIn, fadeOut);
            seq.play();
        });
    }

    private void setupTableColumns() {
        // Dashboard
        colDashId.setCellValueFactory(new PropertyValueFactory<>("borrowingId"));
        colDashReader.setCellValueFactory(new PropertyValueFactory<>("readerId"));
        colDashBorrowDate.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        colDashDueDate.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        colDashStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Books
        colBookId.setCellValueFactory(new PropertyValueFactory<>("bookId"));
        colBookTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colBookAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        colBookCategory.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        colBookQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colBookAvailable.setCellValueFactory(new PropertyValueFactory<>("availableQuantity"));

        tblBooks.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedBook = newVal;
                txtBookTitle.setText(newVal.getTitle());
                txtBookAuthor.setText(newVal.getAuthor());
                txtBookPublisher.setText(newVal.getPublisher());
                txtBookYear.setText(String.valueOf(newVal.getPublishYear()));
                txtBookQuantity.setText(String.valueOf(newVal.getQuantity()));
                txtBookIsbn.setText(newVal.getIsbn());
            }
        });

        // Readers
        colReaderId.setCellValueFactory(new PropertyValueFactory<>("readerId"));
        colReaderName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colReaderPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colReaderEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colReaderStatus.setCellValueFactory(new PropertyValueFactory<>("cardStatus"));

        tblReaders.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedReader = newVal;
                txtReaderName.setText(newVal.getFullName());
                txtReaderPhone.setText(newVal.getPhone());
                txtReaderEmail.setText(newVal.getEmail());
                txtReaderAddress.setText(newVal.getAddress());
            }
        });

        // Borrowings
        colBorrowId.setCellValueFactory(new PropertyValueFactory<>("borrowingId"));
        colBorrowReader.setCellValueFactory(new PropertyValueFactory<>("readerId"));
        colBorrowDate.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        colBorrowDue.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        colBorrowStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colBorrowFine.setCellValueFactory(new PropertyValueFactory<>("fineAmount"));

        // Fines
        colFineId.setCellValueFactory(new PropertyValueFactory<>("fineId"));
        colFineBorrowId.setCellValueFactory(new PropertyValueFactory<>("borrowingId"));
        colFineReader.setCellValueFactory(new PropertyValueFactory<>("readerName"));
        colFineAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colFineStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colFineReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        colFineDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        // Network Sessions
        colSessionId.setCellValueFactory(new PropertyValueFactory<>("sessionId"));
        colSessionIp.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));
        colSessionPort.setCellValueFactory(new PropertyValueFactory<>("port"));
        colSessionUser.setCellValueFactory(new PropertyValueFactory<>("username"));
        colSessionFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colSessionRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colSessionConnectedAt.setCellValueFactory(new PropertyValueFactory<>("connectedAt"));
        colSessionRequests.setCellValueFactory(new PropertyValueFactory<>("requestCount"));
        colSessionLastAction.setCellValueFactory(new PropertyValueFactory<>("lastAction"));

        // Users
        colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colUserUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colUserFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colUserRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colUserStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        tblUsers.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedUser = newVal;
                txtUserUsername.setText(newVal.getUsername());
                txtUserUsername.setEditable(false);
                txtUserPassword.clear();
                txtUserFullName.setText(newVal.getFullName());
                cbUserRole.getSelectionModel().select(newVal.getRole());
                cbUserStatus.getSelectionModel().select(newVal.getStatus());
            }
        });
    }

    private void setActiveNav(Button activeButton, VBox activeView) {
        btnNavDashboard.getStyleClass().remove("sidebar-btn-active");
        btnNavBooks.getStyleClass().remove("sidebar-btn-active");
        btnNavReaders.getStyleClass().remove("sidebar-btn-active");
        btnNavBorrow.getStyleClass().remove("sidebar-btn-active");
        btnNavFines.getStyleClass().remove("sidebar-btn-active");
        btnNavNetwork.getStyleClass().remove("sidebar-btn-active");
        btnNavUsers.getStyleClass().remove("sidebar-btn-active");
        if (btnNavChat != null) btnNavChat.getStyleClass().remove("sidebar-btn-active");

        activeButton.getStyleClass().add("sidebar-btn-active");

        viewDashboard.setVisible(false);
        viewBooks.setVisible(false);
        viewReaders.setVisible(false);
        viewBorrow.setVisible(false);
        viewFines.setVisible(false);
        viewNetwork.setVisible(false);
        viewUsers.setVisible(false);
        if (viewChat != null) viewChat.setVisible(false);

        activeView.setVisible(true);
    }

    @FXML
    public void showChat() {
        setActiveNav(btnNavChat, viewChat);
        unreadChatCount = 0;
        if (btnNavChat != null) {
            btnNavChat.setText("💬 Trò chuyện Nội bộ");
        }
        loadChatHistory();
        Platform.runLater(() -> {
            if (txtChatMessage != null) txtChatMessage.requestFocus();
            if (scrollChat != null) scrollChat.setVvalue(1.0);
        });
    }

    @FXML
    public void showDashboard() {
        setActiveNav(btnNavDashboard, viewDashboard);
        loadDashboardData();
    }

    @FXML
    public void showBooks() {
        setActiveNav(btnNavBooks, viewBooks);
        loadBooksData();
    }

    @FXML
    public void showReaders() {
        setActiveNav(btnNavReaders, viewReaders);
        loadReadersData();
    }

    @FXML
    public void showBorrow() {
        setActiveNav(btnNavBorrow, viewBorrow);
        loadBorrowingsData();
        if (allBooksList.isEmpty()) loadBooksData();
        if (allReadersList.isEmpty()) loadReadersData();
        Platform.runLater(() -> {
            if (txtQuickScan != null) txtQuickScan.requestFocus();
        });
    }

    @FXML
    public void showFines() {
        setActiveNav(btnNavFines, viewFines);
        loadFinesData();
    }

    @FXML
    public void showNetwork() {
        setActiveNav(btnNavNetwork, viewNetwork);
        loadNetworkSessions();
    }

    @FXML
    public void showUsers() {
        setActiveNav(btnNavUsers, viewUsers);
        clearUserForm();
        loadUsersData();
    }

    @FXML
    public void handleRefreshUsers() {
        loadUsersData();
    }

    private void clearUserForm() {
        selectedUser = null;
        txtUserUsername.setEditable(true);
        txtUserUsername.clear();
        txtUserPassword.clear();
        txtUserFullName.clear();
        cbUserRole.getSelectionModel().select("STAFF");
        cbUserStatus.getSelectionModel().select("ACTIVE");
    }

    private void loadUsersData() {
        try {
            Response resp = socketClient.sendRequest(new Request("GET_USERS", null));
            if ("SUCCESS".equals(resp.getStatus())) {
                allUsersList = socketClient.convertData(resp.getData(), new TypeReference<List<User>>() {});
                tblUsers.setItems(FXCollections.observableArrayList(allUsersList));
            } else {
                showAlert("Lỗi", resp.getMessage());
            }
        } catch (IOException e) {
            showAlert("Lỗi truyền tin Socket", e.getMessage());
        }
    }

    @FXML
    public void handleSearchUser() {
        String keyword = txtSearchUser.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            tblUsers.setItems(FXCollections.observableArrayList(allUsersList));
            return;
        }
        List<User> filtered = new ArrayList<>();
        for (User u : allUsersList) {
            if ((u.getUsername() != null && u.getUsername().toLowerCase().contains(keyword)) ||
                (u.getFullName() != null && u.getFullName().toLowerCase().contains(keyword))) {
                filtered.add(u);
            }
        }
        tblUsers.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    public void handleAddUser() {
        String username = txtUserUsername.getText().trim();
        String password = txtUserPassword.getText().trim();
        String fullName = txtUserFullName.getText().trim();
        String role = cbUserRole.getValue();
        String status = cbUserStatus.getValue();

        if (username.length() < 3) {
            showAlert("Lỗi dữ liệu", "Tên đăng nhập phải có ít nhất 3 ký tự!");
            return;
        }
        if (password.length() < 4) {
            showAlert("Lỗi dữ liệu", "Mật khẩu tạo mới phải có ít nhất 4 ký tự!");
            return;
        }
        if (fullName.isEmpty()) {
            showAlert("Lỗi dữ liệu", "Họ và tên nhân viên không được để trống!");
            return;
        }

        User u = new User(0, username, password, fullName, role, status);
        try {
            Response resp = socketClient.sendRequest(new Request("ADD_USER", u));
            if ("SUCCESS".equals(resp.getStatus())) {
                showAlert("Thành công", "Tạo tài khoản người dùng thành công!");
                clearUserForm();
                loadUsersData();
            } else {
                showAlert("Lỗi", resp.getMessage());
            }
        } catch (IOException e) {
            showAlert("Lỗi kết nối", e.getMessage());
        }
    }

    @FXML
    public void handleUpdateUser() {
        if (selectedUser == null) {
            showAlert("Thông báo", "Vui lòng chọn tài khoản nhân viên cần sửa trong bảng!");
            return;
        }
        String fullName = txtUserFullName.getText().trim();
        String role = cbUserRole.getValue();
        String status = cbUserStatus.getValue();
        String password = txtUserPassword.getText().trim();

        if (fullName.isEmpty()) {
            showAlert("Lỗi dữ liệu", "Họ và tên nhân viên không được để trống!");
            return;
        }

        User u = new User(selectedUser.getUserId(), selectedUser.getUsername(), password.isEmpty() ? null : password, fullName, role, status);
        try {
            Response resp = socketClient.sendRequest(new Request("UPDATE_USER", u));
            if ("SUCCESS".equals(resp.getStatus())) {
                showAlert("Thành công", "Cập nhật thông tin tài khoản thành công!");
                loadUsersData();
            } else {
                showAlert("Lỗi", resp.getMessage());
            }
        } catch (IOException e) {
            showAlert("Lỗi kết nối", e.getMessage());
        }
    }

    @FXML
    public void handleResetUserPassword() {
        if (selectedUser == null) {
            showAlert("Thông báo", "Vui lòng chọn tài khoản nhân viên cần đổi mật khẩu!");
            return;
        }
        String newPassword = txtUserPassword.getText().trim();
        if (newPassword.length() < 4) {
            showAlert("Lỗi dữ liệu", "Vui lòng nhập mật khẩu mới có ít nhất 4 ký tự vào ô Mật khẩu!");
            return;
        }

        Map<String, Object> reqData = new HashMap<>();
        reqData.put("userId", selectedUser.getUserId());
        reqData.put("newPassword", newPassword);

        try {
            Response resp = socketClient.sendRequest(new Request("RESET_PASSWORD", reqData));
            if ("SUCCESS".equals(resp.getStatus())) {
                showAlert("Thành công", "Đổi mật khẩu cho tài khoản '" + selectedUser.getUsername() + "' thành công!");
                txtUserPassword.clear();
            } else {
                showAlert("Lỗi", resp.getMessage());
            }
        } catch (IOException e) {
            showAlert("Lỗi kết nối", e.getMessage());
        }
    }

    @FXML
    public void handleDeleteUser() {
        if (selectedUser == null) {
            showAlert("Thông báo", "Vui lòng chọn tài khoản nhân viên cần xóa trong bảng!");
            return;
        }
        if (selectedUser.getUserId() == 1) {
            showAlert("Cảnh báo", "Không thể xóa tài khoản Quản trị viên gốc (admin)!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa tài khoản");
        confirm.setHeaderText("Xóa tài khoản nhân viên: " + selectedUser.getUsername() + " (" + selectedUser.getFullName() + ")?");
        confirm.setContentText("Hành động này không thể hoàn tác!");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Response resp = socketClient.sendRequest(new Request("DELETE_USER", selectedUser.getUserId()));
                if ("SUCCESS".equals(resp.getStatus())) {
                    showAlert("Thành công", "Đã xóa tài khoản thành công!");
                    clearUserForm();
                    loadUsersData();
                } else {
                    showAlert("Lỗi", resp.getMessage());
                }
            } catch (IOException e) {
                showAlert("Lỗi kết nối", e.getMessage());
            }
        }
    }

    @FXML
    public void handleRefreshSessions() {
        loadNetworkSessions();
    }

    private void loadNetworkSessions() {
        try {
            Response resp = socketClient.sendRequest(new Request("GET_ACTIVE_SESSIONS", null));
            if ("SUCCESS".equals(resp.getStatus()) && resp.getData() instanceof Map) {
                Map<?, ?> data = (Map<?, ?>) resp.getData();
                lblStatActiveClients.setText(String.valueOf(data.get("activeCount")));
                lblStatTotalRequests.setText(String.valueOf(data.get("totalRequests")));
                lblStatServerPort.setText("TCP: " + data.get("serverPort"));

                Object sessionsObj = data.get("sessions");
                if (sessionsObj != null) {
                    List<ClientSession> sessions = socketClient.convertData(sessionsObj, new TypeReference<List<ClientSession>>() {});
                    tblClientSessions.setItems(FXCollections.observableArrayList(sessions));
                }
            } else {
                showAlert("Lỗi", resp.getMessage());
            }
        } catch (IOException e) {
            showAlert("Lỗi truyền tin Socket", e.getMessage());
        }
    }

    @FXML
    public void handleKickSelectedSession() {
        ClientSession selected = tblClientSessions.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Thông báo", "Vui lòng chọn một phiên kết nối Client cần ngắt trong bảng!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận ngắt kết nối");
        confirm.setHeaderText("Cưỡng bức ngắt kết nối Socket của Client?");
        confirm.setContentText("Mã phiên: " + selected.getSessionId() + "\nIP/Port: " + selected.getIpAddress() + ":" + selected.getPort() + "\nTài khoản: " + selected.getUsername());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Response resp = socketClient.sendRequest(new Request("KICK_SESSION", selected.getSessionId()));
                if ("SUCCESS".equals(resp.getStatus())) {
                    showAlert("Thành công", "Đã ngắt kết nối phiên " + selected.getSessionId());
                    loadNetworkSessions();
                } else {
                    showAlert("Lỗi", resp.getMessage());
                }
            } catch (IOException e) {
                showAlert("Lỗi truyền tin Socket", e.getMessage());
            }
        }
    }

    private void loadDashboardData() {
        try {
            Response respStats = socketClient.sendRequest(new Request("GET_STATS", null));
            if ("SUCCESS".equals(respStats.getStatus())) {
                Map<?, ?> stats = (Map<?, ?>) respStats.getData();
                lblStatTotalBooks.setText(String.valueOf(stats.get("totalBooks")));
                lblStatAvailableBooks.setText(String.valueOf(stats.get("availableBooks")));
                lblStatTotalReaders.setText(String.valueOf(stats.get("totalReaders")));
                lblStatActiveBorrowings.setText(String.valueOf(stats.get("activeBorrowings")));

                // Load Category Distribution PieChart
                Object catDistObj = stats.get("categoryDistribution");
                if (catDistObj instanceof Map && chartCategory != null) {
                    Map<?, ?> catDist = (Map<?, ?>) catDistObj;
                    ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
                    int count = 0;
                    int otherCount = 0;
                    for (Map.Entry<?, ?> entry : catDist.entrySet()) {
                        String name = String.valueOf(entry.getKey());
                        int val = ((Number) entry.getValue()).intValue();
                        if (count < 5) {
                            pieData.add(new PieChart.Data(name + " (" + val + ")", val));
                        } else {
                            otherCount += val;
                        }
                        count++;
                    }
                    if (otherCount > 0) {
                        pieData.add(new PieChart.Data("Khác (" + otherCount + ")", otherCount));
                    }
                    chartCategory.setData(pieData);
                }

                // Load Top Borrowed Books BarChart
                Object topBooksObj = stats.get("topBorrowedBooks");
                if (topBooksObj instanceof Map && chartTopBooks != null) {
                    Map<?, ?> topBooks = (Map<?, ?>) topBooksObj;
                    chartTopBooks.getData().clear();
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("Lượt mượn");
                    for (Map.Entry<?, ?> entry : topBooks.entrySet()) {
                        String title = String.valueOf(entry.getKey());
                        String shortTitle = title.length() > 16 ? title.substring(0, 14) + "..." : title;
                        int val = ((Number) entry.getValue()).intValue();
                        series.getData().add(new XYChart.Data<>(shortTitle, val));
                    }
                    chartTopBooks.getData().add(series);
                }
            }

            Response respBorrowings = socketClient.sendRequest(new Request("GET_BORROWINGS", null));
            if ("SUCCESS".equals(respBorrowings.getStatus())) {
                List<Borrowing> list = socketClient.convertData(respBorrowings.getData(), new TypeReference<List<Borrowing>>() {});
                tblDashboardBorrowings.setItems(FXCollections.observableArrayList(list));
            }
        } catch (IOException e) {
            showAlert("Lỗi truyền tin Socket", e.getMessage());
        }
    }

    private void loadBooksData() {
        try {
            Response resp = socketClient.sendRequest(new Request("GET_BOOKS", null));
            if ("SUCCESS".equals(resp.getStatus())) {
                List<Book> list = socketClient.convertData(resp.getData(), new TypeReference<List<Book>>() {});
                allBooksList = list;
                tblBooks.setItems(FXCollections.observableArrayList(list));
            }
        } catch (IOException e) {
            showAlert("Lỗi kết nối", e.getMessage());
        }
    }

    @FXML
    public void handleSearchBook() {
        String keyword = txtSearchBook.getText().trim();
        try {
            Response resp = socketClient.sendRequest(new Request("SEARCH_BOOKS", keyword));
            if ("SUCCESS".equals(resp.getStatus())) {
                List<Book> list = socketClient.convertData(resp.getData(), new TypeReference<List<Book>>() {});
                tblBooks.setItems(FXCollections.observableArrayList(list));
            }
        } catch (IOException e) {
            showAlert("Lỗi kết nối", e.getMessage());
        }
    }

    @FXML
    public void handleAddBook() {
        Book b = getBookFromForm();
        if (b == null) return;
        try {
            Response resp = socketClient.sendRequest(new Request("ADD_BOOK", b));
            if ("SUCCESS".equals(resp.getStatus())) {
                showAlert("Thành công", "Đã thêm mới sách!");
                clearBookForm();
                loadBooksData();
            } else {
                showAlert("Lỗi", resp.getMessage());
            }
        } catch (IOException e) {
            showAlert("Lỗi mạng", e.getMessage());
        }
    }

    @FXML
    public void handleUpdateBook() {
        if (selectedBook == null) {
            showAlert("Thông báo", "Vui lòng chọn sách cần sửa từ danh sách.");
            return;
        }
        Book b = getBookFromForm();
        if (b == null) return;
        b.setBookId(selectedBook.getBookId());
        try {
            Response resp = socketClient.sendRequest(new Request("UPDATE_BOOK", b));
            if ("SUCCESS".equals(resp.getStatus())) {
                showAlert("Thành công", "Cập nhật thông tin sách thành công!");
                clearBookForm();
                loadBooksData();
            } else {
                showAlert("Lỗi", resp.getMessage());
            }
        } catch (IOException e) {
            showAlert("Lỗi mạng", e.getMessage());
        }
    }

    @FXML
    public void handleDeleteBook() {
        if (selectedBook == null) {
            showAlert("Thông báo", "Vui lòng chọn sách cần xóa.");
            return;
        }
        try {
            Response resp = socketClient.sendRequest(new Request("DELETE_BOOK", selectedBook.getBookId()));
            if ("SUCCESS".equals(resp.getStatus())) {
                showAlert("Thành công", "Đã xóa sách!");
                clearBookForm();
                loadBooksData();
            } else {
                showAlert("Lỗi", resp.getMessage());
            }
        } catch (IOException e) {
            showAlert("Lỗi mạng", e.getMessage());
        }
    }

    private Book getBookFromForm() {
        String title = txtBookTitle.getText().trim();
        String author = txtBookAuthor.getText().trim();
        String publisher = txtBookPublisher.getText().trim();
        String yearStr = txtBookYear.getText().trim();
        String qtyStr = txtBookQuantity.getText().trim();
        String isbn = txtBookIsbn.getText().trim();

        if (title.isEmpty() || author.isEmpty()) {
            showAlert("Cảnh báo", "Tên sách và tác giả không được trống.");
            return null;
        }

        int year = 2023;
        int qty = 1;
        try {
            if (!yearStr.isEmpty()) year = Integer.parseInt(yearStr);
            if (!qtyStr.isEmpty()) qty = Integer.parseInt(qtyStr);
        } catch (NumberFormatException e) {
            showAlert("Cảnh báo", "Năm và số lượng phải là số nguyên.");
            return null;
        }

        Book b = new Book();
        b.setTitle(title);
        b.setAuthor(author);
        b.setPublisher(publisher);
        b.setPublishYear(year);
        b.setQuantity(qty);
        b.setAvailableQuantity(qty);
        b.setIsbn(isbn);
        b.setCategoryId(1); // Default category
        return b;
    }

    private void clearBookForm() {
        selectedBook = null;
        txtBookTitle.clear();
        txtBookAuthor.clear();
        txtBookPublisher.clear();
        txtBookYear.clear();
        txtBookQuantity.clear();
        txtBookIsbn.clear();
    }

    private void loadReadersData() {
        try {
            Response resp = socketClient.sendRequest(new Request("GET_READERS", null));
            if ("SUCCESS".equals(resp.getStatus())) {
                List<Reader> list = socketClient.convertData(resp.getData(), new TypeReference<List<Reader>>() {});
                allReadersList = list;
                tblReaders.setItems(FXCollections.observableArrayList(list));
            }
        } catch (IOException e) {
            showAlert("Lỗi kết nối", e.getMessage());
        }
    }

    @FXML
    public void handleAddReader() {
        Reader r = getReaderFromForm();
        if (r == null) return;
        try {
            Response resp = socketClient.sendRequest(new Request("ADD_READER", r));
            if ("SUCCESS".equals(resp.getStatus())) {
                showAlert("Thành công", "Đã thêm độc giả mới!");
                clearReaderForm();
                loadReadersData();
            } else {
                showAlert("Lỗi", resp.getMessage());
            }
        } catch (IOException e) {
            showAlert("Lỗi mạng", e.getMessage());
        }
    }

    @FXML
    public void handleUpdateReader() {
        if (selectedReader == null) {
            showAlert("Thông báo", "Chọn độc giả từ bảng để sửa.");
            return;
        }
        Reader r = getReaderFromForm();
        if (r == null) return;
        r.setReaderId(selectedReader.getReaderId());
        try {
            Response resp = socketClient.sendRequest(new Request("UPDATE_READER", r));
            if ("SUCCESS".equals(resp.getStatus())) {
                showAlert("Thành công", "Đã cập nhật độc giả!");
                clearReaderForm();
                loadReadersData();
            } else {
                showAlert("Lỗi", resp.getMessage());
            }
        } catch (IOException e) {
            showAlert("Lỗi mạng", e.getMessage());
        }
    }

    private Reader getReaderFromForm() {
        String name = txtReaderName.getText().trim();
        String phone = txtReaderPhone.getText().trim();
        String email = txtReaderEmail.getText().trim();
        String address = txtReaderAddress.getText().trim();

        if (name.isEmpty()) {
            showAlert("Cảnh báo", "Họ tên độc giả không được trống.");
            return null;
        }

        Reader r = new Reader();
        r.setFullName(name);
        r.setPhone(phone);
        r.setEmail(email);
        r.setAddress(address);
        r.setCardStatus("ACTIVE");
        return r;
    }

    private void clearReaderForm() {
        selectedReader = null;
        txtReaderName.clear();
        txtReaderPhone.clear();
        txtReaderEmail.clear();
        txtReaderAddress.clear();
    }

    private void loadBorrowingsData() {
        try {
            Response resp = socketClient.sendRequest(new Request("GET_BORROWINGS", null));
            if ("SUCCESS".equals(resp.getStatus())) {
                List<Borrowing> list = socketClient.convertData(resp.getData(), new TypeReference<List<Borrowing>>() {});
                tblBorrowings.setItems(FXCollections.observableArrayList(list));
            }
        } catch (IOException e) {
            showAlert("Lỗi kết nối", e.getMessage());
        }
    }

    @FXML
    public void handleCreateBorrowing() {
        String rIdStr = txtBorrowReaderId.getText().trim();
        String bIdsStr = txtBorrowBookIds.getText().trim();
        String daysStr = txtBorrowDays.getText().trim();

        if (rIdStr.isEmpty() || bIdsStr.isEmpty()) {
            showAlert("Cảnh báo", "Vui lòng nhập Mã độc giả và Danh sách mã sách.");
            return;
        }

        try {
            int readerId = Integer.parseInt(rIdStr);
            int days = daysStr.isEmpty() ? 14 : Integer.parseInt(daysStr);
            List<Integer> bookIds = new ArrayList<>();
            for (String part : bIdsStr.split(",")) {
                if (!part.trim().isEmpty()) {
                    bookIds.add(Integer.parseInt(part.trim()));
                }
            }

            Map<String, Object> reqData = new HashMap<>();
            reqData.put("readerId", readerId);
            reqData.put("userId", currentUser.getUserId());
            reqData.put("days", days);
            reqData.put("bookIds", bookIds);

            Response resp = socketClient.sendRequest(new Request("BORROW_BOOKS", reqData));
            if ("SUCCESS".equals(resp.getStatus())) {
                showAlert("Thành công", "Tạo phiếu mượn thành công!");
                txtBorrowReaderId.clear();
                txtBorrowBookIds.clear();
                loadBorrowingsData();
            } else {
                showAlert("Lỗi", resp.getMessage());
            }
        } catch (NumberFormatException e) {
            showAlert("Cảnh báo", "Mã độc giả, mã sách và số ngày phải là định dạng số.");
        } catch (IOException e) {
            showAlert("Lỗi mạng", e.getMessage());
        }
    }

    @FXML
    public void handleReturnSelectedBorrowing() {
        Borrowing selected = tblBorrowings.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Thông báo", "Vui lòng chọn phiếu mượn cần trả.");
            return;
        }

        try {
            Map<String, Object> reqData = new HashMap<>();
            reqData.put("borrowingId", selected.getBorrowingId());

            Response resp = socketClient.sendRequest(new Request("RETURN_BOOKS", reqData));
            if ("SUCCESS".equals(resp.getStatus())) {
                Borrowing returned = socketClient.convertData(resp.getData(), Borrowing.class);
                if (returned != null && returned.getFineAmount() > 0) {
                    showAlert("Thông báo phạt quá hạn", "Trả sách thành công! Độc giả bị phạt " +
                            String.format("%,.0f VNĐ", returned.getFineAmount()) + " do quá hạn nộp sách. Vui lòng kiểm tra tab Tiền phạt.");
                } else {
                    showAlert("Thành công", "Xử lý trả sách thành công!");
                }
                loadBorrowingsData();
            } else {
                showAlert("Lỗi", resp.getMessage());
            }
        } catch (IOException e) {
            showAlert("Lỗi mạng", e.getMessage());
        }
    }

    @FXML
    public void handleRenewSelectedBorrowing() {
        Borrowing selected = tblBorrowings.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Thông báo", "Vui lòng chọn phiếu mượn cần gia hạn.");
            return;
        }

        if (!"BORROWED".equals(selected.getStatus())) {
            showAlert("Cảnh báo", "Chỉ có thể gia hạn phiếu mượn đang trong trạng thái 'Đang mượn' (BORROWED).");
            return;
        }

        try {
            Map<String, Object> reqData = new HashMap<>();
            reqData.put("borrowingId", selected.getBorrowingId());
            reqData.put("extraDays", 7);

            Response resp = socketClient.sendRequest(new Request("RENEW_BORROWING", reqData));
            if ("SUCCESS".equals(resp.getStatus())) {
                showAlert("Thành công", resp.getMessage());
                loadBorrowingsData();
            } else {
                showAlert("Lỗi", resp.getMessage());
            }
        } catch (IOException e) {
            showAlert("Lỗi mạng", e.getMessage());
        }
    }

    @FXML
    public void handleShowBookQrCode() {
        if (selectedBook == null) {
            showAlert("Thông báo", "Vui lòng chọn một cuốn sách từ bảng để xem mã QR!");
            return;
        }

        String qrPayload = "BOOK:" + selectedBook.getBookId() + ":" + (selectedBook.getIsbn() != null ? selectedBook.getIsbn() : "") + ":" + selectedBook.getTitle();
        Image qrImage = QRCodeUtils.generateQRCode(qrPayload, 220, 220);

        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Mã QR Sách Thư Viện");
        dialog.setHeaderText("Mã QR: " + selectedBook.getTitle());

        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(10));

        if (qrImage != null) {
            ImageView imgView = new ImageView(qrImage);
            imgView.setFitWidth(200);
            imgView.setFitHeight(200);
            content.getChildren().add(imgView);
        }

        Label lblInfo = new Label("• Mã sách (ID): " + selectedBook.getBookId() +
                "\n• ISBN: " + (selectedBook.getIsbn() != null && !selectedBook.getIsbn().isEmpty() ? selectedBook.getIsbn() : "Chưa có") +
                "\n• Tác giả: " + selectedBook.getAuthor() +
                "\n• Khả dụng: " + selectedBook.getAvailableQuantity() + " / " + selectedBook.getQuantity());
        lblInfo.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50;");

        Label lblTip = new Label("💡 Quét mã bằng máy quét hoặc dán vào ô 'Quét nhanh' tại quầy Mượn-Trả.");
        lblTip.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");

        content.getChildren().addAll(lblInfo, lblTip);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    @FXML
    public void handleShowReaderQrCard() {
        if (selectedReader == null) {
            showAlert("Thông báo", "Vui lòng chọn một độc giả từ bảng để xem thẻ QR!");
            return;
        }

        String qrPayload = "READER:" + selectedReader.getReaderId() + ":" + selectedReader.getFullName();
        Image qrImage = QRCodeUtils.generateQRCode(qrPayload, 200, 200);

        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Thẻ Độc Giả Điện Tử");
        dialog.setHeaderText("THẺ ĐỘC GIẢ THƯ VIỆN #" + selectedReader.getReaderId());

        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        if (qrImage != null) {
            ImageView imgView = new ImageView(qrImage);
            imgView.setFitWidth(180);
            imgView.setFitHeight(180);
            card.getChildren().add(imgView);
        }

        Label lblName = new Label("👤 " + selectedReader.getFullName());
        lblName.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e3c72;");

        Label lblDetails = new Label("• Mã độc giả: " + selectedReader.getReaderId() +
                "\n• Điện thoại: " + selectedReader.getPhone() +
                "\n• Email: " + selectedReader.getEmail() +
                "\n• Trạng thái thẻ: " + selectedReader.getCardStatus());
        lblDetails.setStyle("-fx-font-size: 12px; -fx-text-fill: #334155;");

        card.getChildren().addAll(lblName, lblDetails);
        dialog.getDialogPane().setContent(card);
        dialog.showAndWait();
    }

    @FXML
    public void handleQuickScan() {
        if (txtQuickScan == null) return;
        String raw = txtQuickScan.getText().trim();
        if (raw.isEmpty()) return;

        txtQuickScan.clear();

        // 1. Check if raw is a READER QR: "READER:<id>:<name>"
        if (raw.toUpperCase().startsWith("READER:")) {
            String[] parts = raw.split(":");
            if (parts.length >= 2) {
                String rId = parts[1].trim();
                txtBorrowReaderId.setText(rId);
                showLiveToast("⚡ Đã quét Thẻ Độc giả #" + rId + (parts.length >= 3 ? " (" + parts[2] + ")" : ""));
                return;
            }
        }

        // 2. Check if raw is a BOOK QR: "BOOK:<id>:<isbn>:<title>"
        if (raw.toUpperCase().startsWith("BOOK:")) {
            String[] parts = raw.split(":");
            if (parts.length >= 2) {
                String bId = parts[1].trim();
                appendBookIdToBorrowList(bId);
                showLiveToast("⚡ Đã quét Sách #" + bId + (parts.length >= 4 ? " (" + parts[3] + ")" : ""));
                return;
            }
        }

        // 3. Numeric ID check: match Book ID first, then Reader ID
        try {
            int numericId = Integer.parseInt(raw);
            Book matchedBook = allBooksList.stream().filter(b -> b.getBookId() == numericId).findFirst().orElse(null);
            if (matchedBook != null) {
                appendBookIdToBorrowList(String.valueOf(matchedBook.getBookId()));
                showLiveToast("⚡ Đã tìm thấy sách: " + matchedBook.getTitle());
                return;
            }

            Reader matchedReader = allReadersList.stream().filter(r -> r.getReaderId() == numericId).findFirst().orElse(null);
            if (matchedReader != null) {
                txtBorrowReaderId.setText(String.valueOf(matchedReader.getReaderId()));
                showLiveToast("⚡ Đã tìm thấy độc giả: " + matchedReader.getFullName());
                return;
            }
        } catch (NumberFormatException ignored) {}

        // 4. Match by ISBN
        Book bookByIsbn = allBooksList.stream()
                .filter(b -> b.getIsbn() != null && b.getIsbn().equalsIgnoreCase(raw))
                .findFirst().orElse(null);
        if (bookByIsbn != null) {
            appendBookIdToBorrowList(String.valueOf(bookByIsbn.getBookId()));
            showLiveToast("⚡ Đã nhận diện ISBN " + raw + " ➔ " + bookByIsbn.getTitle());
            return;
        }

        showAlert("Không nhận diện được mã", "Không tìm thấy Sách hoặc Độc giả tương ứng với mã:\n" + raw);
    }

    private void appendBookIdToBorrowList(String bookId) {
        String current = txtBorrowBookIds.getText().trim();
        if (current.isEmpty()) {
            txtBorrowBookIds.setText(bookId);
        } else {
            List<String> ids = new ArrayList<>(Arrays.asList(current.split("\\s*,\\s*")));
            if (!ids.contains(bookId)) {
                ids.add(bookId);
                txtBorrowBookIds.setText(String.join(",", ids));
            }
        }
    }

    @FXML
    public void handleRefreshFines() {
        loadFinesData();
    }

    private void loadFinesData() {
        try {
            Response resp = socketClient.sendRequest(new Request("GET_FINES", null));
            if ("SUCCESS".equals(resp.getStatus())) {
                List<Fine> list = socketClient.convertData(resp.getData(), new TypeReference<List<Fine>>() {});
                tblFines.setItems(FXCollections.observableArrayList(list));
            }
        } catch (IOException e) {
            showAlert("Lỗi kết nối", e.getMessage());
        }
    }

    @FXML
    public void handlePaySelectedFine() {
        Fine selected = tblFines.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Thông báo", "Vui lòng chọn phiếu phạt cần thu tiền.");
            return;
        }

        if ("PAID".equals(selected.getStatus())) {
            showAlert("Thông báo", "Phiếu phạt #" + selected.getFineId() + " đã được thanh toán trước đó.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận thu tiền");
        confirm.setHeaderText("Thu tiền phạt #" + selected.getFineId());
        confirm.setContentText("Xác nhận thu số tiền: " + String.format("%,.0f VNĐ", selected.getAmount()) +
                "\nĐộc giả: " + selected.getReaderName() +
                "\nLý do: " + selected.getReason());
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Response resp = socketClient.sendRequest(new Request("PAY_FINE", selected.getFineId()));
                if ("SUCCESS".equals(resp.getStatus())) {
                    showAlert("Thành công", "Đã thu tiền phạt thành công!");
                    loadFinesData();
                } else {
                    showAlert("Lỗi", resp.getMessage());
                }
            } catch (IOException e) {
                showAlert("Lỗi mạng", e.getMessage());
            }
        }
    }

    @FXML
    public void handleToggleTheme() {
        if (btnThemeToggle == null || btnThemeToggle.getScene() == null) return;
        Scene scene = btnThemeToggle.getScene();
        String darkCss = getClass().getResource("/css/style-dark.css").toExternalForm();

        isDarkMode = !isDarkMode;
        if (isDarkMode) {
            if (!scene.getStylesheets().contains(darkCss)) {
                scene.getStylesheets().add(darkCss);
            }
            btnThemeToggle.setText("☀️ Giao diện Sáng");
            showLiveToast("🌙 Đã chuyển sang chế độ Giao diện Tối");
        } else {
            scene.getStylesheets().remove(darkCss);
            btnThemeToggle.setText("🌙 Giao diện Tối");
            showLiveToast("☀️ Đã chuyển sang chế độ Giao diện Sáng");
        }
    }

    @FXML
    public void handleLogout() {
        ChatClientListener.getInstance().stop();
        socketClient.disconnect();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/library/client/views/LoginView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnNavDashboard.getScene().getWindow();
            stage.setMaximized(false);
            stage.setResizable(true);
            stage.setMinWidth(680);
            stage.setMinHeight(520);
            stage.setTitle("Quản Lý Thư Viện - Đăng Nhập & Đăng Ký");
            stage.setScene(new Scene(root, 760, 620));
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleSendChat() {
        if (txtChatMessage == null) return;
        String text = txtChatMessage.getText().trim();
        if (text.isEmpty()) return;

        txtChatMessage.clear();

        String sender = currentUser != null ? currentUser.getUsername() : "User";
        String senderName = currentUser != null ? currentUser.getFullName() : "Nhân viên";
        String senderRole = currentUser != null ? currentUser.getRole() : "STAFF";

        Map<String, Object> data = new HashMap<>();
        data.put("sender", sender);
        data.put("senderName", senderName);
        data.put("senderRole", senderRole);
        data.put("message", text);
        data.put("type", "CHAT");

        new Thread(() -> {
            try {
                Response resp = socketClient.sendRequest(new Request("SEND_CHAT_MESSAGE", data));
                if (!"SUCCESS".equals(resp.getStatus())) {
                    Platform.runLater(() -> showAlert("Lỗi gửi tin", resp.getMessage()));
                }
            } catch (IOException e) {
                Platform.runLater(() -> showAlert("Lỗi mạng", "Không thể gửi tin nhắn: " + e.getMessage()));
            }
        }, "SendChat-Thread").start();
    }

    @FXML
    public void handleClearChat() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận");
        confirm.setHeaderText("Xóa toàn bộ lịch sử trò chuyện nội bộ?");
        confirm.setContentText("Hành động này sẽ làm sạch màn hình trò chuyện của tất cả người dùng!");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Response resp = socketClient.sendRequest(new Request("CLEAR_CHAT_HISTORY", null));
                if ("SUCCESS".equals(resp.getStatus())) {
                    if (vboxChatMessages != null) vboxChatMessages.getChildren().clear();
                } else {
                    showAlert("Lỗi", resp.getMessage());
                }
            } catch (IOException e) {
                showAlert("Lỗi mạng", e.getMessage());
            }
        }
    }

    private void loadChatHistory() {
        try {
            Response resp = socketClient.sendRequest(new Request("GET_CHAT_HISTORY", null));
            if ("SUCCESS".equals(resp.getStatus()) && resp.getData() != null) {
                List<ChatMessage> list = socketClient.convertData(resp.getData(), new TypeReference<List<ChatMessage>>() {});
                if (vboxChatMessages != null) {
                    vboxChatMessages.getChildren().clear();
                    for (ChatMessage msg : list) {
                        renderChatMessage(msg);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Không thể tải lịch sử chat: " + e.getMessage());
        }
    }

    private void renderChatMessage(ChatMessage msg) {
        if (vboxChatMessages == null) return;

        boolean isSelf = currentUser != null && currentUser.getUsername() != null
                && currentUser.getUsername().equalsIgnoreCase(msg.getSender());
        boolean isSystem = "SYSTEM".equalsIgnoreCase(msg.getType());

        HBox row = new HBox();
        row.setFillHeight(false);

        if (isSystem) {
            row.setAlignment(Pos.CENTER);
            Label lbl = new Label("🔔 " + msg.getMessage() + " (" + msg.getTimestamp() + ")");
            lbl.getStyleClass().add("chat-bubble-system");
            lbl.setWrapText(true);
            lbl.setMaxWidth(600);
            row.getChildren().add(lbl);
        } else if (isSelf) {
            row.setAlignment(Pos.CENTER_RIGHT);
            VBox bubble = new VBox(3);
            bubble.getStyleClass().add("chat-bubble-self");
            bubble.setMaxWidth(500);

            Label lblMsg = new Label(msg.getMessage());
            lblMsg.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
            lblMsg.setWrapText(true);

            Label lblTime = new Label(msg.getTimestamp());
            lblTime.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 10px;");
            lblTime.setMaxWidth(Double.MAX_VALUE);
            lblTime.setAlignment(Pos.CENTER_RIGHT);

            bubble.getChildren().addAll(lblMsg, lblTime);
            row.getChildren().add(bubble);
        } else {
            row.setAlignment(Pos.CENTER_LEFT);
            row.setSpacing(8);

            String initial = (msg.getSenderName() != null && !msg.getSenderName().isEmpty())
                    ? msg.getSenderName().substring(0, 1).toUpperCase() : "?";
            Label avatar = new Label(initial);
            avatar.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-min-width: 32px; -fx-min-height: 32px; -fx-max-width: 32px; -fx-max-height: 32px; -fx-alignment: center; -fx-background-radius: 16px;");

            VBox bubble = new VBox(3);
            bubble.getStyleClass().add("chat-bubble-other");
            bubble.setMaxWidth(500);

            HBox header = new HBox(6);
            header.setAlignment(Pos.CENTER_LEFT);
            Label lblName = new Label(msg.getSenderName() != null ? msg.getSenderName() : msg.getSender());
            lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #2c3e50;");

            Label lblRole = new Label(msg.getSenderRole() != null ? msg.getSenderRole() : "STAFF");
            lblRole.setStyle("-fx-background-color: " + ("ADMIN".equalsIgnoreCase(msg.getSenderRole()) ? "#e74c3c" : "#3498db") + "; -fx-text-fill: white; -fx-font-size: 9px; -fx-padding: 1 5; -fx-background-radius: 4; -fx-font-weight: bold;");

            Label lblTime = new Label(msg.getTimestamp());
            lblTime.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 10px;");

            header.getChildren().addAll(lblName, lblRole, lblTime);

            Label lblMsg = new Label(msg.getMessage());
            lblMsg.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 13px;");
            lblMsg.setWrapText(true);

            bubble.getChildren().addAll(header, lblMsg);
            row.getChildren().addAll(avatar, bubble);
        }

        vboxChatMessages.getChildren().add(row);
        if (scrollChat != null) {
            scrollChat.layout();
            scrollChat.setVvalue(1.0);
        }
    }

    @FXML
    public void handleExportBooksCsv() {
        exportCsv("EXPORT_BOOKS_CSV", "danh_sach_sach.csv");
    }

    @FXML
    public void handleExportBorrowingsCsv() {
        exportCsv("EXPORT_BORROWINGS_CSV", "lich_su_muon_tra.csv");
    }

    @FXML
    public void handleExportFinesCsv() {
        exportCsv("EXPORT_FINES_CSV", "danh_sach_tien_phat.csv");
    }

    private void exportCsv(String action, String fallbackFileName) {
        try {
            Response resp = socketClient.sendRequest(new Request(action, null));
            if ("SUCCESS".equals(resp.getStatus()) && resp.getData() instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) resp.getData();
                Object fnObj = map.get("fileName");
                String fileName = fnObj != null ? fnObj.toString() : fallbackFileName;
                Object dataObj = map.get("csvData");
                String csvData = dataObj != null ? dataObj.toString() : null;
                if (csvData != null) {
                    saveCsvWithFileChooser(fileName, csvData);
                } else {
                    showAlert("Lỗi xuất file", "Dữ liệu trả về từ Server bị rỗng.");
                }
            } else {
                showAlert("Lỗi", resp.getMessage());
            }
        } catch (IOException e) {
            showAlert("Lỗi kết nối", e.getMessage());
        }
    }

    private void saveCsvWithFileChooser(String defaultFileName, String csvContent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu báo cáo CSV / Excel");
        fileChooser.setInitialFileName(defaultFileName);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"),
                new FileChooser.ExtensionFilter("All Files (*.*)", "*.*")
        );

        Stage stage = null;
        if (contentArea != null && contentArea.getScene() != null) {
            stage = (Stage) contentArea.getScene().getWindow();
        } else if (btnNavDashboard != null && btnNavDashboard.getScene() != null) {
            stage = (Stage) btnNavDashboard.getScene().getWindow();
        }
        File targetFile = fileChooser.showSaveDialog(stage);
        if (targetFile != null) {
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(targetFile), StandardCharsets.UTF_8)) {
                writer.write(csvContent);
                showAlert("Xuất Báo cáo thành công", "File đã được lưu tại:\n" + targetFile.getAbsolutePath());
            } catch (IOException ex) {
                showAlert("Lỗi lưu file", "Không thể ghi file: " + ex.getMessage());
            }
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
