Để khởi chạy hệ thống Quản lý thư viện Client-Server, hãy thực hiện theo các bước sau:

1. Khởi tạo Cơ sở dữ liệu MySQL
Mở MySQL Workbench hoặc CLI và chạy file kịch bản SQL 
schema.sql(line 1) để tạo database library_db và các bảng mẫu.
2. Chạy Server
Mở terminal tại thư mục gốc dự án và chạy lệnh:

Terminal
mvn exec:java
(Hoặc trên PowerShell nếu chỉ định mainClass: mvn exec:java "-Dexec.mainClass=com.library.server.ServerApp")
*(Nếu MySQL có username/password khác mặc định root/123456, bổ sung "-Ddb.user=root" "-Ddb.pass=your_password")*

3. Chạy Client
Mở cửa sổ terminal mới và chạy ứng dụng giao diện JavaFX bằng lớp 
ClientApp.java(line 1):

Terminal
mvn javafx:run
4. Đăng nhập hệ thống
Host / Port: localhost : 8888
Tài khoản Admin: admin / admin123
Tài khoản Staff: staff / staff123

Các bước triển khai hệ thống trên 2 máy tính trong cùng mạng LAN:

Mạng & Firewall: Kết nối 2 máy vào chung mạng WiFi hoặc LAN. Mở PowerShell trên máy Server chạy ipconfig để lấy IP IPv4 (ví dụ 
192.168.1.15), sau đó cho phép kết nối TCP cổng 8888 qua Windows Firewall.

schema.sql(line 1): Nhập kịch bản cơ sở dữ liệu vào MySQL trên máy Server.

ServerApp.java(line 1): Khởi chạy Server trên máy Server bằng lệnh mvn exec:java -Dexec.mainClass="com.library.server.ServerApp".

ClientApp.java(line 1): Khởi chạy Client trên máy thứ 2 bằng mvn javafx:run, nhập ô Host là IP của máy Server và Port 8888 để đăng nhập.
