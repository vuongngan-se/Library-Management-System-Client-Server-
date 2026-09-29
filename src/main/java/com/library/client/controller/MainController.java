package com.library.client.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.library.client.net.SocketClient;
import com.library.common.model.Book;
import com.library.common.model.Borrowing;
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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
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

    // Views
    @FXML private VBox viewDashboard;
    @FXML private VBox viewBooks;
    @FXML private VBox viewReaders;
    @FXML private VBox viewBorrow;

    // Dashboard Metrics
    @FXML private Label lblStatTotalBooks;
    @FXML private Label lblStatAvailableBooks;
    @FXML private Label lblStatTotalReaders;
    @FXML private Label lblStatActiveBorrowings;
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

    private Book selectedBook;
    private Reader selectedReader;

    public void initUser(User user) {
        this.currentUser = user;
        lblUserInfo.setText("Xin chào, " + user.getFullName() + " (" + user.getRole() + ")");
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
    }

    private void setActiveNav(Button activeButton, VBox activeView) {
        btnNavDashboard.getStyleClass().remove("sidebar-btn-active");
        btnNavBooks.getStyleClass().remove("sidebar-btn-active");
        btnNavReaders.getStyleClass().remove("sidebar-btn-active");
        btnNavBorrow.getStyleClass().remove("sidebar-btn-active");

        activeButton.getStyleClass().add("sidebar-btn-active");

        viewDashboard.setVisible(false);
        viewBooks.setVisible(false);
        viewReaders.setVisible(false);
        viewBorrow.setVisible(false);

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

    private void loadDashboardData() {
        try {
            Response respStats = socketClient.sendRequest(new Request("GET_STATS", null));
            if ("SUCCESS".equals(respStats.getStatus())) {
                Map<?, ?> stats = (Map<?, ?>) respStats.getData();
                lblStatTotalBooks.setText(String.valueOf(stats.get("totalBooks")));
                lblStatAvailableBooks.setText(String.valueOf(stats.get("availableBooks")));
                lblStatTotalReaders.setText(String.valueOf(stats.get("totalReaders")));
                lblStatActiveBorrowings.setText(String.valueOf(stats.get("activeBorrowings")));
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
                showAlert("Thành công", "Xử lý trả sách thành công!");
                loadBorrowingsData();
            } else {
                showAlert("Lỗi", resp.getMessage());
            }
        } catch (IOException e) {
            showAlert("Lỗi mạng", e.getMessage());
        }
    }

    @FXML
    public void handleLogout() {
        socketClient.disconnect();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/library/client/views/LoginView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnNavDashboard.getScene().getWindow();
            stage.setTitle("Quản Lý Thư Viện - Đăng Nhập");
            stage.setScene(new Scene(root, 750, 550));
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
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
