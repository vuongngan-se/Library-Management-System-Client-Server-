# Hệ Thống Quản Lý Thư Viện Client–Server (Java Socket & MySQL)

Hệ thống quản lý thư viện được phát triển theo kiến trúc Client–Server 3 lớp, áp dụng lập trình mạng Java TCP Socket, JavaFX Modern UI và cơ sở dữ liệu MySQL.

Ứng dụng cho phép nhiều Client kết nối đồng thời đến Server trung tâm qua mạng LAN để thực hiện các nghiệp vụ quản lý sách, độc giả, mượn trả và tra cứu dữ liệu tập trung.

---

## 1. Mô Hình Kiến Trúc & Công Nghệ

```
+-------------------+        TCP Socket (JSON Protocol)        +-------------------+
|   JavaFX Client   | <======================================> |    Java Server    |
| (UI / Controllers)|                                          | (Multi-threading) |
+-------------------+                                          +---------+---------+
                                                                         | JDBC
                                                                         v
                                                               +-------------------+
                                                               |  MySQL Database   |
                                                               +-------------------+
```

* **Ngôn ngữ**: Java 17
* **Mạng / Giao thức**: TCP Socket (`ServerSocket`, `Socket`), JSON Protocol (`Jackson`)
* **Xử lý đồng thời**: Multi-threading / Thread Pool (`ExecutorService`)
* **Giao diện**: JavaFX (FXML + CSS Modern Flat UI & Animation)
* **Cơ sở dữ liệu**: MySQL, HikariCP Connection Pool
* **Công cụ build**: Apache Maven

---

## 2. Các Chức Năng Chính

* **Đăng nhập & Phân quyền**: Quản trị viên (`ADMIN`) và Nhân viên (`STAFF`).
* **Quản lý Sách**: Thêm, sửa, xóa, tìm kiếm theo tiêu đề/tác giả/thể loại, theo dõi tồn kho.
* **Quản lý Độc giả**: Thêm, sửa, xóa, quản lý trạng thái thẻ độc giả.
* **Quản lý Mượn & Trả Sách**: Lập phiếu mượn, gia hạn, trả sách, tính phí phạt quá hạn.
* **Thống kê & Báo cáo**: Dashboard tổng quan số lượng sách, độc giả và lượt mượn trả.
* **Quản lý Kết nối**: Cấu hình linh hoạt Host IP và Port, hỗ trợ đa kết nối đồng thời.

---

## 3. Cấu Trúc Thư Mục

```
.
├── pom.xml
├── sql/
│   └── schema.sql              # Kịch bản khởi tạo CSDL MySQL & dữ liệu mẫu
└── src/
    ├── main/
    │   ├── java/com/library/
    │   │   ├── client/         # JavaFX Client (App, Controller, SocketClient)
    │   │   ├── common/         # Data Model & Protocol (Request/Response)
    │   │   └── server/         # Java Socket Server, Services & DatabaseConfig
    │   └── resources/
    │       ├── css/            # Style CSS giao diện Modern
    │       └── com/library/client/views/  # FXML Views (LoginView, MainView)
    └── test/                   # Unit Tests
```

---

## 4. Hướng Dẫn Khởi Chạy

### Bước 1: Khởi tạo Cơ sở dữ liệu MySQL
1. Mở MySQL Workbench hoặc CLI.
2. Chạy file kịch bản SQL `sql/schema.sql` để tạo cơ sở dữ liệu `library_db` và các bảng mẫu.

### Bước 2: Chạy trên 1 máy (Localhouse / Development)

1. **Khởi chạy Server**:
   ```bash
   mvn exec:java -Dexec.mainClass="com.library.server.ServerApp"
   ```
   *(Nếu mật khẩu MySQL khác `123456`, truyền tham số: `-Ddb.user=root -Ddb.pass=your_password`)*

2. **Khởi chạy Client**:
   Mở thêm cửa sổ Terminal mới và chạy:
   ```bash
   mvn javafx:run
   ```

3. **Thông tin đăng nhập mặc định**:
   * **Host / Port**: `localhost` / `8888`
   * **Admin**: `admin` / `admin123`
   * **Staff**: `staff` / `staff123`

---

## 5. Hướng Dẫn Chạy Trên 2 Máy Qua Mạng LAN

1. **Chuẩn bị mạng**: Kết nối máy Server và máy Client vào cùng mạng Wi-Fi/LAN.
2. **Lấy IP Máy Server**:
   * Trên máy Server, mở PowerShell/CMD và chạy lệnh `ipconfig`.
   * Tìm địa chỉ IPv4 (Ví dụ: `192.168.1.15`).
3. **Mở cổng Firewall**: Cho phép cổng TCP `8888` hoạt động qua Windows Firewall trên máy Server.
4. **Khởi chạy Server (Trên máy Server)**:
   ```bash
   mvn exec:java -Dexec.mainClass="com.library.server.ServerApp"
   ```
5. **Khởi chạy Client (Trên máy Client)**:
   ```bash
   mvn javafx:run
   ```
   Tại màn hình Đăng nhập, nhập **Host** là IP máy Server (ví dụ `192.168.1.15`) và **Port** `8888`.


  6. Kịch bản demo 2 máy thể hiện rõ kiến trúc Client-Server và tính đồng bộ qua TCP Socket:

Chuẩn bị: Máy 1 chạy ServerApp và MySQL. Máy 1 (Client 1) và Máy 2 (Client 2) cùng nhập địa chỉ IP của Máy 1 và cổng 8888 để kết nối.
Đồng bộ dữ liệu: Máy 1 thực hiện mượn 1 cuốn sách, Máy 2 ấn tra cứu lại ngay lập tức để giảng viên thấy số lượng tồn trong MySQL giảm đi 1.
Xử lý đa luồng: Đăng nhập 2 tài khoản tại 2 máy và gửi yêu cầu đồng thời để chứng minh Server lắng nghe và xử lý nhiều luồng Thread song song.
Quản lý kết nối: Tắt đột ngột Client ở Máy 2, chỉ cho giảng viên xem log tại Server ghi nhận ngắt kết nối an toàn mà hệ thống vẫn hoạt động bình thường.

