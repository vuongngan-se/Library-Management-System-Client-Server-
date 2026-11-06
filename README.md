# 📚 Hệ Thống Quản Lý Thư Viện Client–Server (Java Socket, JavaFX & MySQL)

> **Đồ án Môn học**: Lập Trình Mạng (Network Programming)  
> **Kiến trúc**: Mô hình Client–Server Đa Luồng (Multi-Threaded Socket Architecture)  
> **Công nghệ**: Java 17, JavaFX 17 Modern Flat UI, TCP/UDP Socket, Embedded HTTP Server, Hybrid Cryptography (RSA + AES), MySQL & In-Memory Fallback.

---

## 📌 1. Tổng Quan & Sơ Đồ Kiến Trúc Hệ Thống

Hệ thống được thiết kế theo mô hình Client–Server phân tán, tối ưu hóa cho môi trường mạng LAN trường đại học hoặc doanh nghiệp. Toàn bộ các tương tác nghiệp vụ, quản lý trạng thái phiên làm việc và bảo mật đều được truyền tải qua các giao thức mạng tầng Transport (TCP, UDP) và Application (HTTP, Custom JSON Protocol).

```
                            +-------------------------------------------------------------+
                            |                 CÁC THIẾT BỊ CLIENT TRUY CẬP                |
                            +-------------------------------------------------------------+
                                      |                                       |
                   TCP Socket Cổng 8888 (Plaintext / AES-256)      HTTP Cổng 8080 (REST / HTML5)
                   UDP Broadcast Cổng 8889 (Auto-Discovery)        Mã QR Code quét bằng Camera
                                      |                                       |
                                      v                                       v
+----------------------------------------------------------------------------------------------------+
|                                    MÁY CHỦ TRUNG TÂM (JAVA SERVER)                                 |
|                                                                                                    |
|  [UDP Discovery Server]  [Embedded HTTP Server]  [TCP ServerSocket (Port 8888)]                     |
|     (Cổng UDP 8889)         (Cổng HTTP 8080)        (Multi-threaded ClientHandler Pool)            |
|                                                              |                                     |
|  [Security & QoS Layer]                                      |                                     |
|     • Hybrid Cryptography (RSA-2048 Handshake + AES-256 CBC) |                                     |
|     • DoS Defense / Token Bucket Rate Limiter (15 req/s)     |                                     |
|     • Network Latency & Jitter Simulator (0 - 500ms)         |                                     |
|                                                              v                                     |
|  [Dịch vụ Nghiệp vụ (LibraryService)] <----------------------+                                     |
|     • Auth & RBAC  • Quản lý Sách  • Mượn/Trả  • Đặt trước  • Tiền phạt  • Chat Broadcast Event   |
|                               |                                                                    |
|                               v                                                                    |
|  [Tầng CSDL Kép Linh Hoạt]                                                                        |
|     • MySQL Database (HikariCP Connection Pool)  <====>  • In-Memory Store (Tự động Fallback)      |
+----------------------------------------------------------------------------------------------------+
```

---

## 🚀 2. Danh Sách Đầy Đủ Các Chức Năng Đã Triển Khai

Hệ thống được chia thành 2 phân hệ lớn: **Phân Hệ Lập Trình Mạng & Bảo Mật Nâng Cao** và **Phân Hệ Nghiệp Vụ Quản Lý Thư Viện**.

### A. Phân Hệ Lập Trình Mạng & Bảo Mật Socket (Network Core & Security)

1. **Kiến trúc Đa Luồng (Multi-Threaded Server & Thread Pool)**:
   - Server sử dụng `ServerSocket` kết hợp `ExecutorService (newCachedThreadPool)` để lắng nghe và phục vụ đồng thời hàng trăm máy Client mà không gây nghẽn tiến trình.
   - Mỗi Client kết nối được quản lý bằng một tiến trình `ClientHandler` riêng biệt.

2. **Giao thức Truyền tin Tùy biến (Custom Framing JSON Protocol)**:
   - Thiết kế giao thức chuẩn hóa theo cặp đối tượng `Request(action, data)` và `Response(status, message, data)`.
   - Cơ chế đóng khung gói tin: Gửi 4 bytes độ dài `Int` + mảng byte `UTF-8 JSON`, xử lý triệt để hiện tượng dính gói (Packet Framing / Fragmentation) trên luồng TCP.

3. **Tự động Dò tìm Máy chủ bằng UDP Broadcast (UDP Auto-Discovery - Cổng 8889)**:
   - Client không cần phải gõ địa chỉ IP thủ công: Chỉ cần nhấn **"🔍 Dò tìm Server"**, Client sẽ gửi gói tin UDP Broadcast (`DISCOVER_LIBRARY_SERVER`) tới địa chỉ `255.255.255.255:8889`.
   - Máy chủ `DiscoveryServer` lắng nghe trên cổng UDP 8889 lập tức phản hồi gói tin chứa IP và cổng TCP 8888 để Client tự động điền và kết nối tức thì.

4. **Kiểm tra Nhịp Tim Socket & Đo Độ Trễ Mạng Khứ Hồi (RTT Ping/Pong Heartbeat)**:
   - Luồng ngầm định kỳ gửi gói tin `PING` mang timestamp của client đến Server và nhận gói tin `PONG` phản hồi.
   - Đo đạc chính xác thời gian khứ hồi (Round-Trip Time - RTT tính bằng mili-giây) và hiển thị trực tiếp bằng huy hiệu màu (Xanh: `<50ms`, Vàng: `50-150ms`, Đỏ: `>150ms`) ngay trên thanh tiêu đề ứng dụng.

5. **Bộ Soi Gói Tin Mạng Thời Gian Thực (Mini-Wireshark Packet Inspector)**:
   - Bảng giám sát tích hợp trực tiếp ngay trong ứng dụng JavaFX, tự động bắt tất cả các gói tin mạng chạy qua tầng Socket:
     - Giao thức: `TCP`, `UDP`, `HTTP`.
     - Chiều truyền: `TX ➔` (Truyền đi) và `RX ⬅` (Nhận về).
     - Chi tiết: Timestamp chính xác mili-giây, Endpoint (IP:Port), Tên lệnh tóm tắt, Kích thước gói tin (Bytes).
     - Bộ lọc nhanh: Tất cả, Chỉ TCP, Chỉ UDP, ô tìm kiếm từ khóa payload theo thời gian thực.
     - Khung xem chi tiết **Packet Inspector Detail**: Giao diện Terminal tối màu font `Consolas` hiển thị cấu trúc chuỗi JSON hoặc Ciphertext thực tế chạy trên đường truyền.
     - Nút **Tạm dừng / Tiếp tục soi** và nút **Xóa log**.

6. **Mã Hóa Đường Truyền & Chống Nghe Lén (Hybrid Cryptography: RSA-2048 + AES-256)**:
   - Bảo mật kênh truyền Socket theo tiêu chuẩn TLS/HTTPS:
     - **Handshake Bắt tay RSA-2048**: Server khởi tạo cặp khóa RSA; Client gửi lệnh `HANDSHAKE_INIT` để lấy Public Key, sinh khóa phiên đối xứng ngẫu nhiên AES-256, mã hóa khóa này bằng RSA Public Key và gửi về Server qua `HANDSHAKE_EXCHANGE`.
     - **Mã hóa Dòng Dữ liệu AES-256 CBC**: Mỗi gói tin Request/Response được mã hóa khối đối xứng với vector khởi tạo **Random IV 16 bytes** riêng biệt cho từng gói tin (chống tấn công Replay / Pattern Analysis).
     - **Công tắc Toggle trực quan**: Cho phép Bật/Tắt chế độ mã hóa trực tiếp trên tab Mạng:
       - Khi *Tắt (Plaintext)*: Mini-Wireshark soi thấy chuỗi JSON rõ ràng.
       - Khi *Bật (Encrypted)*: Mọi gói tin đổi thành `🔒 ENCRYPTED`, payload trở thành chuỗi Ciphertext vô nghĩa, ngăn chặn hoàn toàn việc bắt trộm mật khẩu và dữ liệu bằng Wireshark trên mạng LAN.

7. **Cổng Web Di Động Tích Hợp (Embedded HTTP Server - Cổng 8080) & Mã QR**:
   - Máy chủ tích hợp sẵn Web Server nội bộ (sử dụng `com.sun.net.httpserver.HttpServer`) chạy độc lập trên cổng `8080`.
   - Cung cấp trang Web Responsive HTML5/CSS3 tương thích hoàn hảo với điện thoại Smartphone (tra cứu danh mục sách, kiểm tra tồn kho khả dụng và số socket đang online) mà **không cần cài đặt JavaFX Client**.
   - Tích hợp thư viện `ZXing` tạo **Mã QR Code** động chứa đường dẫn LAN của Server: Người dùng chỉ cần đưa camera điện thoại quét mã QR là mở được trang Web ngay lập tức.

8. **Mô Phỏng Môi Trường Mạng & Độ Trễ Jitter (Network Latency Simulator)**:
   - Thanh trượt điều khiển mô phỏng độ trễ đường truyền từ `0 ms` đến `500 ms` và dao động `Jitter (±20ms)` thời gian thực.
   - Thể hiện trực quan sự thay đổi của mạng LAN thực tế và tác động tức thì lên huy hiệu RTT của các máy Client đang kết nối.

9. **Cơ Chế Phòng Thủ DoS & Giới Hạn Tần Suất Yêu Cầu (Rate Limiting Defense)**:
   - Tự động kiểm soát tần suất gửi yêu cầu của từng IP/Session trong cửa sổ thời gian 1 giây.
   - Giới hạn an toàn: Tối đa **15 requests/giây**. Nếu Client spam yêu cầu vượt ngưỡng (Request Flooding), Server sẽ tự động chặn và trả về lỗi `429 Too Many Requests` an toàn.
   - Đi kèm nút **"⚡ Test bắn DoS thử"** gửi 25 yêu cầu dồn dập trong 200ms để minh họa trực quan tính năng phòng vệ trước giảng viên.

10. **Bảng Theo Dõi & Quản Lý Phiên Socket Trực Tiếp (Active TCP Sessions Table)**:
    - Bảng giám sát chi tiết danh sách tất cả các Socket Client đang kết nối: Session ID, Địa chỉ IP, Cổng Port, Tên đăng nhập, Vai trò, Thời gian kết nối, Tổng số Request đã thực hiện, Lệnh mạng gần nhất.
    - Tính năng **"⛔ Ngắt kết nối Client (Kick Session)"**: Cho phép Quản trị viên đóng Socket của một Client bất kỳ từ xa.

11. **Kênh Chat Nội Bộ & Phát Sóng Sự Kiện Đa Luồng (Live Event Broadcast)**:
    - Kênh giao tiếp thời gian thực dành cho nhân viên thủ thư và quản trị viên qua Socket riêng (`ChatClientListener`).
    - Hệ thống tự động phát sóng (Broadcast) các sự kiện kho sách theo thời gian thực tới tất cả các Client đang mở (Sự kiện mượn sách mới, trả sách, hoàn tất đặt trước, thông báo quá hạn,...).

---

### B. Phân Hệ Nghiệp Vụ Quản Lý Thư Viện (Library Management System)

1. **Xác Thực & Phân Quyền 3 Cấp Độ (Role-Based Access Control - RBAC)**:
   - **`ADMIN` (Quản trị viên)**: Toàn quyền truy cập tất cả các phân hệ, bao gồm Giám sát Mạng, Quản lý Nhân viên, Cấu hình Giả lập Mạng & DoS.
   - **`STAFF` (Thủ thư)**: Quản lý sách, quản lý độc giả, lập phiếu mượn/trả, đặt trước sách, thu tiền phạt và tham gia Chat nội bộ.
   - **`USER` (Độc giả)**: Chỉ truy cập màn hình Cổng Độc Giả riêng ("Sách Của Tôi"), tra cứu danh mục sách cá nhân và gia hạn mượn; tự động ẩn toàn bộ thanh điều hướng nội bộ.

2. **Đăng Ký Độc Giả Tự Động (Reader Self-Registration)**:
   - Màn hình Đăng nhập tích hợp tab chuyên biệt **"ĐĂNG KÝ ĐỘC GIẢ"**, cho phép bạn đọc tự tạo tài khoản mới (vai trò mặc định `USER`) và tự động liên kết thẻ thư viện.

3. **Cổng Độc Giả (USER Portal - "Sách Của Tôi")**:
   - Giao diện thiết kế riêng cho bạn đọc: Bảng tổng hợp các sách mình đang mượn, ngày mượn, hạn trả và số ngày còn lại.
   - Nút **"Gia hạn sách trực tuyến" (+7 ngày)**: Độc giả tự gia hạn sách mượn trực tiếp mà không cần đến quầy thủ thư.

4. **Quản Lý Danh Mục Sách (Book Management)**:
   - Thêm, sửa, xóa đầu sách với đầy đủ thuộc tính: Mã sách, Tiêu đề, Tác giả, Thể loại, Năm xuất bản, Mã ISBN, Tổng số lượng và Số lượng khả dụng (`availableCopies`).
   - Tìm kiếm sách thời gian thực theo từ khóa Tiêu đề, Tác giả hoặc Thể loại.

5. **Quản Lý Độc Giả (Reader Management)**:
   - Quản lý hồ sơ độc giả: Mã thẻ, Họ tên, Số điện thoại, Email, Địa chỉ, Ngày tạo thẻ và Trạng thái thẻ (`ACTIVE`, `SUSPENDED`).
   - Thêm mới, chỉnh sửa thông tin và tìm kiếm độc giả nhanh chóng.

6. **Quản Lý Mượn & Trả Sách (Circulation Management)**:
   - **Lập phiếu mượn**: Cho phép mượn nhiều sách cùng lúc trên một phiếu mượn; hệ thống tự động kiểm tra số lượng tồn và trừ `availableCopies` trong CSDL.
   - **Trả sách**: Nhận lại sách, tự động khôi phục số lượng tồn kho và cập nhật ngày trả thực tế.
   - **Gia hạn sách tại quầy**: Gia hạn thêm 7 ngày chỉ với 1 click.

7. **Quản Lý Đặt Trước Sách (Reservation Engine)**:
   - Khi một đầu sách đã hết bản khả dụng (`availableCopies == 0`), độc giả có thể gửi yêu cầu đặt trước sách.
   - Khi có độc giả khác trả cuốn sách đó về kho, hệ thống tự động phát hiện và gửi thông báo ưu tiên tới người đặt trước qua kênh thông báo nội bộ.

8. **Quản Lý Tiền Phạt & Vi Phạm (Fine Management)**:
   - Tự động tính tiền phạt khi trả sách quá hạn quy định (mặc định: **5.000 VNĐ / ngày trễ**).
   - Lập phiếu phạt vi phạm cho các trường hợp làm rách, hỏng hoặc mất sách.
   - Xác nhận thu tiền phạt và chuyển trạng thái sang `PAID`.

9. **Bảng Thống Kê Dashboard & Biểu Đồ Trực Quan (Analytics Dashboard)**:
   - 4 Thẻ số liệu tổng quan: Tổng đầu sách, Số sách đang sẵn sàng cho mượn, Tổng số độc giả, Số phiếu mượn đang hoạt động.
   - **Biểu đồ tròn (PieChart)**: Phân bổ cơ cấu sách theo từng thể loại văn học, khoa học, kỹ thuật, lịch sử...
   - **Biểu đồ cột (BarChart)**: Top các cuốn sách được bạn đọc mượn nhiều nhất trong hệ thống kèm Tooltip hiển thị số lượt mượn.
   - Bảng theo dõi các giao dịch mượn/trả phát sinh gần đây nhất.

10. **Chế Độ Giao Diện Kép (Dark Mode / Light Mode)**:
    - Nút chuyển đổi giao diện **Sáng / Tối (Dark Palette)** trên thanh Navigation Bar với bộ màu hiện đại, êm mắt, chuẩn phẳng (Flat UI).

11. **Cơ Chế Cơ Sở Dữ Liệu Kép Linh Hoạt (MySQL + In-Memory Fallback)**:
    - Tự động kết nối cơ sở dữ liệu MySQL qua connection pool `HikariCP`.
    - **Cơ chế Fallback thông minh**: Nếu máy tính chưa bật MySQL Server hoặc sai mật khẩu, hệ thống tự động chuyển sang chế độ dữ liệu trong bộ nhớ (`InMemoryStore`) mà **không làm sập phần mềm**, giúp sinh viên dễ dàng demo ngay cả khi gặp sự cố CSDL!

---

## 📂 3. Cấu Trúc Thư Mục Dự Án

```
.
├── pom.xml                                      # Quản lý thư viện Maven (JavaFX, Jackson, ZXing, HikariCP, JUnit 5)
├── sql/
│   └── schema.sql                               # Kịch bản DDL MySQL: Tạo bảng & dữ liệu mẫu hoàn chỉnh
└── src/
    ├── main/
    │   ├── java/com/library/
    │   │   ├── client/                          # [PHÂN HỆ CLIENT - JAVAFX]
    │   │   │   ├── ClientApp.java               # Lớp khởi chạy ứng dụng JavaFX Client
    │   │   │   ├── controller/
    │   │   │   │   ├── LoginController.java     # Điều khiển Đăng nhập, Đăng ký độc giả & UDP Discovery
    │   │   │   │   └── MainController.java      # Điều khiển toàn bộ màn hình Dashboard, Sách, Mạng, Chat, Soi gói tin
    │   │   │   ├── net/
    │   │   │   │   ├── SocketClient.java        # TCP Client: Quản lý Socket, Bắt tay RSA/AES, Đo Ping RTT
    │   │   │   │   ├── DiscoveryClient.java     # UDP Client: Broadcast tìm Server trong mạng LAN
    │   │   │   │   ├── PacketInspector.java     # Engine thu thập gói tin thời gian thực (Mini-Wireshark)
    │   │   │   │   └── ChatClientListener.java  # Luồng lắng nghe thông điệp Chat đẩy từ Server
    │   │   │   └── util/
    │   │   │       └── QRCodeUtils.java         # Tiện ích sinh ảnh mã QR Code bằng ZXing
    │   │   │
    │   │   ├── common/                          # [GIAO THỨC & MÔ HÌNH CHUNG]
    │   │   │   ├── model/                       # Data Models (Book, Reader, Borrowing, Reservation, Fine, NetworkPacket,...)
    │   │   │   └── protocol/
    │   │   │       ├── Request.java             # Cấu trúc gói tin Yêu cầu
    │   │   │       ├── Response.java            # Cấu trúc gói tin Phản hồi
    │   │   │       ├── NetworkUtils.java        # Xử lý đóng gói mảng byte JSON qua DataStream
    │   │   │       └── CryptoUtils.java         # Mật mã học: Cặp khóa RSA-2048 & Mã hóa đối xứng AES-256 CBC
    │   │   │
    │   │   └── server/                          # [PHÂN HỆ SERVER - MÁY CHỦ SOCKET]
    │   │       ├── ServerApp.java               # Lớp khởi chạy TCP Server, UDP Server & Embedded HTTP Server
    │   │       ├── config/
    │   │       │   └── DatabaseConfig.java      # Cấu hình kết nối MySQL và cơ chế chuyển đổi InMemory
    │   │       ├── dao/
    │   │       │   └── InMemoryStore.java       # Bộ nhớ giả lập CSDL tự động khi không có MySQL
    │   │       ├── net/
    │   │       │   ├── ClientHandler.java       # Xử lý đa luồng từng kết nối, Bắt tay RSA, Giải mã AES, Rate Limiting
    │   │       │   ├── ClientSessionManager.java# Quản lý danh sách các Socket Client đang trực tuyến
    │   │       │   ├── DiscoveryServer.java     # UDP Server: Lắng nghe broadcast cổng 8889 và trả lời IP/Port
    │   │       │   ├── EmbeddedHttpServer.java  # HTTP Server: Cấp phát Web Portal di động cổng 8080 & REST API
    │   │       │   ├── ChatManager.java         # Quản lý phòng chat và phát sóng sự kiện đa luồng
    │   │       │   └── NetworkSimulationConfig.java # Cấu hình độ trễ mạng giả lập & chống DoS
    │   │       └── service/
    │   │           └── LibraryService.java      # Xử lý toàn bộ logic nghiệp vụ thư viện
    │   │
    │   └── resources/
    │       ├── css/
    │       │   ├── style.css                    # Bộ giao diện Modern Flat UI (Chế độ Sáng)
    │       │   └── style-dark.css               # Bộ giao diện Dark Palette (Chế độ Tối)
    │       └── com/library/client/views/
    │           ├── LoginView.fxml               # Màn hình Đăng nhập, Đăng ký Độc giả & Dò tìm Server
    │           └── MainView.fxml                # Màn hình chính Dashboard, Quản lý, Giám sát Mạng & Mini-Wireshark
    │
    └── test/java/com/library/                   # [BỘ KIỂM THỬ TỰ ĐỘNG - 39/39 TESTS PASS]
        ├── client/
        │   ├── MainViewLoadTest.java            # Kiểm tra tải và ánh xạ FXML an toàn
        │   ├── net/
        │   │   ├── PacketInspectorTest.java     # Kiểm tra động cơ thu thập gói tin mạng
        │   │   └── SocketEncryptionTest.java    # Kiểm tra vòng lặp Plaintext ➔ Handshake RSA ➔ Mã hóa AES
        │   └── util/
        │       └── QRCodeUtilsTest.java         # Kiểm tra tạo ma trận mã QR Code
        ├── common/protocol/
        │   ├── CryptoUtilsTest.java             # Kiểm tra thuật toán RSA-2048, AES-256 và Random IV
        │   └── NetworkUtilsTest.java            # Kiểm tra tuần tự hóa dữ liệu JSON qua luồng mạng
        └── server/
            ├── SeedDatabaseTest.java            # Kiểm tra nạp CSDL và cơ chế In-Memory fallback
            ├── net/
            │   ├── DiscoveryNetTest.java        # Kiểm tra mạng UDP Discovery tự động
            │   └── EmbeddedHttpServerTest.java  # Kiểm tra phản hồi HTTP Server cổng 8080
            └── service/
                └── LibraryServiceTest.java      # 22 bài test kiểm tra nghiệp vụ mượn, trả, gia hạn, phạt,...
```

---

## ⚙️ 4. Hướng Dẫn Cài Đặt & Khởi Chạy

### Yêu cầu môi trường:
* **JDK**: Java 17 trở lên.
* **Build Tool**: Apache Maven 3.8+.
* **Cơ sở dữ liệu**: MySQL 8.0+ (Tùy chọn: Nếu không bật MySQL, hệ thống tự chạy In-Memory mượt mà).

---

### Cách 1: Chạy Thử Nghiệm Trên 1 Máy (Localhost)

1. **Khởi tạo CSDL MySQL (Nếu có)**:
   - Mở MySQL Workbench hoặc CLI.
   - Mở và chạy file [`sql/schema.sql`](file:///d:/VKU/VKU/5/LTM/Project/Final/sql/schema.sql) để tạo CSDL `library_db` cùng tài khoản và dữ liệu mẫu.

2. **Khởi chạy Server**:
   Mở cửa sổ Terminal thứ nhất:
   ```bash
   mvn exec:java
   ```
   *(Hoặc chỉ định rõ class chính: `mvn exec:java "-Dexec.mainClass=com.library.server.ServerApp"`)*  
   *(Nếu mật khẩu MySQL khác `123456`, truyền tham số: `"-Ddb.user=root"` `"-Ddb.pass=mat_khau_cua_ban"`)*

3. **Khởi chạy Client**:
   Mở cửa sổ Terminal thứ hai:
   ```bash
   mvn javafx:run
   ```

4. **Tài khoản mặc định đăng nhập hệ thống**:
   | Vai trò | Tên đăng nhập | Mật khẩu | Phân quyền truy cập |
   | :--- | :--- | :--- | :--- |
   | **Quản Trị Viên (ADMIN)** | `admin` | `admin123` | Toàn quyền (Quản trị hệ thống, Quản lý nhân viên, Giám sát mạng & Giả lập DoS) |
   | **Thủ Thư (STAFF)** | `staff` | `staff123` | Nghiệp vụ thư viện (Sách, Độc giả, Mượn trả, Đặt trước, Tiền phạt, Chat nội bộ) |
   | **Độc Giả (USER)** | Tự đăng ký hoặc `reader` | Tự đặt | Cổng độc giả "Sách Của Tôi" (Tra cứu sách đang mượn, Gia hạn mượn trực tuyến) |

---

### Cách 2: Chạy Trên 2 Máy Khác Nhau Qua Mạng LAN / Wi-Fi

1. **Chuẩn bị mạng**: Kết nối cả Máy 1 (Server) và Máy 2 (Client) vào cùng một mạng Wi-Fi hoặc trạm phát Hotspot từ điện thoại.
2. **Kiểm tra IP Máy 1 (Server)**:
   - Trên Máy 1, mở PowerShell/CMD và gõ: `ipconfig`.
   - Tìm dòng `IPv4 Address` (Ví dụ: `192.168.1.15`).
3. **Mở tường lửa Firewall**: Cho phép cổng TCP `8888`, UDP `8889` và HTTP `8080` giao tiếp qua Windows Firewall trên Máy 1.
4. **Khởi chạy Server trên Máy 1**:
   ```bash
   mvn exec:java
   ```
5. **Khởi chạy Client trên Máy 2**:
   ```bash
   mvn javafx:run
   ```
   - Tại màn hình Đăng nhập của Máy 2: Nhấn ngay nút **"🔍 Dò tìm Server"**.
   - Client tự động gửi gói tin UDP broadcast trong mạng LAN, tìm ra Máy 1 (`192.168.1.15:8888`) và tự điền vào form mà **không cần người dùng phải gõ tay**!
   - Nhập tài khoản và nhấn Đăng nhập để sử dụng.

---

## 🎬 5. Kịch Bản Demo Nổi Bật Khi Báo Cáo Trước Giảng Viên

Dưới đây là 7 kịch bản demo mẫu thể hiện trọn vẹn toàn bộ các tính năng cốt lõi của môn học Lập Trình Mạng:

### Kịch bản 1: Tự động phát hiện Máy chủ qua UDP Broadcast (Port 8889)
* **Thao tác**: Tại màn hình Đăng nhập của Client, để trống ô Host và Port. Nhấn nút **"🔍 Dò tìm Server"**.
* **Hiện tượng**: Client phát gói tin UDP Broadcast tìm Server trong mạng LAN. Trong chưa đầy 0.5 giây, địa chỉ IP và Cổng của máy chủ tự động xuất hiện trên màn hình kèm thông báo màu xanh.

### Kịch bản 2: Bảng Soi Gói Tin Mạng Thời Gian Thực (Mini-Wireshark Packet Inspector)
* **Thao tác**: Đăng nhập tài khoản `admin`. Nhấn vào tab **"Mạng"**, cuộn xuống bảng **Mini-Wireshark Packet Inspector**.
* **Hiện tượng**: Mọi thao tác click chuột trên ứng dụng (như tra cứu sách, làm mới danh sách, đăng nhập, đo ping) đều lập tức xuất hiện từng dòng gói tin TCP `TX ➔` (gửi đi) và `RX ⬅` (nhận về).
* **Điểm nhấn**: Nhấp vào một gói tin bất kỳ để giảng viên xem toàn bộ cấu trúc chuỗi JSON thực tế đang chạy trên đường dây mạng ở khung Terminal phía dưới. Thử bấm lọc `TCP`, `UDP`, tìm kiếm từ khóa hoặc bấm Tạm dừng/Tiếp tục.

### Kịch bản 3: Mã Hóa Đường Truyền Socket Hybrid RSA-2048 + AES-256 & Chống Nghe Lén
* **Thao tác**:
  1. Ban đầu để chế độ mặc định *Plaintext*: Bấm tra cứu sách hoặc đăng nhập, chỉ cho giảng viên xem trên bảng Mini-Wireshark các gói tin hiển thị rõ từng trường dữ liệu JSON (nếu có kẻ nghe lén trên mạng LAN sẽ đọc được mật khẩu).
  2. Bấm nút **"🔒 Kích hoạt Mã hóa AES-256"**: Ứng dụng thực hiện Bắt tay Handshake (Server cấp RSA Public Key ➔ Client sinh khóa AES-256 mã hóa gửi về Server). Trạng thái chuyển sang màu xanh: `[🔒 ĐÃ BẢO MẬT (RSA-2048 + AES-256)]`.
  3. Bấm nút **"🧪 Gửi thử Gói tin"** hoặc thử mượn/trả sách: Trên bảng Mini-Wireshark, các gói tin đổi thành `🔒 ENCRYPTED`. Khi nhấp vào xem chi tiết, toàn bộ dữ liệu đã được mã hóa thành Ciphertext bí mật với Random IV 16-byte, kẻ nghe lén trên mạng LAN hoàn toàn không thể giải mã được!

### Kịch bản 4: Cổng Web Di Động (HTTP Server 8080) & Quét Mã QR Bằng Điện Thoại
* **Thao tác**: Mở tab **"Mạng"**, nhấn nút **"📲 QR Di Động"**.
* **Hiện tượng**: Một cửa sổ hiển thị **Mã QR Code** động chứa địa chỉ LAN của máy chủ (ví dụ `http://192.168.1.15:8080`).
* **Trình diễn**: Lấy camera điện thoại di động quét mã QR: Trình duyệt điện thoại lập tức mở ra trang Web Responsive tra cứu đầu sách trực tuyến, xem số lượng sách khả dụng và số socket đang online thời gian thực mà không cần cài đặt JavaFX Client!

### Kịch bản 5: Giả Lập Môi Trường Mạng (Latency Slider) & Phòng Vệ Tấn Công DoS (Rate Limiting)
* **Thao tác 1 (Giả lập trễ)**: Kéo thanh trượt **Độ trễ mạng** lên `250 ms`, tích chọn `Mô phỏng Jitter (±20ms)` và bấm **"⚙️ Áp dụng cấu hình"**. Quan sát huy hiệu Ping trên góc phải ứng dụng chuyển từ màu xanh `<5ms` sang màu đỏ `~260ms`.
* **Thao tác 2 (Phòng vệ DoS)**: Bấm nút **"⚡ Test bắn DoS thử"**: Ứng dụng gửi liên tục 25 request trong tích tắc. Server kích hoạt thuật toán Rate Limiting (chặn trên 15 req/s) và trả về thông báo lỗi phòng vệ an toàn `429 Too Many Requests`, chứng minh hệ thống không bị treo hoặc sập.

### Kịch bản 6: Tính Đồng Bộ Dữ Liệu & Đa Luồng Song Song Giữa 2 Máy Client
* **Thao tác**: Chạy 2 Client trên 2 máy tính khác nhau (hoặc mở 2 cửa sổ Client).
  - Máy 1 (Thủ thư) thực hiện cho mượn 1 cuốn sách (ví dụ cuốn "Lập trình Java Socket").
  - Máy 2 (Độc giả) mở xem danh mục sách: Số lượng khả dụng ngay lập tức giảm đi 1 cuốn.
  - Đồng thời, kênh **Chat nội bộ** ở cả hai máy lập tức nhận được thông báo phát sóng từ hệ thống: *"Phiếu mượn #505 vừa được tạo. Kho sách đã cập nhật tự động"*.

### Kịch bản 7: Quản Lý Socket Đang Hoạt Động & Ngắt Kết Nối Client Từ Xa (Kick Session)
* **Thao tác**: Trên tab **"Mạng"** của tài khoản Admin, xem bảng **Active TCP Sockets**. Chọn một phiên làm việc của máy Client khác và nhấn **"⛔ Ngắt kết nối Client"**.
* **Hiện tượng**: Socket của máy Client đó bị Server đóng an toàn lập tức, danh sách phiên tự động cập nhật giảm đi 1, chứng minh khả năng quản trị kết nối tập trung của máy chủ.

---

## 🏆 6. Kết Quả Kiểm Thử Tự Động (Automated Test Suite)

Dự án được xây dựng với bộ kiểm thử tự động toàn diện bằng **JUnit 5**, bao phủ toàn bộ các tầng Mạng, Mật mã, Giao thức, Web Server và Nghiệp vụ CSDL:

```bash
mvn test
```

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.library.client.MainViewLoadTest (1 test)
[INFO] Running com.library.client.net.PacketInspectorTest (3 tests)
[INFO] Running com.library.client.net.SocketEncryptionTest (1 test)
[INFO] Running com.library.client.util.QRCodeUtilsTest (2 tests)
[INFO] Running com.library.common.protocol.CryptoUtilsTest (3 tests)
[INFO] Running com.library.common.protocol.NetworkUtilsTest (2 tests)
[INFO] Running com.library.server.net.DiscoveryNetTest (1 test)
[INFO] Running com.library.server.net.EmbeddedHttpServerTest (3 tests)
[INFO] Running com.library.server.SeedDatabaseTest (1 test)
[INFO] Running com.library.server.service.LibraryServiceTest (22 tests)
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 39, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```
🎯 **Kết quả**: **39/39 bài test vượt qua thành công (100% Pass)**.
