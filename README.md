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

* **Đăng nhập & Đăng ký**: Phân quyền Quản trị viên (`ADMIN`), Nhân viên (`STAFF`) và Độc giả (`USER`); hỗ trợ độc giả tự đăng ký tài khoản mới trực tiếp.
* **UDP Auto-Discovery**: Client tự động phát hiện địa chỉ IP & Port của Server trong mạng LAN qua gói tin UDP Broadcast (Port 8889) mà không cần gõ IP thủ công.
* **Ping / Pong Heartbeat & Đo độ trễ (RTT)**: Kiểm tra nhịp tim socket định kỳ và hiển thị độ trễ mạng thời gian thực (ms) ngay trên thanh tiêu đề ứng dụng.
* **Cổng Độc giả (USER Portal)**: Tab "Sách Của Tôi" dành riêng cho tài khoản độc giả tra cứu các sách đang mượn và gửi yêu cầu gia hạn trực tuyến (+7 ngày) chỉ với 1 click.
* **Giả lập Môi trường Mạng & Jitter (ADMIN)**: Thanh trượt mô phỏng độ trễ (0 - 500ms) và dao động Jitter (±20ms) thời gian thực trên tab Giám sát Mạng, tác động tức thì lên badge RTT của các máy Client.
* **Cơ chế Phòng thủ DoS & Rate Limiting**: Tự động giới hạn tần suất gửi yêu cầu an toàn (15 req/s) để chống spam socket, kèm nút "Test bắn DoS thử" minh họa trực quan khả năng phòng vệ của Server khi báo cáo đồ án.
* **Cổng Web Di Động (Embedded HTTP Server - Cổng 8080)**: Máy chủ tích hợp sẵn HTTP Server cấp phát trang Web Responsive cho điện thoại smartphone tra cứu đầu sách trực tuyến, xem số lượng bản khả dụng và số socket online mà không cần cài app Client JavaFX; hỗ trợ tạo **Mã QR Code** quét bằng camera điện thoại để truy cập tức thì.
* **Kênh Chat Nội bộ Thời Gian Thực**: Trò chuyện trực tuyến giữa các nhân viên thủ thư và quản trị viên, tự động đồng bộ sự kiện kho sách theo thời gian thực.
* **Quản lý Sách**: Thêm, sửa, xóa, tìm kiếm theo tiêu đề/tác giả/thể loại/ISBN, theo dõi tồn kho và số lượng khả dụng.
* **Quản lý Độc giả**: Thêm, sửa, xem thông tin liên lạc, địa chỉ và trạng thái thẻ độc giả.
* **Quản lý Mượn & Trả Sách**: Lập phiếu mượn nhiều sách cùng lúc, theo dõi hạn trả, xử lý trả sách và khôi phục số lượng tự động.
* **Gia hạn Sách Mượn**: Gia hạn thời hạn mượn thêm 7 ngày trực tiếp trên giao diện chỉ với 1 cú click.
* **Quản lý Tiền phạt & Vi phạm**: Theo dõi danh sách phiếu phạt do trễ hạn/làm hỏng sách, tự động tính tiền phạt khi trả trễ (5.000đ/ngày) và xác nhận thu tiền phạt (`PAID`).
* **Thống kê Dashboard & Biểu đồ trực quan**: Biểu đồ PieChart cơ cấu thể loại, BarChart top sách mượn nhiều nhất có Tooltip chi tiết; hỗ trợ chuyển đổi Giao diện Tối (Dark Mode).
* **Bảng Soi Gói Tin Mạng Thời Gian Thực (Mini-Wireshark Packet Inspector)**: Tích hợp trực tiếp màn hình bắt và phân tích gói tin mạng (TCP Request/Response, UDP Broadcast Discovery, HTTP Web Portal) với đầy đủ Timestamp, Giao thức, Chiều truyền (TX ➔ / RX ⬅), Kích thước (Bytes), Endpoint và Cửa sổ soi Payload JSON chi tiết. Hỗ trợ bộ lọc nhanh theo giao thức (Tất cả, TCP, UDP), tìm kiếm từ khóa payload, tính năng Tạm dừng/Tiếp tục bắt gói tin và Xóa log.
* **Mã Hóa Đường Truyền Socket & Chống Nghe Lén (Hybrid Cryptography: RSA-2048 + AES-256)**: Bảo mật an toàn kênh truyền Socket bằng kiến trúc lai ghép giống chuẩn TLS/HTTPS: Quy trình bắt tay trao đổi khóa bí mật RSA-2048 kết hợp mã hóa luồng dữ liệu Request/Response bằng khối đối xứng AES-256-CBC với IV ngẫu nhiên 16 bytes. Hỗ trợ công tắc Bật/Tắt mã hóa trực tiếp trên giao diện để đối chiếu trực quan sự khác biệt giữa Plaintext và Ciphertext trên bảng Packet Inspector.
* **Chế độ dữ liệu linh hoạt**: Tự động kết nối MySQL (hỗ trợ cả mật khẩu thường lẫn trống), tự động chuyển sang In-Memory Fallback nếu chưa bật CSDL.

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
   mvn exec:java
   ```
   *Hoặc chỉ định rõ class (trên PowerShell cần bọc ngoặc kép cả tham số):*
   ```powershell
   mvn exec:java "-Dexec.mainClass=com.library.server.ServerApp"
   ```
   *(Nếu mật khẩu MySQL khác `123456`, truyền thêm tham số: `"-Ddb.user=root"` `"-Ddb.pass=your_password"`)*

2. **Khởi chạy Client**:
   Mở thêm cửa sổ Terminal mới và chạy:
   ```bash
   mvn javafx:run
   ```

3. **Thông tin đăng nhập & Đăng ký**:
   * **Host / Port**: `localhost` / `8888`
   * **Tài khoản mặc định**:
     * **Admin**: `admin` / `admin123`
     * **Staff**: `staff` / `staff123`
   * **Đăng ký tài khoản Độc giả**: Chọn tab **"ĐĂNG KÝ ĐỘC GIẢ"** ngay trên giao diện để tạo tài khoản độc giả mới (`USER`) phục vụ tra cứu và đặt trước sách. Tài khoản Quản trị viên (`ADMIN`) và Thủ thư (`STAFF`) được phân quyền quản lý trong hệ thống nội bộ.

---

## 5. Hướng Dẫn Chạy Trên 2 Máy Qua Mạng LAN

1. **Chuẩn bị mạng**: Kết nối máy Server và máy Client vào cùng mạng Wi-Fi/LAN.
2. **Lấy IP Máy Server**:
   * Trên máy Server, mở PowerShell/CMD và chạy lệnh `ipconfig`.
   * Tìm địa chỉ IPv4 (Ví dụ: `192.168.1.15`).
3. **Mở cổng Firewall**: Cho phép cổng TCP `8888` hoạt động qua Windows Firewall trên máy Server.
4. **Khởi chạy Server (Trên máy Server)**:
   ```bash
   mvn exec:java
   ```
5. **Khởi chạy Client (Trên máy Client)**:
   ```bash
   mvn javafx:run
   ```
   Tại màn hình Đăng nhập, người dùng chỉ cần nhấn nút **"🔍 Dò tìm Server"** — hệ thống sẽ tự động gửi gói tin UDP Broadcast để tìm IP máy chủ Server và tự động điền vào ô IP/Port mà không cần gõ thủ công! (Hoặc có thể nhập IP thủ công ví dụ `192.168.1.15` và Port `8888`).


### 6. Các Kịch Bản Demo Nổi Bật Dành Cho Giảng Viên

1. **Dò tìm Server tự động bằng UDP Broadcast**:
   - Tại màn hình Login, nhấn nút **"🔍 Dò tìm Server"**. Client tự động broadcast gói tin UDP trên cổng `8889`, nhận phản hồi chứa IP/Port của Server và tự điền mà không cần gõ tay.
2. **Soi Gói Tin Mạng Thời Gian Thực (Mini-Wireshark Packet Inspector)**:
   - Vào tab **"Mạng"** -> Cuộn tới phần **"Mini-Wireshark Packet Inspector"**.
   - Mọi thao tác gửi Request/nhận Response qua TCP, gói broadcast UDP và HTTP request đều lập tức hiện lên bảng theo dõi với timestamp micro-giây, giao thức, chiều gửi/nhận (TX/RX), dung lượng và endpoint.
   - Nhấp vào bất kỳ hàng nào để xem toàn bộ cấu trúc Payload JSON ở khung bên dưới. Dùng bộ lọc `TCP`, `UDP`, tìm kiếm từ khóa, tạm dừng soi hoặc xóa log.
3. **Mã Hóa Đường Truyền & Chống Nghe Lén (Hybrid RSA-2048 + AES-256)**:
   - Ban đầu để chế độ Plaintext: Thực hiện tra cứu sách hoặc đăng nhập, chỉ cho giảng viên xem trên bảng Packet Inspector payload JSON hiển thị rõ ràng từng trường thông tin.
   - Nhấn nút **"🔒 Kích hoạt Mã hóa AES-256"**: Ứng dụng tự động kích hoạt quy trình Bắt tay Handshake (Server cấp RSA Public Key ➔ Client sinh khóa AES-256 và mã hóa gửi về). Huy hiệu chuyển sang `[🔒 ĐÃ BẢO MẬT (RSA-2048 + AES-256)]`.
   - Bấm **"🧪 Gửi thử Gói tin"** hoặc tiếp tục mượn/trả sách: Mọi gói tin gửi/nhận trên socket đều được mã hóa thành Ciphertext (nhãn `🔒 ENCRYPTED`). Dù có kẻ xấu cắm Wireshark trên mạng LAN cũng không thể giải mã hay đọc trộm được dữ liệu.
4. **Cổng Web Di Động (HTTP Server 8080 & Mã QR)**:
   - Mở tab **"Mạng"**, bấm **"Tạo Mã QR Truy Cập"** để sinh mã QR liên kết tới địa chỉ mạng LAN của Server (ví dụ `http://192.168.1.15:8080`).
   - Lấy camera điện thoại quét mã QR hoặc mở trình duyệt web điện thoại: xem ngay danh mục sách và số lượng socket trực tuyến theo thời gian thực mà không cần cài app Client JavaFX.
4. **Giả lập Môi Trường Mạng & Chống DoS (Rate Limiting)**:
   - Kéo thanh trượt **Mô Phỏng Độ Trễ Mạng** (0 - 500ms): huy hiệu độ trễ RTT trên góc phải ứng dụng đổi màu xanh/vàng/đỏ và hiển thị đúng số ms trễ thực tế.
   - Bấm nút **"Test bắn DoS thử"**: Server kích hoạt cơ chế Rate Limiting (chặn spam quá 15 req/s) và trả về thông báo lỗi quá tải `429 Too Many Requests` an toàn.
5. **Đồng bộ Dữ liệu & Đa Luồng trên 2 Máy**:
   - **Đồng bộ dữ liệu**: Máy 1 thực hiện mượn 1 cuốn sách, Máy 2 ấn tra cứu lại ngay lập tức để thấy số lượng tồn trong CSDL giảm đi 1.
   - **Xử lý đa luồng**: Đăng nhập 2 tài khoản tại 2 máy và gửi yêu cầu đồng thời để chứng minh Server lắng nghe và phục vụ song song trên các thread riêng biệt.
   - **Quản lý kết nối**: Tắt đột ngột Client ở Máy 2, chỉ cho giảng viên xem log tại Server ghi nhận ngắt kết nối an toàn mà không làm sập tiến trình Server.

