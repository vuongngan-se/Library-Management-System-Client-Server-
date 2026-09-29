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

-- Sample Initial Seed Data
-- Plain passwords for default users: admin / admin123, staff / staff123 (stored as plain text or bcrypt-ready)
INSERT INTO `users` (`username`, `password`, `full_name`, `role`) VALUES
('admin', 'admin123', 'Quản trị viên', 'ADMIN'),
('staff', 'staff123', 'Nhân viên thư viện', 'STAFF')
ON DUPLICATE KEY UPDATE `username`=`username`;

INSERT INTO `categories` (`category_name`, `description`) VALUES
('Lập trình', 'Sách về ngôn ngữ lập trình, thuật toán, công nghệ thông tin'),
('Kinh tế', 'Quản trị kinh doanh, tài chính, kế toán'),
('Văn học', 'Tiểu thuyết, truyện ngắn, thơ'),
('Ngoại ngữ', 'Tiếng Anh, Tiếng Nhật, Tiếng Trung')
ON DUPLICATE KEY UPDATE `category_name`=`category_name`;

INSERT INTO `books` (`title`, `author`, `category_id`, `publisher`, `publish_year`, `quantity`, `available_quantity`, `isbn`) VALUES
('Lập trình Java nâng cao', 'Nguyễn Văn A', 1, 'NXB Giáo Dục', 2022, 10, 10, '978-1234567890'),
('Cơ sở dữ liệu MySQL', 'Trần Thị B', 1, 'NXB Thống Kê', 2021, 5, 5, '978-0987654321'),
('Đắc Nhân Tâm', 'Dale Carnegie', 3, 'NXB Trẻ', 2020, 15, 12, '978-1122334455'),
('Kinh tế vĩ mô', 'Lê Văn C', 2, 'NXB Kinh Tế', 2023, 8, 8, '978-5544332211');

INSERT INTO `readers` (`full_name`, `phone`, `email`, `address`, `card_status`) VALUES
('Phạm Văn Đồng', '0912345678', 'dong@gmail.com', 'Đà Nẵng', 'ACTIVE'),
('Nguyễn Thị Mai', '0987654321', 'mai@gmail.com', 'Quảng Nam', 'ACTIVE');
