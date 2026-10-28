package com.library.client.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.library.client.net.SocketClient;
import com.library.common.model.Book;
import com.library.common.model.Borrowing;
import com.library.common.model.ClientSession;
import com.library.common.model.Fine;
import com.library.common.model.Reader;
import com.library.common.model.User;
import com.library.common.protocol.Request;
import com.library.common.protocol.Response;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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

    // Sidebar buttons
    @FXML private Button btnNavDashboard;
    @FXML private Button btnNavBooks;
    @FXML private Button btnNavReaders;
    @FXML private Button btnNavBorrow;
    @FXML private Button btnNavFines;
    @FXML private Button btnNavNetwork;

    // Main Content Area & Views
    @FXML private StackPane contentArea;
    @FXML private VBox viewDashboard;
    @FXML private VBox viewBooks;
    @FXML private VBox viewReaders;
    @FXML private VBox viewBorrow;
    @FXML private VBox viewFines;
    @FXML private VBox viewNetwork;

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

    private Book selectedBook;
    private Reader selectedReader;

    @FXML
    public void initialize() {
        if (chartCategory != null) {
            chartCategory.setAnimated(false);
        }
        if (chartTopBooks != null) {
            chartTopBooks.setAnimated(false);
        }
    }

    public void initUser(User user) {
        this.currentUser = user;
        lblUserInfo.setText("Xin chào, " + user.getFullName() + " (" + user.getRole() + ")");
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            btnNavNetwork.setVisible(false);
            btnNavNetwork.setManaged(false);
        }
        setupTableColumns();
        showDashboard();
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
    }

    private void setActiveNav(Button activeButton, VBox activeView) {
        btnNavDashboard.getStyleClass().remove("sidebar-btn-active");
        btnNavBooks.getStyleClass().remove("sidebar-btn-active");
        btnNavReaders.getStyleClass().remove("sidebar-btn-active");
        btnNavBorrow.getStyleClass().remove("sidebar-btn-active");
        btnNavFines.getStyleClass().remove("sidebar-btn-active");
        btnNavNetwork.getStyleClass().remove("sidebar-btn-active");

        activeButton.getStyleClass().add("sidebar-btn-active");

        viewDashboard.setVisible(false);
        viewBooks.setVisible(false);
        viewReaders.setVisible(false);
        viewBorrow.setVisible(false);
        viewFines.setVisible(false);
        viewNetwork.setVisible(false);

        activeView.setVisible(true);
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
    public void handleLogout() {
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
