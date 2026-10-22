-- Database creation for Library Management System
CREATE DATABASE IF NOT EXISTS `library_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `library_db`;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS `users` (
    `user_id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `full_name` VARCHAR(100) NOT NULL,
    `role` VARCHAR(20) NOT NULL DEFAULT 'STAFF', -- ADMIN, STAFF
    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Categories Table
CREATE TABLE IF NOT EXISTS `categories` (
    `category_id` INT AUTO_INCREMENT PRIMARY KEY,
    `category_name` VARCHAR(100) NOT NULL UNIQUE,
    `description` TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Books Table
CREATE TABLE IF NOT EXISTS `books` (
    `book_id` INT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(200) NOT NULL,
    `author` VARCHAR(100) NOT NULL,
    `category_id` INT,
    `publisher` VARCHAR(100),
    `publish_year` INT,
    `quantity` INT NOT NULL DEFAULT 0,
    `available_quantity` INT NOT NULL DEFAULT 0,
    `isbn` VARCHAR(20),
    FOREIGN KEY (`category_id`) REFERENCES `categories`(`category_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Readers Table
CREATE TABLE IF NOT EXISTS `readers` (
    `reader_id` INT AUTO_INCREMENT PRIMARY KEY,
    `full_name` VARCHAR(100) NOT NULL,
    `phone` VARCHAR(20),
    `email` VARCHAR(100),
    `address` VARCHAR(200),
    `card_status` VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, LOCKED, EXPIRED
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Borrowings Table
CREATE TABLE IF NOT EXISTS `borrowings` (
    `borrowing_id` INT AUTO_INCREMENT PRIMARY KEY,
    `reader_id` INT NOT NULL,
    `user_id` INT,
    `borrow_date` DATE NOT NULL,
    `due_date` DATE NOT NULL,
    `return_date` DATE,
    `status` VARCHAR(20) DEFAULT 'BORROWED', -- BORROWED, RETURNED, OVERDUE
    `fine_amount` DECIMAL(10,2) DEFAULT 0.00,
    FOREIGN KEY (`reader_id`) REFERENCES `readers`(`reader_id`),
    FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Borrowing Details Table
CREATE TABLE IF NOT EXISTS `borrowing_details` (
    `detail_id` INT AUTO_INCREMENT PRIMARY KEY,
    `borrowing_id` INT NOT NULL,
    `book_id` INT NOT NULL,
    `quantity` INT DEFAULT 1,
    `returned_quantity` INT DEFAULT 0,
    `status` VARCHAR(20) DEFAULT 'BORROWED', -- BORROWED, RETURNED
    FOREIGN KEY (`borrowing_id`) REFERENCES `borrowings`(`borrowing_id`) ON DELETE CASCADE,
    FOREIGN KEY (`book_id`) REFERENCES `books`(`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Fines Table
CREATE TABLE IF NOT EXISTS `fines` (
    `fine_id` INT AUTO_INCREMENT PRIMARY KEY,
    `borrowing_id` INT NOT NULL,
    `amount` DECIMAL(10,2) NOT NULL,
    `status` VARCHAR(20) DEFAULT 'UNPAID', -- UNPAID, PAID
    `reason` VARCHAR(255),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`borrowing_id`) REFERENCES `borrowings`(`borrowing_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- SAMPLE SEED DATA
-- =====================================================================

-- 1. Users (10 Quản trị viên & Thủ thư)
INSERT INTO `users` (`user_id`, `username`, `password`, `full_name`, `role`, `status`) VALUES
(1, 'admin', 'admin123', 'Quản trị viên hệ thống', 'ADMIN', 'ACTIVE'),
(2, 'staff', 'staff123', 'Nhân viên thư viện 1', 'STAFF', 'ACTIVE'),
(3, 'thuthu_lan', '123456', 'Nguyễn Hương Lan', 'STAFF', 'ACTIVE'),
(4, 'thuthu_minh', '123456', 'Trần Quang Minh', 'STAFF', 'ACTIVE'),
(5, 'thuthu_hoa', '123456', 'Lê Thị Hoa', 'STAFF', 'ACTIVE'),
(6, 'thuthu_dung', '123456', 'Hoàng Quốc Dũng', 'STAFF', 'ACTIVE'),
(7, 'thuthu_thao', '123456', 'Phạm Phương Thảo', 'STAFF', 'ACTIVE'),
(8, 'manager_tuan', '123456', 'Vũ Anh Tuấn', 'ADMIN', 'ACTIVE'),
(9, 'thuthu_vy', '123456', 'Đỗ Thảo Vy', 'STAFF', 'ACTIVE'),
(10, 'thuthu_khang', '123456', 'Bùi Tuấn Khang', 'STAFF', 'ACTIVE')
ON DUPLICATE KEY UPDATE `full_name`=VALUES(`full_name`), `role`=VALUES(`role`), `status`=VALUES(`status`);

-- 2. Categories (15 Thể loại chi tiết)
INSERT INTO `categories` (`category_id`, `category_name`, `description`) VALUES
(1, 'Lập trình & Kỹ thuật phần mềm', 'Sách về ngôn ngữ lập trình, kiến trúc phần mềm, Clean Code, Web & Mobile'),
(2, 'Kinh tế, Tài chính & Khởi nghiệp', 'Quản trị kinh doanh, đầu tư chứng khoán, khởi nghiệp tinh gọn & tiếp thị'),
(3, 'Văn học Việt Nam kinh điển & hiện đại', 'Tiểu thuyết, truyện ngắn, tác phẩm hiện thực phê phán & văn học đương đại'),
(4, 'Ngoại ngữ & Du học', 'Giáo trình tiếng Anh, TOEIC, IELTS, Tiếng Nhật, Hàn, Trung'),
(5, 'Trí tuệ nhân tạo & Data Science', 'Machine Learning, Deep Learning, Big Data, Khoa học dữ liệu & NLP'),
(6, 'Kỹ năng sống & Phát triển bản thân', 'Tư duy phản biện, kỹ năng giao tiếp, quản lý thời gian & lãnh đạo'),
(7, 'Lịch sử, Văn hóa & Địa lý', 'Lịch sử hào hùng Việt Nam, văn minh thế giới & địa lý các vùng miền'),
(8, 'Tâm lý học & Triết học ứng dụng', 'Tâm lý học hành vi, nghệ thuật sống khắc kỷ & khoa học não bộ'),
(9, 'Khoa học tự nhiên & Kỹ thuật', 'Vật lý hiện đại, hóa học ứng dụng, toán học & viễn thông'),
(10, 'Y học, Dinh dưỡng & Sức khỏe', 'Y học thường thức, chế độ ăn khoa học, rèn luyện thân thể & sơ cứu'),
(11, 'Văn học Nước ngoài & Best-seller', 'Tác phẩm văn học kinh điển thế giới, tiểu thuyết trinh thám & viễn tưởng'),
(12, 'Truyện tranh & Văn học thiếu nhi', 'Văn học thiếu nhi, truyện cổ tích nhân văn & truyện phiêu lưu'),
(13, 'Mạng máy tính & An toàn thông tin', 'Giao thức mạng TCP/IP, bảo mật hệ thống, an ninh mạng & DevOps'),
(14, 'Thiết kế Đồ họa, UI/UX & Mỹ thuật', 'Thiết kế giao diện người dùng, tư duy thẩm mỹ thị giác & bố cục đồ họa'),
(15, 'Quản trị Dự án & Lãnh đạo', 'Phương pháp Agile/Scrum, quản lý rủi ro & thuật lãnh đạo nhóm')
ON DUPLICATE KEY UPDATE `category_name`=VALUES(`category_name`), `description`=VALUES(`description`);

-- 3. Books (105 Đầu sách phong phú đa ngành)
INSERT INTO `books` (`book_id`, `title`, `author`, `category_id`, `publisher`, `publish_year`, `quantity`, `available_quantity`, `isbn`) VALUES
-- 1: Lập trình & Kỹ thuật phần mềm
(101, 'Lập trình Java Socket & Mạng máy tính', 'Nguyễn Văn A', 1, 'NXB Giáo Dục', 2023, 15, 12, '978-604-01-2345'),
(102, 'Cơ sở dữ liệu MySQL nâng cao', 'Trần Thị B', 1, 'NXB Thống Kê', 2022, 12, 10, '978-604-02-6789'),
(103, 'Thiết kế kiến trúc hệ thống Client-Server', 'Đặng Hoàng Giang', 1, 'NXB Khoa Học Kỹ Thuật', 2023, 10, 8, '978-604-03-3456'),
(104, 'Cấu trúc dữ liệu và giải thuật trong Java', 'Phạm Văn Ất', 1, 'NXB Bách Khoa', 2021, 20, 15, '978-604-04-7890'),
(105, 'Lập trình Web với Spring Boot & React', 'Lê Hữu Dũng', 1, 'NXB Thông Tin & Truyền Thông', 2024, 18, 14, '978-604-05-1122'),
(106, 'Clean Code - Nghệ thuật viết mã sạch', 'Robert C. Martin', 1, 'NXB Dân Trí', 2022, 25, 20, '978-604-06-8877'),
(107, 'Design Patterns - Mẫu thiết kế trong lập trình', 'Erich Gamma', 1, 'NXB Bách Khoa', 2020, 15, 11, '978-604-07-2233'),
(108, 'Lập trình Python từ cơ bản đến chuyên sâu', 'Nguyễn Thanh Thủy', 1, 'NXB Giáo Dục', 2023, 22, 18, '978-604-08-4455'),
(109, 'Lập trình di động đa nền tảng với Flutter', 'Trần Anh Tuấn', 1, 'NXB Thông Tin & Truyền Thông', 2024, 16, 13, '978-604-09-1234'),
(110, 'Microservices Architecture trong thực tế', 'Chris Richardson', 1, 'NXB Khoa Học Kỹ Thuật', 2023, 14, 11, '978-604-10-2345'),
(111, 'Tối ưu hóa hiệu năng ứng dụng Java', 'Scott Oaks', 1, 'NXB Bách Khoa', 2022, 12, 9, '978-604-11-3456'),
(112, 'Lập trình C++ hiện đại và Lập trình thi đấu', 'Lê Minh Hoàng', 1, 'NXB Đại Học Quốc Gia', 2021, 25, 20, '978-604-12-4567'),

-- 5: Trí tuệ nhân tạo & Data Science
(113, 'Trí tuệ nhân tạo (AI) trong kỷ nguyên số', 'Stuart Russell & Peter Norvig', 5, 'NXB Tri Thức', 2023, 14, 10, '978-604-13-5678'),
(114, 'Học máy và Ứng dụng thực tiễn (Machine Learning)', 'Vũ Hữu Tiệp', 5, 'NXB Bách Khoa', 2022, 16, 12, '978-604-14-6789'),
(115, 'Xử lý dữ liệu lớn với Apache Spark & Hadoop', 'Trần Văn Khôi', 5, 'NXB Thông Tin & Truyền Thông', 2023, 10, 8, '978-604-15-7890'),
(116, 'Deep Learning căn bản và nâng cao', 'Ian Goodfellow', 5, 'NXB Khoa Học Kỹ Thuật', 2022, 15, 11, '978-604-16-8901'),
(117, 'Xử lý ngôn ngữ tự nhiên (NLP) với Transformers', 'Lewis Tunstall', 5, 'NXB Thông Tin & Truyền Thông', 2024, 12, 10, '978-604-17-9012'),
(118, 'Thị giác máy tính với OpenCV và PyTorch', 'Adrian Rosebrock', 5, 'NXB Bách Khoa', 2023, 14, 11, '978-604-18-0123'),
(119, 'Khoa học dữ liệu với Python và Pandas', 'Wes McKinney', 5, 'NXB Lao Động', 2023, 20, 16, '978-604-19-1234'),

-- 13: Mạng máy tính & An toàn thông tin
(120, 'An toàn thông tin và Phòng chống tấn công mạng', 'Hà Duyên Châu', 13, 'NXB Công An Nhân Dân', 2024, 12, 9, '978-604-20-2345'),
(121, 'Giáo trình Mạng máy tính và Giao thức TCP/IP', 'James F. Kurose', 13, 'NXB Giáo Dục', 2022, 30, 24, '978-604-21-3456'),
(122, 'Thực hành Hacking đạo đức và Kiểm thử xâm nhập', 'Georgia Weidman', 13, 'NXB Thông Tin & Truyền Thông', 2023, 15, 12, '978-604-22-4567'),
(123, 'Quản trị hệ thống Linux và DevOps', 'Michael Jang', 13, 'NXB Bách Khoa', 2023, 18, 14, '978-604-23-5678'),
(124, 'Điện toán đám mây với AWS và Docker', 'Scott Coulton', 13, 'NXB Lao Động', 2024, 16, 13, '978-604-24-6789'),

-- 2: Kinh tế, Tài chính & Khởi nghiệp
(125, 'Kinh tế vĩ mô ứng dụng', 'Lê Văn C', 2, 'NXB Kinh Tế', 2023, 10, 8, '978-604-25-7890'),
(126, 'Chiến lược đại dương xanh', 'W. Chan Kim & Renée Mauborgne', 2, 'NXB Lao Động', 2021, 14, 11, '978-604-26-8901'),
(127, 'Khởi nghiệp tinh gọn (The Lean Startup)', 'Eric Ries', 2, 'NXB Trẻ', 2022, 16, 13, '978-604-27-9012'),
(128, 'Từ tốt đến vĩ đại (Good to Great)', 'Jim Collins', 2, 'NXB Trẻ', 2020, 20, 16, '978-604-28-0123'),
(129, 'Marketing 5.0 - Công nghệ vị nhân sinh', 'Philip Kotler', 2, 'NXB Trẻ', 2023, 18, 15, '978-604-29-1234'),
(130, 'Nhà đầu tư thông minh (The Intelligent Investor)', 'Benjamin Graham', 2, 'NXB Tài Chính', 2021, 15, 10, '978-604-30-2345'),
(131, 'Dốc hết trái tim (Pour Your Heart Into It)', 'Howard Schultz', 2, 'NXB Trẻ', 2020, 14, 11, '978-604-31-3456'),
(132, 'Cha giàu cha nghèo (Rich Dad Poor Dad)', 'Robert Kiyosaki', 2, 'NXB Trẻ', 2021, 35, 28, '978-604-32-4567'),
(133, 'Bí mật tư duy triệu phú', 'T. Harv Eker', 2, 'NXB Tổng Hợp TP.HCM', 2022, 25, 20, '978-604-33-5678'),
(134, 'Chiến lược kinh doanh thời kỳ số hóa', 'David L. Rogers', 2, 'NXB Kinh Tế', 2023, 12, 9, '978-604-34-6789'),

-- 3: Văn học Việt Nam kinh điển & hiện đại
(135, 'Số Đỏ', 'Vũ Trọng Phụng', 3, 'NXB Văn Học', 2019, 15, 12, '978-604-35-7890'),
(136, 'Tắt Đèn', 'Ngô Tất Tố', 3, 'NXB Văn Học', 2020, 14, 12, '978-604-36-8901'),
(137, 'Nỗi Buồn Chiến Tranh', 'Bảo Ninh', 3, 'NXB Trẻ', 2022, 14, 12, '978-604-37-9012'),
(138, 'Chiếc Thuyền Ngoài Xa', 'Nguyễn Minh Châu', 3, 'NXB Giáo Dục', 2021, 16, 14, '978-604-38-0123'),
(139, 'Vợ Nhặt và Tuyển tập truyện ngắn Kim Lân', 'Kim Lân', 3, 'NXB Văn Học', 2021, 18, 15, '978-604-39-1234'),
(140, 'Chí Phèo - Tuyển tập tác phẩm Nam Cao', 'Nam Cao', 3, 'NXB Văn Học', 2020, 20, 16, '978-604-40-2345'),
(141, 'Truyện Kiều (Bản hiệu đính kinh điển)', 'Nguyễn Du', 3, 'NXB Hội Nhà Văn', 2021, 25, 22, '978-604-41-3456'),
(142, 'Mắt Biếc', 'Nguyễn Nhật Ánh', 3, 'NXB Trẻ', 2022, 30, 25, '978-604-42-4567'),
(143, 'Cho tôi xin một vé đi tuổi thơ', 'Nguyễn Nhật Ánh', 3, 'NXB Trẻ', 2023, 28, 22, '978-604-43-5678'),
(144, 'Cánh đồng bất tận', 'Nguyễn Ngọc Tư', 3, 'NXB Trẻ', 2021, 16, 13, '978-604-44-6789'),

-- 11: Văn học Nước ngoài & Best-seller
(145, 'Nhà Giả Kim (The Alchemist)', 'Paulo Coelho', 11, 'NXB Hội Nhà Văn', 2021, 30, 24, '978-604-45-7890'),
(146, 'Rừng Na Uy (Norwegian Wood)', 'Haruki Murakami', 11, 'NXB Hội Nhà Văn', 2020, 20, 17, '978-604-46-8901'),
(147, 'Bố Già (The Godfather)', 'Mario Puzo', 11, 'NXB Văn Học', 2021, 18, 14, '978-604-47-9012'),
(148, 'Trăm năm cô đơn', 'Gabriel García Márquez', 11, 'NXB Văn Học', 2022, 15, 11, '978-604-48-0123'),
(149, 'Tiếng chim hót trong bụi mận gai', 'Colleen McCullough', 11, 'NXB Hội Nhà Văn', 2021, 14, 10, '978-604-49-1234'),
(150, 'Ông già và biển cả (The Old Man and the Sea)', 'Ernest Hemingway', 11, 'NXB Văn Học', 2020, 22, 18, '978-604-50-2345'),
(151, 'Gatsby Vĩ Đại (The Great Gatsby)', 'F. Scott Fitzgerald', 11, 'NXB Hội Nhà Văn', 2022, 16, 13, '978-604-51-3456'),
(152, 'Giết con chim nhại (To Kill a Mockingbird)', 'Harper Lee', 11, 'NXB Nhã Nam', 2021, 18, 14, '978-604-52-4567'),
(153, 'Những người khốn khổ (Les Misérables)', 'Victor Hugo', 11, 'NXB Văn Học', 2020, 12, 9, '978-604-53-5678'),
(154, 'Chuông nguyện hồn ai', 'Ernest Hemingway', 11, 'NXB Văn Học', 2021, 14, 11, '978-604-54-6789'),

-- 4: Ngoại ngữ & Du học
(155, 'Luyện thi IELTS Target Band 7.5+', 'Rachel Mitchell', 4, 'NXB Tổng Hợp TP.HCM', 2023, 15, 11, '978-604-55-7890'),
(156, 'Ngữ pháp tiếng Anh toàn thư', 'Mai Lan Hương', 4, 'NXB Đà Nẵng', 2022, 25, 21, '978-604-56-8901'),
(157, '600 Từ vựng TOEIC căn bản và nâng cao', 'Barron', 4, 'NXB Lao Động', 2023, 30, 25, '978-604-57-9012'),
(158, 'Giáo trình Minna no Nihongo Sơ cấp 1', '3A Corporation', 4, 'NXB Trẻ', 2022, 20, 16, '978-604-58-0123'),
(159, 'Giáo trình Minna no Nihongo Sơ cấp 2', '3A Corporation', 4, 'NXB Trẻ', 2022, 18, 14, '978-604-59-1234'),
(160, 'Tiếng Hàn Tổng Hợp dành cho người Việt Nam Sơ cấp 1', 'Cho Hang Rok', 4, 'NXB Đại Học Quốc Gia', 2023, 22, 18, '978-604-60-2345'),
(161, 'Giáo trình Hán ngữ Tập 1 (Quyển Thượng)', 'Dương Ký Châu', 4, 'NXB Giáo Dục', 2022, 25, 20, '978-604-61-3456'),
(162, 'Luyện phát âm tiếng Anh chuẩn giọng Mỹ', 'Ann Cook', 4, 'NXB Tổng Hợp TP.HCM', 2023, 16, 12, '978-604-62-4567'),

-- 6: Kỹ năng sống & Phát triển bản thân
(163, 'Đắc Nhân Tâm (How to Win Friends and Influence People)', 'Dale Carnegie', 6, 'NXB Trẻ', 2020, 35, 26, '978-604-63-5678'),
(164, 'Tuổi trẻ đáng giá bao nhiêu', 'Rosie Nguyễn', 6, 'NXB Nhã Nam', 2022, 22, 18, '978-604-64-6789'),
(165, 'Tư duy nhanh và chậm (Thinking, Fast and Slow)', 'Daniel Kahneman', 6, 'NXB Thế Giới', 2021, 15, 12, '978-604-65-7890'),
(166, '7 Thói quen để thành đạt (The 7 Habits)', 'Stephen R. Covey', 6, 'NXB Tổng Hợp TP.HCM', 2021, 18, 14, '978-604-66-8901'),
(167, 'Đọc vị bất kỳ ai (Get Anyone to Do Anything)', 'David J. Lieberman', 6, 'NXB Lao Động Xã Hội', 2022, 16, 13, '978-604-67-9012'),
(168, 'Sức mạnh của thói quen (The Power of Habit)', 'Charles Duhigg', 6, 'NXB Thế Giới', 2021, 20, 16, '978-604-68-0123'),
(169, 'Càng bình tĩnh càng hạnh phúc', 'Vãn Tình', 6, 'NXB Văn Học', 2023, 22, 18, '978-604-69-1234'),
(170, 'Tinh thần thép trong mọi nghịch cảnh', 'Ryan Holiday', 6, 'NXB Dân Trí', 2022, 14, 11, '978-604-70-2345'),
(171, 'Sức mạnh của sự tập trung tối đa (Deep Work)', 'Cal Newport', 6, 'NXB Trẻ', 2023, 16, 12, '978-604-71-3456'),

-- 7: Lịch sử, Văn hóa & Địa lý
(172, 'Lược sử loài người (Sapiens)', 'Yuval Noah Harari', 7, 'NXB Tri Thức', 2020, 18, 14, '978-604-72-4567'),
(173, 'Việt Nam sử lược', 'Trần Trọng Kim', 7, 'NXB Văn Hóa Thông Tin', 2020, 15, 12, '978-604-73-5678'),
(174, 'Đại Việt sử ký toàn thư', 'Ngô Sĩ Liên', 7, 'NXB Khoa Học Xã Hội', 2019, 10, 8, '978-604-74-6789'),
(175, 'Lược sử tương lai (Homo Deus)', 'Yuval Noah Harari', 7, 'NXB Tri Thức', 2021, 16, 13, '978-604-75-7890'),
(176, 'Súng, Vi trùng và Thép', 'Jared Diamond', 7, 'NXB Tri Thức', 2021, 14, 11, '978-604-76-8901'),
(177, 'Đất rừng phương Nam', 'Đoàn Giỏi', 7, 'NXB Kim Đồng', 2022, 25, 21, '978-604-77-9012'),

-- 8: Tâm lý học & Triết học ứng dụng
(178, 'Tâm lý học về tiền (The Psychology of Money)', 'Morgan Housel', 8, 'NXB Dân Trí', 2022, 15, 11, '978-604-78-0123'),
(179, 'Nghệ thuật sống Stoic (A Guide to the Good Life)', 'William B. Irvine', 8, 'NXB Thế Giới', 2022, 12, 10, '978-604-79-1234'),
(180, 'Đi tìm lẽ sống (Man Search for Meaning)', 'Viktor Frankl', 8, 'NXB Tổng Hợp TP.HCM', 2021, 20, 16, '978-604-80-2345'),
(181, 'Bàn về hạnh phúc (The Art of Happiness)', 'Dalai Lama', 8, 'NXB Tôn Giáo', 2022, 14, 11, '978-604-81-3456'),
(182, 'Khắc kỷ từ Zeno đến Marcus Aurelius', 'Ryan Holiday', 8, 'NXB Dân Trí', 2023, 15, 12, '978-604-82-4567'),
(183, 'Thao túng tâm lý và Nhận biết bẫy thao túng', 'Shannon Thomas', 8, 'NXB Thanh Niên', 2023, 16, 13, '978-604-83-5678'),

-- 9: Khoa học tự nhiên & Kỹ thuật
(184, 'Nguyên lý máy tính và mạng viễn thông', 'Nguyễn Nam Hoàng', 9, 'NXB Bách Khoa', 2023, 15, 12, '978-604-84-6789'),
(185, 'Vật lý đại cương dành cho kỹ sư', 'Lương Duyên Bình', 9, 'NXB Giáo Dục', 2022, 25, 20, '978-604-85-7890'),
(186, 'Hóa học lượng tử đại cương', 'Đào Đình Thức', 9, 'NXB Đại Học Quốc Gia', 2021, 12, 10, '978-604-86-8901'),
(187, 'Vũ trụ trong vỏ hạt dẻ (The Universe in a Nutshell)', 'Stephen Hawking', 9, 'NXB Trẻ', 2021, 16, 13, '978-604-87-9012'),
(188, 'Lược sử thời gian (A Brief History of Time)', 'Stephen Hawking', 9, 'NXB Trẻ', 2022, 18, 14, '978-604-88-0123'),

-- 10: Y học, Dinh dưỡng & Sức khỏe
(189, 'Dinh dưỡng học và Chế độ ăn lành mạnh', 'Trần Văn Tuấn', 10, 'NXB Y Học', 2023, 14, 11, '978-604-89-1234'),
(190, 'Sơ cứu ban đầu và Xử lý tai nạn khẩn cấp', 'Hội Chữ Thập Đỏ', 10, 'NXB Y Học', 2022, 20, 17, '978-604-90-2345'),
(191, 'Nhân tố Enzyme', 'Hiromi Shinya', 10, 'NXB Thế Giới', 2021, 25, 20, '978-604-91-3456'),
(192, 'Giải mã giấc ngủ (Why We Sleep)', 'Matthew Walker', 10, 'NXB Lao Động', 2022, 16, 13, '978-604-92-4567'),
(193, 'Thể dục và Vận động hồi phục cột sống', 'Nguyễn Hữu Nam', 10, 'NXB Thể Dục Thể Thao', 2023, 15, 12, '978-604-93-5678'),

-- 12: Truyện tranh & Thiếu nhi
(194, 'Dế Mèn phiêu lưu ký', 'Tô Hoài', 12, 'NXB Kim Đồng', 2022, 30, 24, '978-604-94-6789'),
(195, 'Không gia đình (Sans Famille)', 'Hector Malot', 12, 'NXB Kim Đồng', 2021, 20, 16, '978-604-95-7890'),
(196, 'Những tấm lòng cao cả', 'Edmondo De Amicis', 12, 'NXB Kim Đồng', 2021, 18, 15, '978-604-96-8901'),
(197, 'Hoàng tử bé (The Little Prince)', 'Antoine de Saint-Exupéry', 12, 'NXB Kim Đồng', 2023, 35, 28, '978-604-97-9012'),
(198, 'Gió qua rặng liễu', 'Kenneth Grahame', 12, 'NXB Kim Đồng', 2022, 15, 12, '978-604-98-0123'),

-- 14: Thiết kế Đồ họa, UI/UX & Mỹ thuật
(199, 'Thiết kế trải nghiệm người dùng (UX Design)', 'Steve Krug', 14, 'NXB Thông Tin & Truyền Thông', 2023, 14, 11, '978-604-99-1234'),
(200, 'Nguyên lý bố cục và màu sắc trong thiết kế', 'Robin Williams', 14, 'NXB Mỹ Thuật', 2022, 15, 12, '978-604-100-2345'),
(201, 'Câu chuyện nghệ thuật (The Story of Art)', 'E.H. Gombrich', 14, 'NXB Dân Trí', 2021, 12, 9, '978-604-101-3456'),

-- 15: Quản trị Dự án & Lãnh đạo
(202, 'Quản trị dự án Agile và Scrum trong phát triển phần mềm', 'Ken Schwaber', 15, 'NXB Bách Khoa', 2023, 16, 13, '978-604-102-4567'),
(203, '21 Nguyên tắc vàng của nghệ thuật lãnh đạo', 'John C. Maxwell', 15, 'NXB Lao Động', 2021, 18, 14, '978-604-103-5678'),
(204, 'Nhà lãnh đạo không chức danh', 'Robin Sharma', 15, 'NXB Trẻ', 2022, 20, 16, '978-604-104-6789'),
(205, 'OKRs - Bí quyết đo lường mục tiêu hiệu quả', 'John Doerr', 15, 'NXB Tổng Hợp TP.HCM', 2023, 15, 12, '978-604-105-7890')
ON DUPLICATE KEY UPDATE `title`=VALUES(`title`), `author`=VALUES(`author`), `category_id`=VALUES(`category_id`), `publisher`=VALUES(`publisher`), `publish_year`=VALUES(`publish_year`), `quantity`=VALUES(`quantity`), `available_quantity`=VALUES(`available_quantity`), `isbn`=VALUES(`isbn`);

-- 4. Readers (50 Độc giả thành viên chính thức)
INSERT INTO `readers` (`reader_id`, `full_name`, `phone`, `email`, `address`, `card_status`) VALUES
(1, 'Phạm Văn Đồng', '0912345678', 'dong.pv@gmail.com', '123 Lê Duẩn, Hải Châu, Đà Nẵng', 'ACTIVE'),
(2, 'Nguyễn Thị Mai', '0987654321', 'mai.nt@gmail.com', '45 Nguyễn Thị Minh Khai, Đà Nẵng', 'ACTIVE'),
(3, 'Trần Đức Anh', '0905112233', 'anh.td@gmail.com', '88 Hùng Vương, Thanh Khê, Đà Nẵng', 'ACTIVE'),
(4, 'Lê Hoàng Yến', '0935445566', 'yen.lh@gmail.com', '210 Điện Biên Phủ, Đà Nẵng', 'ACTIVE'),
(5, 'Võ Quốc Cường', '0978998877', 'cuong.vq@gmail.com', '15 Quang Trung, Hải Châu, Đà Nẵng', 'ACTIVE'),
(6, 'Phan Bảo Châu', '0914223344', 'chau.pb@gmail.com', '68 Bạch Đằng, Đà Nẵng', 'ACTIVE'),
(7, 'Đỗ Mạnh Hùng', '0945667788', 'hung.dm@gmail.com', '102 Trưng Nữ Vương, Đà Nẵng', 'ACTIVE'),
(8, 'Bùi Thu Hà', '0967889900', 'ha.bt@gmail.com', '34 Phan Châu Trinh, Đà Nẵng', 'ACTIVE'),
(9, 'Huỳnh Gia Huy', '0903123456', 'huy.hg@gmail.com', '56 Trần Phú, Đà Nẵng', 'ACTIVE'),
(10, 'Nguyễn Ngọc Trâm', '0922334455', 'tram.nn@gmail.com', '79 Hoàng Diệu, Đà Nẵng', 'ACTIVE'),
(11, 'Dương Tuấn Kiệt', '0918776655', 'kiet.dt@gmail.com', '150 Nguyễn Văn Linh, Đà Nẵng', 'ACTIVE'),
(12, 'Đặng Thùy Trang', '0933221144', 'trang.dt@gmail.com', '92 Ngô Quyền, Sơn Trà, Đà Nẵng', 'ACTIVE'),
(13, 'Vũ Hoàng Nam', '0981223355', 'nam.vh@gmail.com', '412 Tôn Đức Thắng, Liên Chiểu, Đà Nẵng', 'ACTIVE'),
(14, 'Hồ Thị Bích Ngọc', '0908771122', 'ngoc.htb@gmail.com', '35 Hà Huy Tập, Thanh Khê, Đà Nẵng', 'ACTIVE'),
(15, 'Lý Quốc Bảo', '0944556677', 'bao.lq@gmail.com', '72 Lê Độ, Thanh Khê, Đà Nẵng', 'ACTIVE'),
(16, 'Trịnh Thu Thảo', '0966112244', 'thao.tt@gmail.com', '18 Núi Thành, Hải Châu, Đà Nẵng', 'ACTIVE'),
(17, 'Đinh Hữu Phúc', '0971239876', 'phuc.dh@gmail.com', '255 Trường Chinh, Cẩm Lệ, Đà Nẵng', 'ACTIVE'),
(18, 'Lương Minh Quân', '0938665544', 'quan.lm@gmail.com', '14 Cách Mạng Tháng 8, Đà Nẵng', 'ACTIVE'),
(19, 'Tạ Kim Oanh', '0919443322', 'oanh.tk@gmail.com', '63 Võ Văn Kiệt, Sơn Trà, Đà Nẵng', 'ACTIVE'),
(20, 'Đoàn Xuân Thành', '0909554433', 'thanh.dx@gmail.com', '105 Ông Ích Khiêm, Hải Châu, Đà Nẵng', 'ACTIVE'),
(21, 'Nguyễn Thành Long', '0905223344', 'long.nt@gmail.com', '182 Ngũ Hành Sơn, Đà Nẵng', 'ACTIVE'),
(22, 'Hoàng Thúy Vy', '0935118899', 'vy.ht@gmail.com', '45 An Thượng 2, Ngũ Hành Sơn, Đà Nẵng', 'ACTIVE'),
(23, 'Lê Bá Khánh Trình', '0914667788', 'trinh.lbk@gmail.com', '290 Dũng Sĩ Thanh Khê, Đà Nẵng', 'ACTIVE'),
(24, 'Vũ Thị Kim Liên', '0988334455', 'lien.vtk@gmail.com', '64 Lê Thanh Nghị, Hải Châu, Đà Nẵng', 'ACTIVE'),
(25, 'Trần Đình Trọng', '0976554433', 'trong.td@gmail.com', '118 Huỳnh Thúc Kháng, Hải Châu, Đà Nẵng', 'ACTIVE'),
(26, 'Phạm Ngọc Lan Anh', '0903887766', 'anh.pnl@gmail.com', '85 Hàm Nghi, Thanh Khê, Đà Nẵng', 'ACTIVE'),
(27, 'Đỗ Quốc Việt', '0947223311', 'viet.dq@gmail.com', '240 Lê Duẩn, Thanh Khê, Đà Nẵng', 'ACTIVE'),
(28, 'Bùi Quang Huy', '0968112233', 'huy.bq@gmail.com', '12 Phan Đăng Lưu, Hải Châu, Đà Nẵng', 'ACTIVE'),
(29, 'Huỳnh Thị Bích Thủy', '0917998877', 'thuy.htb@gmail.com', '52 Nguyễn Hữu Thọ, Cẩm Lệ, Đà Nẵng', 'ACTIVE'),
(30, 'Phan Văn Đức', '0932445566', 'duc.pv@gmail.com', '310 Tôn Đức Thắng, Liên Chiểu, Đà Nẵng', 'ACTIVE'),
(31, 'Ngô Bảo Châu', '0909113355', 'chau.nb@gmail.com', '77 Lý Thường Kiệt, Hải Châu, Đà Nẵng', 'ACTIVE'),
(32, 'Đinh Thị Mỹ Hạnh', '0925667788', 'hanh.dtm@gmail.com', '19 Tô Ngọc Vân, Thanh Khê, Đà Nẵng', 'ACTIVE'),
(33, 'Mai Văn Phước', '0984776655', 'phuoc.mv@gmail.com', '48 Trần Quý Cáp, Hải Châu, Đà Nẵng', 'ACTIVE'),
(34, 'Trương Hoàng Long', '0943556677', 'long.th@gmail.com', '134 Nguyễn Tri Phương, Đà Nẵng', 'ACTIVE'),
(35, 'Cao Thị Cẩm Tú', '0915332211', 'tu.ctc@gmail.com', '89 Phan Bội Châu, Hải Châu, Đà Nẵng', 'ACTIVE'),
(36, 'Lâm Tuấn Hưng', '0969887766', 'hung.lt@gmail.com', '222 Âu Cơ, Liên Chiểu, Đà Nẵng', 'ACTIVE'),
(37, 'Châu Kiều Trang', '0973445566', 'trang.ck@gmail.com', '61 Chu Văn An, Hải Châu, Đà Nẵng', 'ACTIVE'),
(38, 'Hà Vĩnh Khang', '0906223344', 'khang.hv@gmail.com', '107 Hoàng Hoa Thám, Thanh Khê, Đà Nẵng', 'ACTIVE'),
(39, 'Tăng Thanh Hà', '0938112233', 'ha.tt@gmail.com', '42 Pasteur, Hải Châu, Đà Nẵng', 'ACTIVE'),
(40, 'Võ Văn Kiệt', '0912665544', 'kiet.vv@gmail.com', '335 Cách Mạng Tháng 8, Cẩm Lệ, Đà Nẵng', 'ACTIVE'),
(41, 'Lê Thị Hồng Nhung', '0986998877', 'nhung.lth@gmail.com', '16 Đống Đa, Hải Châu, Đà Nẵng', 'ACTIVE'),
(42, 'Dương Triệu Vũ', '0945113355', 'vu.dt@gmail.com', '84 Triệu Nữ Vương, Hải Châu, Đà Nẵng', 'ACTIVE'),
(43, 'Nguyễn Hữu Thọ', '0978224466', 'tho.nh@gmail.com', '126 Nguyễn Lương Bằng, Liên Chiểu, Đà Nẵng', 'ACTIVE'),
(44, 'Hồ Xuân Hương', '0907335577', 'huong.hx@gmail.com', '95 Lê Lợi, Hải Châu, Đà Nẵng', 'ACTIVE'),
(45, 'Trịnh Công Sơn', '0934668800', 'son.tc@gmail.com', '47 Quang Dũng, Thanh Khê, Đà Nẵng', 'ACTIVE'),
(46, 'Bùi Bích Phương', '0916554433', 'phuong.bb@gmail.com', '280 Mai Đăng Chơn, Ngũ Hành Sơn, Đà Nẵng', 'ACTIVE'),
(47, 'Hoàng Nam Tiến', '0965332211', 'tien.hn@gmail.com', 'FPT City, Ngũ Hành Sơn, Đà Nẵng', 'ACTIVE'),
(48, 'Phạm Nhật Vượng', '0989112233', 'vuong.pn@gmail.com', 'Vincom Plaza, Ngô Quyền, Sơn Trà, Đà Nẵng', 'ACTIVE'),
(49, 'Nguyễn Thị Phương Thảo', '0904889900', 'thao.ntp@gmail.com', 'Furama Resort, Ngũ Hành Sơn, Đà Nẵng', 'ACTIVE'),
(50, 'Đoàn Nguyên Đức', '0913778899', 'duc.dn@gmail.com', '51 Hoàng Diệu, Hải Châu, Đà Nẵng', 'ACTIVE')
ON DUPLICATE KEY UPDATE `full_name`=VALUES(`full_name`), `phone`=VALUES(`phone`), `email`=VALUES(`email`), `address`=VALUES(`address`), `card_status`=VALUES(`card_status`);

-- 5. Borrowings (35 Phiếu mượn đa dạng trạng thái)
INSERT INTO `borrowings` (`borrowing_id`, `reader_id`, `user_id`, `borrow_date`, `due_date`, `return_date`, `status`, `fine_amount`) VALUES
(501, 1, 2, '2026-10-10', '2026-10-24', NULL, 'BORROWED', 0.00),
(502, 2, 2, '2026-10-05', '2026-10-19', '2026-10-18', 'RETURNED', 0.00),
(503, 3, 1, '2026-09-25', '2026-10-09', NULL, 'OVERDUE', 15000.00),
(504, 4, 3, '2026-10-15', '2026-10-29', NULL, 'BORROWED', 0.00),
(505, 5, 1, '2026-10-18', '2026-11-01', NULL, 'BORROWED', 0.00),
(506, 6, 2, '2026-09-20', '2026-10-04', '2026-10-03', 'RETURNED', 0.00),
(507, 7, 3, '2026-09-15', '2026-09-29', NULL, 'OVERDUE', 25000.00),
(508, 8, 2, '2026-10-12', '2026-10-26', NULL, 'BORROWED', 0.00),
(509, 9, 1, '2026-10-14', '2026-10-28', NULL, 'BORROWED', 0.00),
(510, 10, 3, '2026-10-01', '2026-10-15', '2026-10-14', 'RETURNED', 0.00),
(511, 11, 2, '2026-10-16', '2026-10-30', NULL, 'BORROWED', 0.00),
(512, 12, 1, '2026-10-19', '2026-11-02', NULL, 'BORROWED', 0.00),
(513, 13, 3, '2026-09-28', '2026-10-12', '2026-10-11', 'RETURNED', 0.00),
(514, 14, 2, '2026-10-20', '2026-11-03', NULL, 'BORROWED', 0.00),
(515, 15, 1, '2026-09-10', '2026-09-24', NULL, 'OVERDUE', 35000.00),
(516, 16, 4, '2026-10-02', '2026-10-16', '2026-10-15', 'RETURNED', 0.00),
(517, 17, 4, '2026-10-17', '2026-10-31', NULL, 'BORROWED', 0.00),
(518, 18, 5, '2026-09-18', '2026-10-02', NULL, 'OVERDUE', 30000.00),
(519, 19, 5, '2026-10-11', '2026-10-25', NULL, 'BORROWED', 0.00),
(520, 20, 2, '2026-10-08', '2026-10-22', '2026-10-21', 'RETURNED', 0.00),
(521, 21, 3, '2026-10-13', '2026-10-27', NULL, 'BORROWED', 0.00),
(522, 22, 1, '2026-10-21', '2026-11-04', NULL, 'BORROWED', 0.00),
(523, 23, 4, '2026-09-22', '2026-10-06', NULL, 'OVERDUE', 20000.00),
(524, 24, 2, '2026-10-04', '2026-10-18', '2026-10-17', 'RETURNED', 0.00),
(525, 25, 5, '2026-10-19', '2026-11-02', NULL, 'BORROWED', 0.00),
(526, 26, 3, '2026-10-16', '2026-10-30', NULL, 'BORROWED', 0.00),
(527, 27, 2, '2026-10-07', '2026-10-21', '2026-10-20', 'RETURNED', 0.00),
(528, 28, 1, '2026-09-05', '2026-09-19', NULL, 'OVERDUE', 50000.00),
(529, 29, 4, '2026-10-14', '2026-10-28', NULL, 'BORROWED', 0.00),
(530, 30, 5, '2026-10-15', '2026-10-29', NULL, 'BORROWED', 0.00),
(531, 31, 2, '2026-10-09', '2026-10-23', NULL, 'BORROWED', 0.00),
(532, 32, 3, '2026-09-12', '2026-09-26', NULL, 'OVERDUE', 40000.00),
(533, 33, 1, '2026-10-18', '2026-11-01', NULL, 'BORROWED', 0.00),
(534, 34, 4, '2026-10-03', '2026-10-17', '2026-10-16', 'RETURNED', 0.00),
(535, 35, 2, '2026-10-20', '2026-11-03', NULL, 'BORROWED', 0.00)
ON DUPLICATE KEY UPDATE `reader_id`=VALUES(`reader_id`), `user_id`=VALUES(`user_id`), `status`=VALUES(`status`), `fine_amount`=VALUES(`fine_amount`);

-- 6. Borrowing Details (45 Chi tiết sách mượn)
INSERT INTO `borrowing_details` (`detail_id`, `borrowing_id`, `book_id`, `quantity`, `returned_quantity`, `status`) VALUES
(1, 501, 101, 1, 0, 'BORROWED'),
(2, 501, 102, 1, 0, 'BORROWED'),
(3, 502, 145, 1, 1, 'RETURNED'),
(4, 503, 104, 1, 0, 'BORROWED'),
(5, 504, 105, 1, 0, 'BORROWED'),
(6, 505, 113, 1, 0, 'BORROWED'),
(7, 506, 156, 1, 1, 'RETURNED'),
(8, 507, 106, 1, 0, 'BORROWED'),
(9, 508, 114, 1, 0, 'BORROWED'),
(10, 509, 125, 1, 0, 'BORROWED'),
(11, 510, 127, 1, 1, 'RETURNED'),
(12, 511, 128, 1, 0, 'BORROWED'),
(13, 512, 129, 1, 0, 'BORROWED'),
(14, 513, 130, 1, 1, 'RETURNED'),
(15, 514, 163, 1, 0, 'BORROWED'),
(16, 515, 108, 1, 0, 'BORROWED'),
(17, 516, 132, 1, 1, 'RETURNED'),
(18, 517, 109, 1, 0, 'BORROWED'),
(19, 518, 110, 1, 0, 'BORROWED'),
(20, 519, 116, 1, 0, 'BORROWED'),
(21, 520, 135, 1, 1, 'RETURNED'),
(22, 521, 141, 1, 0, 'BORROWED'),
(23, 522, 142, 1, 0, 'BORROWED'),
(24, 523, 146, 1, 0, 'BORROWED'),
(25, 524, 147, 1, 1, 'RETURNED'),
(26, 525, 155, 1, 0, 'BORROWED'),
(27, 526, 157, 1, 0, 'BORROWED'),
(28, 527, 164, 1, 1, 'RETURNED'),
(29, 528, 165, 1, 0, 'BORROWED'),
(30, 529, 172, 1, 0, 'BORROWED'),
(31, 530, 178, 1, 0, 'BORROWED'),
(32, 531, 184, 1, 0, 'BORROWED'),
(33, 532, 189, 1, 0, 'BORROWED'),
(34, 533, 194, 1, 0, 'BORROWED'),
(35, 534, 197, 1, 1, 'RETURNED'),
(36, 535, 199, 1, 0, 'BORROWED'),
(37, 501, 103, 1, 0, 'BORROWED'),
(38, 504, 107, 1, 0, 'BORROWED'),
(39, 508, 111, 1, 0, 'BORROWED'),
(40, 511, 120, 1, 0, 'BORROWED'),
(41, 514, 166, 1, 0, 'BORROWED'),
(42, 517, 115, 1, 0, 'BORROWED'),
(43, 521, 143, 1, 0, 'BORROWED'),
(44, 525, 158, 1, 0, 'BORROWED'),
(45, 530, 180, 1, 0, 'BORROWED')
ON DUPLICATE KEY UPDATE `status`=VALUES(`status`), `returned_quantity`=VALUES(`returned_quantity`);

-- 7. Fines (10 Phiếu phạt vi phạm quá hạn & bồi hoàn)
INSERT INTO `fines` (`fine_id`, `borrowing_id`, `amount`, `status`, `reason`) VALUES
(1, 503, 15000.00, 'UNPAID', 'Trễ hạn 13 ngày (Phiếu mượn #503)'),
(2, 507, 25000.00, 'UNPAID', 'Trễ hạn 23 ngày (Phiếu mượn #507)'),
(3, 515, 35000.00, 'UNPAID', 'Trễ hạn 28 ngày (Phiếu mượn #515)'),
(4, 518, 30000.00, 'UNPAID', 'Trễ hạn 20 ngày (Phiếu mượn #518)'),
(5, 523, 20000.00, 'PAID',   'Trễ hạn 16 ngày (Phiếu mượn #523) - Đã thanh toán'),
(6, 528, 50000.00, 'UNPAID', 'Trễ hạn 33 ngày (Phiếu mượn #528)'),
(7, 532, 40000.00, 'UNPAID', 'Trễ hạn 26 ngày (Phiếu mượn #532)'),
(8, 502, 10000.00, 'PAID',   'Làm bẩn nhẹ bìa sách #145 - Đã thu phí xử lý'),
(9, 506, 15000.00, 'PAID',   'Trễ hạn 1 ngày - Đã thanh toán khi trả sách'),
(10, 513, 20000.00, 'PAID',  'Gấp góc hư hỏng trang sách - Đã bồi thường')
ON DUPLICATE KEY UPDATE `amount`=VALUES(`amount`), `status`=VALUES(`status`), `reason`=VALUES(`reason`);
