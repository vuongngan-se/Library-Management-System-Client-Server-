package com.library.server.dao;

import com.library.common.model.*;
import com.library.server.config.DatabaseConfig;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryStore {
    private static final Map<Integer, User> users = new ConcurrentHashMap<>();
    private static final Map<Integer, Category> categories = new ConcurrentHashMap<>();
    private static final Map<Integer, Book> books = new ConcurrentHashMap<>();
    private static final Map<Integer, Reader> readers = new ConcurrentHashMap<>();
    private static final Map<Integer, Borrowing> borrowings = new ConcurrentHashMap<>();
    private static final Map<Integer, Fine> fines = new ConcurrentHashMap<>();

    private static final AtomicInteger userIdSeq = new AtomicInteger(100);
    private static final AtomicInteger bookIdSeq = new AtomicInteger(300);
    private static final AtomicInteger readerIdSeq = new AtomicInteger(100);
    private static final AtomicInteger borrowingIdSeq = new AtomicInteger(600);
    private static final AtomicInteger fineIdSeq = new AtomicInteger(100);

    static {
        // 1. Users
        users.put(1, new User(1, "admin", "admin123", "Quản trị viên hệ thống", "ADMIN", "ACTIVE"));
        users.put(2, new User(2, "staff", "staff123", "Nhân viên thư viện 1", "STAFF", "ACTIVE"));
        users.put(3, new User(3, "thuthu_lan", "123456", "Nguyễn Hương Lan", "STAFF", "ACTIVE"));
        users.put(4, new User(4, "thuthu_minh", "123456", "Trần Quang Minh", "STAFF", "ACTIVE"));
        users.put(5, new User(5, "thuthu_hoa", "123456", "Lê Thị Hoa", "STAFF", "ACTIVE"));
        users.put(6, new User(6, "thuthu_dung", "123456", "Hoàng Quốc Dũng", "STAFF", "ACTIVE"));
        users.put(7, new User(7, "thuthu_thao", "123456", "Phạm Phương Thảo", "STAFF", "ACTIVE"));
        users.put(8, new User(8, "manager_tuan", "123456", "Vũ Anh Tuấn", "ADMIN", "ACTIVE"));
        users.put(9, new User(9, "thuthu_vy", "123456", "Đỗ Thảo Vy", "STAFF", "ACTIVE"));
        users.put(10, new User(10, "thuthu_khang", "123456", "Bùi Tuấn Khang", "STAFF", "ACTIVE"));

        // 2. Categories
        categories.put(1, new Category(1, "Lập trình & Kỹ thuật phần mềm", "Sách về ngôn ngữ lập trình, kiến trúc phần mềm, Clean Code, Web & Mobile"));
        categories.put(2, new Category(2, "Kinh tế, Tài chính & Khởi nghiệp", "Quản trị kinh doanh, đầu tư chứng khoán, khởi nghiệp tinh gọn & tiếp thị"));
        categories.put(3, new Category(3, "Văn học Việt Nam kinh điển & hiện đại", "Tiểu thuyết, truyện ngắn, tác phẩm hiện thực phê phán & văn học đương đại"));
        categories.put(4, new Category(4, "Ngoại ngữ & Du học", "Giáo trình tiếng Anh, TOEIC, IELTS, Tiếng Nhật, Hàn, Trung"));
        categories.put(5, new Category(5, "Trí tuệ nhân tạo & Data Science", "Machine Learning, Deep Learning, Big Data, Khoa học dữ liệu & NLP"));
        categories.put(6, new Category(6, "Kỹ năng sống & Phát triển bản thân", "Tư duy phản biện, kỹ năng giao tiếp, quản lý thời gian & lãnh đạo"));
        categories.put(7, new Category(7, "Lịch sử, Văn hóa & Địa lý", "Lịch sử hào hùng Việt Nam, văn minh thế giới & địa lý các vùng miền"));
        categories.put(8, new Category(8, "Tâm lý học & Triết học ứng dụng", "Tâm lý học hành vi, nghệ thuật sống khắc kỷ & khoa học não bộ"));
        categories.put(9, new Category(9, "Khoa học tự nhiên & Kỹ thuật", "Vật lý hiện đại, hóa học ứng dụng, toán học & viễn thông"));
        categories.put(10, new Category(10, "Y học, Dinh dưỡng & Sức khỏe", "Y học thường thức, chế độ ăn khoa học, rèn luyện thân thể & sơ cứu"));
        categories.put(11, new Category(11, "Văn học Nước ngoài & Best-seller", "Tác phẩm văn học kinh điển thế giới, tiểu thuyết trinh thám & viễn tưởng"));
        categories.put(12, new Category(12, "Truyện tranh & Văn học thiếu nhi", "Văn học thiếu nhi, truyện cổ tích nhân văn & truyện phiêu lưu"));
        categories.put(13, new Category(13, "Mạng máy tính & An toàn thông tin", "Giao thức mạng TCP/IP, bảo mật hệ thống, an ninh mạng & DevOps"));
        categories.put(14, new Category(14, "Thiết kế Đồ họa, UI/UX & Mỹ thuật", "Thiết kế giao diện người dùng, tư duy thẩm mỹ thị giác & bố cục đồ họa"));
        categories.put(15, new Category(15, "Quản trị Dự án & Lãnh đạo", "Phương pháp Agile/Scrum, quản lý rủi ro & thuật lãnh đạo nhóm"));

        // 3. Books (105 books)
        // 1: Lập trình & Kỹ thuật phần mềm
        addBook(101, "Lập trình Java Socket & Mạng máy tính", "Nguyễn Văn A", 1, "NXB Giáo Dục", 2023, 15, 12, "978-604-01-2345");
        addBook(102, "Cơ sở dữ liệu MySQL nâng cao", "Trần Thị B", 1, "NXB Thống Kê", 2022, 12, 10, "978-604-02-6789");
        addBook(103, "Thiết kế kiến trúc hệ thống Client-Server", "Đặng Hoàng Giang", 1, "NXB Khoa Học Kỹ Thuật", 2023, 10, 8, "978-604-03-3456");
        addBook(104, "Cấu trúc dữ liệu và giải thuật trong Java", "Phạm Văn Ất", 1, "NXB Bách Khoa", 2021, 20, 15, "978-604-04-7890");
        addBook(105, "Lập trình Web với Spring Boot & React", "Lê Hữu Dũng", 1, "NXB Thông Tin & Truyền Thông", 2024, 18, 14, "978-604-05-1122");
        addBook(106, "Clean Code - Nghệ thuật viết mã sạch", "Robert C. Martin", 1, "NXB Dân Trí", 2022, 25, 20, "978-604-06-8877");
        addBook(107, "Design Patterns - Mẫu thiết kế trong lập trình", "Erich Gamma", 1, "NXB Bách Khoa", 2020, 15, 11, "978-604-07-2233");
        addBook(108, "Lập trình Python từ cơ bản đến chuyên sâu", "Nguyễn Thanh Thủy", 1, "NXB Giáo Dục", 2023, 22, 18, "978-604-08-4455");
        addBook(109, "Lập trình di động đa nền tảng với Flutter", "Trần Anh Tuấn", 1, "NXB Thông Tin & Truyền Thông", 2024, 16, 13, "978-604-09-1234");
        addBook(110, "Microservices Architecture trong thực tế", "Chris Richardson", 1, "NXB Khoa Học Kỹ Thuật", 2023, 14, 11, "978-604-10-2345");
        addBook(111, "Tối ưu hóa hiệu năng ứng dụng Java", "Scott Oaks", 1, "NXB Bách Khoa", 2022, 12, 9, "978-604-11-3456");
        addBook(112, "Lập trình C++ hiện đại và Lập trình thi đấu", "Lê Minh Hoàng", 1, "NXB Đại Học Quốc Gia", 2021, 25, 20, "978-604-12-4567");

        // 5: Trí tuệ nhân tạo & Data Science
        addBook(113, "Trí tuệ nhân tạo (AI) trong kỷ nguyên số", "Stuart Russell & Peter Norvig", 5, "NXB Tri Thức", 2023, 14, 10, "978-604-13-5678");
        addBook(114, "Học máy và Ứng dụng thực tiễn (Machine Learning)", "Vũ Hữu Tiệp", 5, "NXB Bách Khoa", 2022, 16, 12, "978-604-14-6789");
        addBook(115, "Xử lý dữ liệu lớn với Apache Spark & Hadoop", "Trần Văn Khôi", 5, "NXB Thông Tin & Truyền Thông", 2023, 10, 8, "978-604-15-7890");
        addBook(116, "Deep Learning căn bản và nâng cao", "Ian Goodfellow", 5, "NXB Khoa Học Kỹ Thuật", 2022, 15, 11, "978-604-16-8901");
        addBook(117, "Xử lý ngôn ngữ tự nhiên (NLP) với Transformers", "Lewis Tunstall", 5, "NXB Thông Tin & Truyền Thông", 2024, 12, 10, "978-604-17-9012");
        addBook(118, "Thị giác máy tính với OpenCV và PyTorch", "Adrian Rosebrock", 5, "NXB Bách Khoa", 2023, 14, 11, "978-604-18-0123");
        addBook(119, "Khoa học dữ liệu với Python và Pandas", "Wes McKinney", 5, "NXB Lao Động", 2023, 20, 16, "978-604-19-1234");

        // 13: Mạng máy tính & An toàn thông tin
        addBook(120, "An toàn thông tin và Phòng chống tấn công mạng", "Hà Duyên Châu", 13, "NXB Công An Nhân Dân", 2024, 12, 9, "978-604-20-2345");
        addBook(121, "Giáo trình Mạng máy tính và Giao thức TCP/IP", "James F. Kurose", 13, "NXB Giáo Dục", 2022, 30, 24, "978-604-21-3456");
        addBook(122, "Thực hành Hacking đạo đức và Kiểm thử xâm nhập", "Georgia Weidman", 13, "NXB Thông Tin & Truyền Thông", 2023, 15, 12, "978-604-22-4567");
        addBook(123, "Quản trị hệ thống Linux và DevOps", "Michael Jang", 13, "NXB Bách Khoa", 2023, 18, 14, "978-604-23-5678");
        addBook(124, "Điện toán đám mây với AWS và Docker", "Scott Coulton", 13, "NXB Lao Động", 2024, 16, 13, "978-604-24-6789");

        // 2: Kinh tế, Tài chính & Khởi nghiệp
        addBook(125, "Kinh tế vĩ mô ứng dụng", "Lê Văn C", 2, "NXB Kinh Tế", 2023, 10, 8, "978-604-25-7890");
        addBook(126, "Chiến lược đại dương xanh", "W. Chan Kim & Renée Mauborgne", 2, "NXB Lao Động", 2021, 14, 11, "978-604-26-8901");
        addBook(127, "Khởi nghiệp tinh gọn (The Lean Startup)", "Eric Ries", 2, "NXB Trẻ", 2022, 16, 13, "978-604-27-9012");
        addBook(128, "Từ tốt đến vĩ đại (Good to Great)", "Jim Collins", 2, "NXB Trẻ", 2020, 20, 16, "978-604-28-0123");
        addBook(129, "Marketing 5.0 - Công nghệ vị nhân sinh", "Philip Kotler", 2, "NXB Trẻ", 2023, 18, 15, "978-604-29-1234");
        addBook(130, "Nhà đầu tư thông minh (The Intelligent Investor)", "Benjamin Graham", 2, "NXB Tài Chính", 2021, 15, 10, "978-604-30-2345");
        addBook(131, "Dốc hết trái tim (Pour Your Heart Into It)", "Howard Schultz", 2, "NXB Trẻ", 2020, 14, 11, "978-604-31-3456");
        addBook(132, "Cha giàu cha nghèo (Rich Dad Poor Dad)", "Robert Kiyosaki", 2, "NXB Trẻ", 2021, 35, 28, "978-604-32-4567");
        addBook(133, "Bí mật tư duy triệu phú", "T. Harv Eker", 2, "NXB Tổng Hợp TP.HCM", 2022, 25, 20, "978-604-33-5678");
        addBook(134, "Chiến lược kinh doanh thời kỳ số hóa", "David L. Rogers", 2, "NXB Kinh Tế", 2023, 12, 9, "978-604-34-6789");

        // 3: Văn học Việt Nam kinh điển & hiện đại
        addBook(135, "Số Đỏ", "Vũ Trọng Phụng", 3, "NXB Văn Học", 2019, 15, 12, "978-604-35-7890");
        addBook(136, "Tắt Đèn", "Ngô Tất Tố", 3, "NXB Văn Học", 2020, 14, 12, "978-604-36-8901");
        addBook(137, "Nỗi Buồn Chiến Tranh", "Bảo Ninh", 3, "NXB Trẻ", 2022, 14, 12, "978-604-37-9012");
        addBook(138, "Chiếc Thuyền Ngoài Xa", "Nguyễn Minh Châu", 3, "NXB Giáo Dục", 2021, 16, 14, "978-604-38-0123");
        addBook(139, "Vợ Nhặt và Tuyển tập truyện ngắn Kim Lân", "Kim Lân", 3, "NXB Văn Học", 2021, 18, 15, "978-604-39-1234");
        addBook(140, "Chí Phèo - Tuyển tập tác phẩm Nam Cao", "Nam Cao", 3, "NXB Văn Học", 2020, 20, 16, "978-604-40-2345");
        addBook(141, "Truyện Kiều (Bản hiệu đính kinh điển)", "Nguyễn Du", 3, "NXB Hội Nhà Văn", 2021, 25, 22, "978-604-41-3456");
        addBook(142, "Mắt Biếc", "Nguyễn Nhật Ánh", 3, "NXB Trẻ", 2022, 30, 25, "978-604-42-4567");
        addBook(143, "Cho tôi xin một vé đi tuổi thơ", "Nguyễn Nhật Ánh", 3, "NXB Trẻ", 2023, 28, 22, "978-604-43-5678");
        addBook(144, "Cánh đồng bất tận", "Nguyễn Ngọc Tư", 3, "NXB Trẻ", 2021, 16, 13, "978-604-44-6789");

        // 11: Văn học Nước ngoài & Best-seller
        addBook(145, "Nhà Giả Kim (The Alchemist)", "Paulo Coelho", 11, "NXB Hội Nhà Văn", 2021, 30, 24, "978-604-45-7890");
        addBook(146, "Rừng Na Uy (Norwegian Wood)", "Haruki Murakami", 11, "NXB Hội Nhà Văn", 2020, 20, 17, "978-604-46-8901");
        addBook(147, "Bố Già (The Godfather)", "Mario Puzo", 11, "NXB Văn Học", 2021, 18, 14, "978-604-47-9012");
        addBook(148, "Trăm năm cô đơn", "Gabriel García Márquez", 11, "NXB Văn Học", 2022, 15, 11, "978-604-48-0123");
        addBook(149, "Tiếng chim hót trong bụi mận gai", "Colleen McCullough", 11, "NXB Hội Nhà Văn", 2021, 14, 10, "978-604-49-1234");
        addBook(150, "Ông già và biển cả (The Old Man and the Sea)", "Ernest Hemingway", 11, "NXB Văn Học", 2020, 22, 18, "978-604-50-2345");
        addBook(151, "Gatsby Vĩ Đại (The Great Gatsby)", "F. Scott Fitzgerald", 11, "NXB Hội Nhà Văn", 2022, 16, 13, "978-604-51-3456");
        addBook(152, "Giết con chim nhại (To Kill a Mockingbird)", "Harper Lee", 11, "NXB Nhã Nam", 2021, 18, 14, "978-604-52-4567");
        addBook(153, "Những người khốn khổ (Les Misérables)", "Victor Hugo", 11, "NXB Văn Học", 2020, 12, 9, "978-604-53-5678");
        addBook(154, "Chuông nguyện hồn ai", "Ernest Hemingway", 11, "NXB Văn Học", 2021, 14, 11, "978-604-54-6789");

        // 4: Ngoại ngữ & Du học
        addBook(155, "Luyện thi IELTS Target Band 7.5+", "Rachel Mitchell", 4, "NXB Tổng Hợp TP.HCM", 2023, 15, 11, "978-604-55-7890");
        addBook(156, "Ngữ pháp tiếng Anh toàn thư", "Mai Lan Hương", 4, "NXB Đà Nẵng", 2022, 25, 21, "978-604-56-8901");
        addBook(157, "600 Từ vựng TOEIC căn bản và nâng cao", "Barron", 4, "NXB Lao Động", 2023, 30, 25, "978-604-57-9012");
        addBook(158, "Giáo trình Minna no Nihongo Sơ cấp 1", "3A Corporation", 4, "NXB Trẻ", 2022, 20, 16, "978-604-58-0123");
        addBook(159, "Giáo trình Minna no Nihongo Sơ cấp 2", "3A Corporation", 4, "NXB Trẻ", 2022, 18, 14, "978-604-59-1234");
        addBook(160, "Tiếng Hàn Tổng Hợp dành cho người Việt Nam Sơ cấp 1", "Cho Hang Rok", 4, "NXB Đại Học Quốc Gia", 2023, 22, 18, "978-604-60-2345");
        addBook(161, "Giáo trình Hán ngữ Tập 1 (Quyển Thượng)", "Dương Ký Châu", 4, "NXB Giáo Dục", 2022, 25, 20, "978-604-61-3456");
        addBook(162, "Luyện phát âm tiếng Anh chuẩn giọng Mỹ", "Ann Cook", 4, "NXB Tổng Hợp TP.HCM", 2023, 16, 12, "978-604-62-4567");

        // 6: Kỹ năng sống & Phát triển bản thân
        addBook(163, "Đắc Nhân Tâm (How to Win Friends and Influence People)", "Dale Carnegie", 6, "NXB Trẻ", 2020, 35, 26, "978-604-63-5678");
        addBook(164, "Tuổi trẻ đáng giá bao nhiêu", "Rosie Nguyễn", 6, "NXB Nhã Nam", 2022, 22, 18, "978-604-64-6789");
        addBook(165, "Tư duy nhanh và chậm (Thinking, Fast and Slow)", "Daniel Kahneman", 6, "NXB Thế Giới", 2021, 15, 12, "978-604-65-7890");
        addBook(166, "7 Thói quen để thành đạt (The 7 Habits)", "Stephen R. Covey", 6, "NXB Tổng Hợp TP.HCM", 2021, 18, 14, "978-604-66-8901");
        addBook(167, "Đọc vị bất kỳ ai (Get Anyone to Do Anything)", "David J. Lieberman", 6, "NXB Lao Động Xã Hội", 2022, 16, 13, "978-604-67-9012");
        addBook(168, "Sức mạnh của thói quen (The Power of Habit)", "Charles Duhigg", 6, "NXB Thế Giới", 2021, 20, 16, "978-604-68-0123");
        addBook(169, "Càng bình tĩnh càng hạnh phúc", "Vãn Tình", 6, "NXB Văn Học", 2023, 22, 18, "978-604-69-1234");
        addBook(170, "Tinh thần thép trong mọi nghịch cảnh", "Ryan Holiday", 6, "NXB Dân Trí", 2022, 14, 11, "978-604-70-2345");
        addBook(171, "Sức mạnh của sự tập trung tối đa (Deep Work)", "Cal Newport", 6, "NXB Trẻ", 2023, 16, 12, "978-604-71-3456");

        // 7: Lịch sử, Văn hóa & Địa lý
        addBook(172, "Lược sử loài người (Sapiens)", "Yuval Noah Harari", 7, "NXB Tri Thức", 2020, 18, 14, "978-604-72-4567");
        addBook(173, "Việt Nam sử lược", "Trần Trọng Kim", 7, "NXB Văn Hóa Thông Tin", 2020, 15, 12, "978-604-73-5678");
        addBook(174, "Đại Việt sử ký toàn thư", "Ngô Sĩ Liên", 7, "NXB Khoa Học Xã Hội", 2019, 10, 8, "978-604-74-6789");
        addBook(175, "Lược sử tương lai (Homo Deus)", "Yuval Noah Harari", 7, "NXB Tri Thức", 2021, 16, 13, "978-604-75-7890");
        addBook(176, "Súng, Vi trùng và Thép", "Jared Diamond", 7, "NXB Tri Thức", 2021, 14, 11, "978-604-76-8901");
        addBook(177, "Đất rừng phương Nam", "Đoàn Giỏi", 7, "NXB Kim Đồng", 2022, 25, 21, "978-604-77-9012");

        // 8: Tâm lý học & Triết học ứng dụng
        addBook(178, "Tâm lý học về tiền (The Psychology of Money)", "Morgan Housel", 8, "NXB Dân Trí", 2022, 15, 11, "978-604-78-0123");
        addBook(179, "Nghệ thuật sống Stoic (A Guide to the Good Life)", "William B. Irvine", 8, "NXB Thế Giới", 2022, 12, 10, "978-604-79-1234");
        addBook(180, "Đi tìm lẽ sống (Man Search for Meaning)", "Viktor Frankl", 8, "NXB Tổng Hợp TP.HCM", 2021, 20, 16, "978-604-80-2345");
        addBook(181, "Bàn về hạnh phúc (The Art of Happiness)", "Dalai Lama", 8, "NXB Tôn Giáo", 2022, 14, 11, "978-604-81-3456");
        addBook(182, "Khắc kỷ từ Zeno đến Marcus Aurelius", "Ryan Holiday", 8, "NXB Dân Trí", 2023, 15, 12, "978-604-82-4567");
        addBook(183, "Thao túng tâm lý và Nhận biết bẫy thao túng", "Shannon Thomas", 8, "NXB Thanh Niên", 2023, 16, 13, "978-604-83-5678");

        // 9: Khoa học tự nhiên & Kỹ thuật
        addBook(184, "Nguyên lý máy tính và mạng viễn thông", "Nguyễn Nam Hoàng", 9, "NXB Bách Khoa", 2023, 15, 12, "978-604-84-6789");
        addBook(185, "Vật lý đại cương dành cho kỹ sư", "Lương Duyên Bình", 9, "NXB Giáo Dục", 2022, 25, 20, "978-604-85-7890");
        addBook(186, "Hóa học lượng tử đại cương", "Đào Đình Thức", 9, "NXB Đại Học Quốc Gia", 2021, 12, 10, "978-604-86-8901");
        addBook(187, "Vũ trụ trong vỏ hạt dẻ (The Universe in a Nutshell)", "Stephen Hawking", 9, "NXB Trẻ", 2021, 16, 13, "978-604-87-9012");
        addBook(188, "Lược sử thời gian (A Brief History of Time)", "Stephen Hawking", 9, "NXB Trẻ", 2022, 18, 14, "978-604-88-0123");

        // 10: Y học, Dinh dưỡng & Sức khỏe
        addBook(189, "Dinh dưỡng học và Chế độ ăn lành mạnh", "Trần Văn Tuấn", 10, "NXB Y Học", 2023, 14, 11, "978-604-89-1234");
        addBook(190, "Sơ cứu ban đầu và Xử lý tai nạn khẩn cấp", "Hội Chữ Thập Đỏ", 10, "NXB Y Học", 2022, 20, 17, "978-604-90-2345");
        addBook(191, "Nhân tố Enzyme", "Hiromi Shinya", 10, "NXB Thế Giới", 2021, 25, 20, "978-604-91-3456");
        addBook(192, "Giải mã giấc ngủ (Why We Sleep)", "Matthew Walker", 10, "NXB Lao Động", 2022, 16, 13, "978-604-92-4567");
        addBook(193, "Thể dục và Vận động hồi phục cột sống", "Nguyễn Hữu Nam", 10, "NXB Thể Dục Thể Thao", 2023, 15, 12, "978-604-93-5678");

        // 12: Truyện tranh & Thiếu nhi
        addBook(194, "Dế Mèn phiêu lưu ký", "Tô Hoài", 12, "NXB Kim Đồng", 2022, 30, 24, "978-604-94-6789");
        addBook(195, "Không gia đình (Sans Famille)", "Hector Malot", 12, "NXB Kim Đồng", 2021, 20, 16, "978-604-95-7890");
        addBook(196, "Những tấm lòng cao cả", "Edmondo De Amicis", 12, "NXB Kim Đồng", 2021, 18, 15, "978-604-96-8901");
        addBook(197, "Hoàng tử bé (The Little Prince)", "Antoine de Saint-Exupéry", 12, "NXB Kim Đồng", 2023, 35, 28, "978-604-97-9012");
        addBook(198, "Gió qua rặng liễu", "Kenneth Grahame", 12, "NXB Kim Đồng", 2022, 15, 12, "978-604-98-0123");

        // 14: Thiết kế Đồ họa, UI/UX & Mỹ thuật
        addBook(199, "Thiết kế trải nghiệm người dùng (UX Design)", "Steve Krug", 14, "NXB Thông Tin & Truyền Thông", 2023, 14, 11, "978-604-99-1234");
        addBook(200, "Nguyên lý bố cục và màu sắc trong thiết kế", "Robin Williams", 14, "NXB Mỹ Thuật", 2022, 15, 12, "978-604-100-2345");
        addBook(201, "Câu chuyện nghệ thuật (The Story of Art)", "E.H. Gombrich", 14, "NXB Dân Trí", 2021, 12, 9, "978-604-101-3456");

        // 15: Quản trị Dự án & Lãnh đạo
        addBook(202, "Quản trị dự án Agile và Scrum trong phát triển phần mềm", "Ken Schwaber", 15, "NXB Bách Khoa", 2023, 16, 13, "978-604-102-4567");
        addBook(203, "21 Nguyên tắc vàng của nghệ thuật lãnh đạo", "John C. Maxwell", 15, "NXB Lao Động", 2021, 18, 14, "978-604-103-5678");
        addBook(204, "Nhà lãnh đạo không chức danh", "Robin Sharma", 15, "NXB Trẻ", 2022, 20, 16, "978-604-104-6789");
        addBook(205, "OKRs - Bí quyết đo lường mục tiêu hiệu quả", "John Doerr", 15, "NXB Tổng Hợp TP.HCM", 2023, 15, 12, "978-604-105-7890");

        // 4. Readers (50 readers)
        addReader(1, "Phạm Văn Đồng", "0912345678", "dong.pv@gmail.com", "123 Lê Duẩn, Hải Châu, Đà Nẵng", "ACTIVE");
        addReader(2, "Nguyễn Thị Mai", "0987654321", "mai.nt@gmail.com", "45 Nguyễn Thị Minh Khai, Đà Nẵng", "ACTIVE");
        addReader(3, "Trần Đức Anh", "0905112233", "anh.td@gmail.com", "88 Hùng Vương, Thanh Khê, Đà Nẵng", "ACTIVE");
        addReader(4, "Lê Hoàng Yến", "0935445566", "yen.lh@gmail.com", "210 Điện Biên Phủ, Đà Nẵng", "ACTIVE");
        addReader(5, "Võ Quốc Cường", "0978998877", "cuong.vq@gmail.com", "15 Quang Trung, Hải Châu, Đà Nẵng", "ACTIVE");
        addReader(6, "Phan Bảo Châu", "0914223344", "chau.pb@gmail.com", "68 Bạch Đằng, Đà Nẵng", "ACTIVE");
        addReader(7, "Đỗ Mạnh Hùng", "0945667788", "hung.dm@gmail.com", "102 Trưng Nữ Vương, Đà Nẵng", "ACTIVE");
        addReader(8, "Bùi Thu Hà", "0967889900", "ha.bt@gmail.com", "34 Phan Châu Trinh, Đà Nẵng", "ACTIVE");
        addReader(9, "Huỳnh Gia Huy", "0903123456", "huy.hg@gmail.com", "56 Trần Phú, Đà Nẵng", "ACTIVE");
        addReader(10, "Nguyễn Ngọc Trâm", "0922334455", "tram.nn@gmail.com", "79 Hoàng Diệu, Đà Nẵng", "ACTIVE");
        addReader(11, "Dương Tuấn Kiệt", "0918776655", "kiet.dt@gmail.com", "150 Nguyễn Văn Linh, Đà Nẵng", "ACTIVE");
        addReader(12, "Đặng Thùy Trang", "0933221144", "trang.dt@gmail.com", "92 Ngô Quyền, Sơn Trà, Đà Nẵng", "ACTIVE");
        addReader(13, "Vũ Hoàng Nam", "0981223355", "nam.vh@gmail.com", "412 Tôn Đức Thắng, Liên Chiểu, Đà Nẵng", "ACTIVE");
        addReader(14, "Hồ Thị Bích Ngọc", "0908771122", "ngoc.htb@gmail.com", "35 Hà Huy Tập, Thanh Khê, Đà Nẵng", "ACTIVE");
        addReader(15, "Lý Quốc Bảo", "0944556677", "bao.lq@gmail.com", "72 Lê Độ, Thanh Khê, Đà Nẵng", "ACTIVE");
        addReader(16, "Trịnh Thu Thảo", "0966112244", "thao.tt@gmail.com", "18 Núi Thành, Hải Châu, Đà Nẵng", "ACTIVE");
        addReader(17, "Đinh Hữu Phúc", "0971239876", "phuc.dh@gmail.com", "255 Trường Chinh, Cẩm Lệ, Đà Nẵng", "ACTIVE");
        addReader(18, "Lương Minh Quân", "0938665544", "quan.lm@gmail.com", "14 Cách Mạng Tháng 8, Đà Nẵng", "ACTIVE");
        addReader(19, "Tạ Kim Oanh", "0919443322", "oanh.tk@gmail.com", "63 Võ Văn Kiệt, Sơn Trà, Đà Nẵng", "ACTIVE");
        addReader(20, "Đoàn Xuân Thành", "0909554433", "thanh.dx@gmail.com", "105 Ông Ích Khiêm, Hải Châu, Đà Nẵng", "ACTIVE");
        addReader(21, "Nguyễn Thành Long", "0905223344", "long.nt@gmail.com", "182 Ngũ Hành Sơn, Đà Nẵng", "ACTIVE");
        addReader(22, "Hoàng Thúy Vy", "0935118899", "vy.ht@gmail.com", "45 An Thượng 2, Ngũ Hành Sơn, Đà Nẵng", "ACTIVE");
        addReader(23, "Lê Bá Khánh Trình", "0914667788", "trinh.lbk@gmail.com", "290 Dũng Sĩ Thanh Khê, Đà Nẵng", "ACTIVE");
        addReader(24, "Vũ Thị Kim Liên", "0988334455", "lien.vtk@gmail.com", "64 Lê Thanh Nghị, Hải Châu, Đà Nẵng", "ACTIVE");
        addReader(25, "Trần Đình Trọng", "0976554433", "trong.td@gmail.com", "118 Huỳnh Thúc Kháng, Hải Châu, Đà Nẵng", "ACTIVE");
        addReader(26, "Phạm Ngọc Lan Anh", "0903887766", "anh.pnl@gmail.com", "85 Hàm Nghi, Thanh Khê, Đà Nẵng", "ACTIVE");
        addReader(27, "Đỗ Quốc Việt", "0947223311", "viet.dq@gmail.com", "240 Lê Duẩn, Thanh Khê, Đà Nẵng", "ACTIVE");
        addReader(28, "Bùi Quang Huy", "0968112233", "huy.bq@gmail.com", "12 Phan Đăng Lưu, Hải Châu, Đà Nẵng", "ACTIVE");
        addReader(29, "Huỳnh Thị Bích Thủy", "0917998877", "thuy.htb@gmail.com", "52 Nguyễn Hữu Thọ, Cẩm Lệ, Đà Nẵng", "ACTIVE");
        addReader(30, "Phan Văn Đức", "0932445566", "duc.pv@gmail.com", "310 Tôn Đức Thắng, Liên Chiểu, Đà Nẵng", "ACTIVE");
        addReader(31, "Ngô Bảo Châu", "0909113355", "chau.nb@gmail.com", "77 Lý Thường Kiệt, Hải Châu, Đà Nẵng", "ACTIVE");
        addReader(32, "Đinh Thị Mỹ Hạnh", "0925667788", "hanh.dtm@gmail.com", "19 Tô Ngọc Vân, Thanh Khê, Đà Nẵng", "ACTIVE");
        addReader(33, "Mai Văn Phước", "0984776655", "phuoc.mv@gmail.com", "48 Trần Quý Cáp, Hải Châu, Đà Nẵng", "ACTIVE");
        addReader(34, "Trương Hoàng Long", "0943556677", "long.th@gmail.com", "134 Nguyễn Tri Phương, Đà Nẵng", "ACTIVE");
        addReader(35, "Cao Thị Cẩm Tú", "0915332211", "tu.ctc@gmail.com", "89 Phan Bội Châu, Hải Châu, Đà Nẵng", "ACTIVE");
        addReader(36, "Lâm Tuấn Hưng", "0969887766", "hung.lt@gmail.com", "222 Âu Cơ, Liên Chiểu, Đà Nẵng", "ACTIVE");
        addReader(37, "Châu Kiều Trang", "0973445566", "trang.ck@gmail.com", "61 Chu Văn An, Hải Châu, Đà Nẵng", "ACTIVE");
        addReader(38, "Hà Vĩnh Khang", "0906223344", "khang.hv@gmail.com", "107 Hoàng Hoa Thám, Thanh Khê, Đà Nẵng", "ACTIVE");
        addReader(39, "Tăng Thanh Hà", "0938112233", "ha.tt@gmail.com", "42 Pasteur, Hải Châu, Đà Nẵng", "ACTIVE");
        addReader(40, "Võ Văn Kiệt", "0912665544", "kiet.vv@gmail.com", "335 Cách Mạng Tháng 8, Cẩm Lệ, Đà Nẵng", "ACTIVE");
        addReader(41, "Lê Thị Hồng Nhung", "0986998877", "nhung.lth@gmail.com", "16 Đống Đa, Hải Châu, Đà Nẵng", "ACTIVE");
        addReader(42, "Dương Triệu Vũ", "0945113355", "vu.dt@gmail.com", "84 Triệu Nữ Vương, Hải Châu, Đà Nẵng", "ACTIVE");
        addReader(43, "Nguyễn Hữu Thọ", "0978224466", "tho.nh@gmail.com", "126 Nguyễn Lương Bằng, Liên Chiểu, Đà Nẵng", "ACTIVE");
        addReader(44, "Hồ Xuân Hương", "0907335577", "huong.hx@gmail.com", "95 Lê Lợi, Hải Châu, Đà Nẵng", "ACTIVE");
        addReader(45, "Trịnh Công Sơn", "0934668800", "son.tc@gmail.com", "47 Quang Dũng, Thanh Khê, Đà Nẵng", "ACTIVE");
        addReader(46, "Bùi Bích Phương", "0916554433", "phuong.bb@gmail.com", "280 Mai Đăng Chơn, Ngũ Hành Sơn, Đà Nẵng", "ACTIVE");
        addReader(47, "Hoàng Nam Tiến", "0965332211", "tien.hn@gmail.com", "FPT City, Ngũ Hành Sơn, Đà Nẵng", "ACTIVE");
        addReader(48, "Phạm Nhật Vượng", "0989112233", "vuong.pn@gmail.com", "Vincom Plaza, Ngô Quyền, Sơn Trà, Đà Nẵng", "ACTIVE");
        addReader(49, "Nguyễn Thị Phương Thảo", "0904889900", "thao.ntp@gmail.com", "Furama Resort, Ngũ Hành Sơn, Đà Nẵng", "ACTIVE");
        addReader(50, "Đoàn Nguyên Đức", "0913778899", "duc.dn@gmail.com", "51 Hoàng Diệu, Hải Châu, Đà Nẵng", "ACTIVE");

        // 5. Borrowings (35 borrowings for in-memory mode)
        seedBorrowing(501, 1, 2, "2026-10-10", "2026-10-24", null, "BORROWED", 0.0);
        seedBorrowing(502, 2, 2, "2026-10-05", "2026-10-19", "2026-10-18", "RETURNED", 0.0);
        seedBorrowing(503, 3, 1, "2026-09-25", "2026-10-09", null, "OVERDUE", 15000.0);
        seedBorrowing(504, 4, 3, "2026-10-15", "2026-10-29", null, "BORROWED", 0.0);
        seedBorrowing(505, 5, 1, "2026-10-18", "2026-11-01", null, "BORROWED", 0.0);
        seedBorrowing(506, 6, 2, "2026-09-20", "2026-10-04", "2026-10-03", "RETURNED", 0.0);
        seedBorrowing(507, 7, 3, "2026-09-15", "2026-09-29", null, "OVERDUE", 25000.0);
        seedBorrowing(508, 8, 2, "2026-10-12", "2026-10-26", null, "BORROWED", 0.0);
        seedBorrowing(509, 9, 1, "2026-10-14", "2026-10-28", null, "BORROWED", 0.0);
        seedBorrowing(510, 10, 3, "2026-10-01", "2026-10-15", "2026-10-14", "RETURNED", 0.0);
        seedBorrowing(511, 11, 2, "2026-10-16", "2026-10-30", null, "BORROWED", 0.0);
        seedBorrowing(512, 12, 1, "2026-10-19", "2026-11-02", null, "BORROWED", 0.0);
        seedBorrowing(513, 13, 3, "2026-09-28", "2026-10-12", "2026-10-11", "RETURNED", 0.0);
        seedBorrowing(514, 14, 2, "2026-10-20", "2026-11-03", null, "BORROWED", 0.0);
        seedBorrowing(515, 15, 1, "2026-09-10", "2026-09-24", null, "OVERDUE", 35000.0);
        seedBorrowing(516, 16, 4, "2026-10-02", "2026-10-16", "2026-10-15", "RETURNED", 0.0);
        seedBorrowing(517, 17, 4, "2026-10-17", "2026-10-31", null, "BORROWED", 0.0);
        seedBorrowing(518, 18, 5, "2026-09-18", "2026-10-02", null, "OVERDUE", 30000.0);
        seedBorrowing(519, 19, 5, "2026-10-11", "2026-10-25", null, "BORROWED", 0.0);
        seedBorrowing(520, 20, 2, "2026-10-08", "2026-10-22", "2026-10-21", "RETURNED", 0.0);
        seedBorrowing(521, 21, 3, "2026-10-13", "2026-10-27", null, "BORROWED", 0.0);
        seedBorrowing(522, 22, 1, "2026-10-21", "2026-11-04", null, "BORROWED", 0.0);
        seedBorrowing(523, 23, 4, "2026-09-22", "2026-10-06", null, "OVERDUE", 20000.0);
        seedBorrowing(524, 24, 2, "2026-10-04", "2026-10-18", "2026-10-17", "RETURNED", 0.0);
        seedBorrowing(525, 25, 5, "2026-10-19", "2026-11-02", null, "BORROWED", 0.0);
        seedBorrowing(526, 26, 3, "2026-10-16", "2026-10-30", null, "BORROWED", 0.0);
        seedBorrowing(527, 27, 2, "2026-10-07", "2026-10-21", "2026-10-20", "RETURNED", 0.0);
        seedBorrowing(528, 28, 1, "2026-09-05", "2026-09-19", null, "OVERDUE", 50000.0);
        seedBorrowing(529, 29, 4, "2026-10-14", "2026-10-28", null, "BORROWED", 0.0);
        seedBorrowing(530, 30, 5, "2026-10-15", "2026-10-29", null, "BORROWED", 0.0);
        seedBorrowing(531, 31, 2, "2026-10-09", "2026-10-23", null, "BORROWED", 0.0);
        seedBorrowing(532, 32, 3, "2026-09-12", "2026-09-26", null, "OVERDUE", 40000.0);
        seedBorrowing(533, 33, 1, "2026-10-18", "2026-11-01", null, "BORROWED", 0.0);
        seedBorrowing(534, 34, 4, "2026-10-03", "2026-10-17", "2026-10-16", "RETURNED", 0.0);
        seedBorrowing(535, 35, 2, "2026-10-20", "2026-11-03", null, "BORROWED", 0.0);

        // 6. Fines (10 fines)
        seedFine(1, 503, 15000.0, "UNPAID", "Trễ hạn 13 ngày (Phiếu mượn #503)", "2026-10-10 09:00:00");
        seedFine(2, 507, 25000.0, "UNPAID", "Trễ hạn 23 ngày (Phiếu mượn #507)", "2026-10-10 09:00:00");
        seedFine(3, 515, 35000.0, "UNPAID", "Trễ hạn 28 ngày (Phiếu mượn #515)", "2026-10-10 09:00:00");
        seedFine(4, 518, 30000.0, "UNPAID", "Trễ hạn 20 ngày (Phiếu mượn #518)", "2026-10-10 09:00:00");
        seedFine(5, 523, 20000.0, "PAID",   "Trễ hạn 16 ngày (Phiếu mượn #523) - Đã thanh toán", "2026-10-10 09:00:00");
        seedFine(6, 528, 50000.0, "UNPAID", "Trễ hạn 33 ngày (Phiếu mượn #528)", "2026-10-10 09:00:00");
        seedFine(7, 532, 40000.0, "UNPAID", "Trễ hạn 26 ngày (Phiếu mượn #532)", "2026-10-10 09:00:00");
        seedFine(8, 502, 10000.0, "PAID",   "Làm bẩn nhẹ bìa sách #145 - Đã thu phí xử lý", "2026-10-10 09:00:00");
        seedFine(9, 506, 15000.0, "PAID",   "Trễ hạn 1 ngày - Đã thanh toán khi trả sách", "2026-10-10 09:00:00");
        seedFine(10, 513, 20000.0, "PAID",  "Gấp góc hư hỏng trang sách - Đã bồi thường", "2026-10-10 09:00:00");
    }

    private static void addBook(int id, String title, String author, int categoryId, String publisher, int year, int qty, int avail, String isbn) {
        Book b = new Book(id, title, author, categoryId, publisher, year, qty, avail, isbn);
        Category c = categories.get(categoryId);
        if (c != null) {
            b.setCategoryName(c.getCategoryName());
        }
        books.put(id, b);
    }

    private static void addReader(int id, String name, String phone, String email, String address, String status) {
        readers.put(id, new Reader(id, name, phone, email, address, status));
    }

    private static void seedBorrowing(int id, int readerId, int userId, String borrowDate, String dueDate, String returnDate, String status, double fine) {
        Borrowing b = new Borrowing();
        b.setBorrowingId(id);
        b.setReaderId(readerId);
        Reader r = readers.get(readerId);
        if (r != null) b.setReaderName(r.getFullName());
        b.setUserId(userId);
        User u = users.get(userId);
        if (u != null) b.setUserName(u.getFullName());
        b.setBorrowDate(borrowDate);
        b.setDueDate(dueDate);
        b.setReturnDate(returnDate);
        b.setStatus(status);
        b.setFineAmount(fine);
        borrowings.put(id, b);
    }

    private static void seedFine(int id, int borrowingId, double amount, String status, String reason, String date) {
        Fine f = new Fine(id, borrowingId, amount, status, reason, date);
        Borrowing b = borrowings.get(borrowingId);
        if (b != null) {
            f.setReaderId(b.getReaderId());
            f.setReaderName(b.getReaderName());
        }
        fines.put(id, f);
    }

    public static User findUserByUsername(String username) {
        if (!DatabaseConfig.isInMemoryMode()) {
            try (Connection conn = DatabaseConfig.getConnection()) {
                if (conn != null) {
                    PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE username = ?");
                    ps.setString(1, username);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        return new User(
                                rs.getInt("user_id"),
                                rs.getString("username"),
                                rs.getString("password"),
                                rs.getString("full_name"),
                                rs.getString("role"),
                                rs.getString("status")
                        );
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        User u = users.values().stream().filter(user -> user.getUsername().equals(username)).findFirst().orElse(null);
        if (u != null) {
            return new User(
                    u.getUserId(),
                    u.getUsername(),
                    u.getPassword(),
                    u.getFullName(),
                    u.getRole(),
                    u.getStatus()
            );
        }
        return null;
    }

    public static boolean registerUser(User user) {
        if (!DatabaseConfig.isInMemoryMode()) {
            try (Connection conn = DatabaseConfig.getConnection()) {
                if (conn != null) {
                    String sql = "INSERT INTO users(username, password, full_name, role, status) VALUES(?,?,?,?,?)";
                    PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, user.getUsername());
                    ps.setString(2, user.getPassword());
                    ps.setString(3, user.getFullName());
                    ps.setString(4, user.getRole() != null ? user.getRole() : "STAFF");
                    ps.setString(5, user.getStatus() != null ? user.getStatus() : "ACTIVE");
                    int affected = ps.executeUpdate();
                    if (affected > 0) {
                        ResultSet rs = ps.getGeneratedKeys();
                        if (rs.next()) {
                            user.setUserId(rs.getInt(1));
                        }
                        users.put(user.getUserId(), user);
                        return true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        int id = userIdSeq.incrementAndGet();
        user.setUserId(id);
        if (user.getRole() == null) user.setRole("STAFF");
        if (user.getStatus() == null) user.setStatus("ACTIVE");
        users.put(id, user);
        return true;
    }

    public static List<Book> getAllBooks() {
        if (!DatabaseConfig.isInMemoryMode()) {
            List<Book> list = new ArrayList<>();
            try (Connection conn = DatabaseConfig.getConnection()) {
                if (conn != null) {
                    String sql = "SELECT b.*, c.category_name FROM books b LEFT JOIN categories c ON b.category_id = c.category_id ORDER BY b.book_id ASC";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        Book b = new Book(
                                rs.getInt("book_id"),
                                rs.getString("title"),
                                rs.getString("author"),
                                rs.getInt("category_id"),
                                rs.getString("publisher"),
                                rs.getInt("publish_year"),
                                rs.getInt("quantity"),
                                rs.getInt("available_quantity"),
                                rs.getString("isbn")
                        );
                        b.setCategoryName(rs.getString("category_name"));
                        list.add(b);
                    }
                    if (!list.isEmpty()) {
                        return list;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>(books.values());
    }

    public static boolean saveBook(Book b) {
        if (!DatabaseConfig.isInMemoryMode()) {
            try (Connection conn = DatabaseConfig.getConnection()) {
                if (conn != null) {
                    if (b.getBookId() <= 0) {
                        String sql = "INSERT INTO books(title, author, category_id, publisher, publish_year, quantity, available_quantity, isbn) VALUES(?,?,?,?,?,?,?,?)";
                        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                        ps.setString(1, b.getTitle());
                        ps.setString(2, b.getAuthor());
                        ps.setInt(3, b.getCategoryId());
                        ps.setString(4, b.getPublisher());
                        ps.setInt(5, b.getPublishYear());
                        ps.setInt(6, b.getQuantity());
                        ps.setInt(7, b.getAvailableQuantity());
                        ps.setString(8, b.getIsbn());
                        int aff = ps.executeUpdate();
                        if (aff > 0) {
                            ResultSet rs = ps.getGeneratedKeys();
                            if (rs.next()) b.setBookId(rs.getInt(1));
                        }
                    } else {
                        String sql = "UPDATE books SET title=?, author=?, category_id=?, publisher=?, publish_year=?, quantity=?, available_quantity=?, isbn=? WHERE book_id=?";
                        PreparedStatement ps = conn.prepareStatement(sql);
                        ps.setString(1, b.getTitle());
                        ps.setString(2, b.getAuthor());
                        ps.setInt(3, b.getCategoryId());
                        ps.setString(4, b.getPublisher());
                        ps.setInt(5, b.getPublishYear());
                        ps.setInt(6, b.getQuantity());
                        ps.setInt(7, b.getAvailableQuantity());
                        ps.setString(8, b.getIsbn());
                        ps.setInt(9, b.getBookId());
                        ps.executeUpdate();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (b.getBookId() <= 0) {
            int id = bookIdSeq.incrementAndGet();
            b.setBookId(id);
            b.setAvailableQuantity(b.getQuantity());
        }
        Category c = categories.get(b.getCategoryId());
        if (c != null) {
            b.setCategoryName(c.getCategoryName());
        }
        books.put(b.getBookId(), b);
        return true;
    }

    public static boolean deleteBook(int bookId) {
        if (!DatabaseConfig.isInMemoryMode()) {
            try (Connection conn = DatabaseConfig.getConnection()) {
                if (conn != null) {
                    PreparedStatement ps = conn.prepareStatement("DELETE FROM books WHERE book_id = ?");
                    ps.setInt(1, bookId);
                    ps.executeUpdate();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return books.remove(bookId) != null;
    }

    public static List<Reader> getAllReaders() {
        if (!DatabaseConfig.isInMemoryMode()) {
            List<Reader> list = new ArrayList<>();
            try (Connection conn = DatabaseConfig.getConnection()) {
                if (conn != null) {
                    PreparedStatement ps = conn.prepareStatement("SELECT * FROM readers ORDER BY reader_id ASC");
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        list.add(new Reader(
                                rs.getInt("reader_id"),
                                rs.getString("full_name"),
                                rs.getString("phone"),
                                rs.getString("email"),
                                rs.getString("address"),
                                rs.getString("card_status")
                        ));
                    }
                    if (!list.isEmpty()) {
                        return list;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>(readers.values());
    }

    public static boolean saveReader(Reader r) {
        if (!DatabaseConfig.isInMemoryMode()) {
            try (Connection conn = DatabaseConfig.getConnection()) {
                if (conn != null) {
                    if (r.getReaderId() <= 0) {
                        String sql = "INSERT INTO readers(full_name, phone, email, address, card_status) VALUES(?,?,?,?,?)";
                        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                        ps.setString(1, r.getFullName());
                        ps.setString(2, r.getPhone());
                        ps.setString(3, r.getEmail());
                        ps.setString(4, r.getAddress());
                        ps.setString(5, r.getCardStatus());
                        int aff = ps.executeUpdate();
                        if (aff > 0) {
                            ResultSet rs = ps.getGeneratedKeys();
                            if (rs.next()) r.setReaderId(rs.getInt(1));
                        }
                    } else {
                        String sql = "UPDATE readers SET full_name=?, phone=?, email=?, address=?, card_status=? WHERE reader_id=?";
                        PreparedStatement ps = conn.prepareStatement(sql);
                        ps.setString(1, r.getFullName());
                        ps.setString(2, r.getPhone());
                        ps.setString(3, r.getEmail());
                        ps.setString(4, r.getAddress());
                        ps.setString(5, r.getCardStatus());
                        ps.setInt(6, r.getReaderId());
                        ps.executeUpdate();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (r.getReaderId() <= 0) {
            int id = readerIdSeq.incrementAndGet();
            r.setReaderId(id);
        }
        readers.put(r.getReaderId(), r);
        return true;
    }

    public static List<Category> getAllCategories() {
        if (!DatabaseConfig.isInMemoryMode()) {
            List<Category> list = new ArrayList<>();
            try (Connection conn = DatabaseConfig.getConnection()) {
                if (conn != null) {
                    PreparedStatement ps = conn.prepareStatement("SELECT * FROM categories ORDER BY category_id ASC");
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        list.add(new Category(
                                rs.getInt("category_id"),
                                rs.getString("category_name"),
                                rs.getString("description")
                        ));
                    }
                    if (!list.isEmpty()) {
                        return list;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>(categories.values());
    }

    public static List<Borrowing> getAllBorrowings() {
        if (!DatabaseConfig.isInMemoryMode()) {
            List<Borrowing> list = new ArrayList<>();
            try (Connection conn = DatabaseConfig.getConnection()) {
                if (conn != null) {
                    String sql = "SELECT b.*, r.full_name AS reader_name, u.full_name AS user_name FROM borrowings b " +
                            "LEFT JOIN readers r ON b.reader_id = r.reader_id " +
                            "LEFT JOIN users u ON b.user_id = u.user_id " +
                            "ORDER BY b.borrowing_id DESC";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        Borrowing bo = new Borrowing();
                        bo.setBorrowingId(rs.getInt("borrowing_id"));
                        bo.setReaderId(rs.getInt("reader_id"));
                        bo.setReaderName(rs.getString("reader_name"));
                        bo.setUserId(rs.getInt("user_id"));
                        bo.setUserName(rs.getString("user_name"));
                        bo.setBorrowDate(rs.getString("borrow_date"));
                        bo.setDueDate(rs.getString("due_date"));
                        bo.setReturnDate(rs.getString("return_date"));
                        bo.setStatus(rs.getString("status"));
                        bo.setFineAmount(rs.getDouble("fine_amount"));
                        list.add(bo);
                    }
                    if (!list.isEmpty()) {
                        return list;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>(borrowings.values());
    }

    public static Borrowing findBorrowingById(int borrowingId) {
        if (!DatabaseConfig.isInMemoryMode()) {
            try (Connection conn = DatabaseConfig.getConnection()) {
                if (conn != null) {
                    String sql = "SELECT * FROM borrowings WHERE borrowing_id = ?";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setInt(1, borrowingId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        Borrowing bo = new Borrowing();
                        bo.setBorrowingId(rs.getInt("borrowing_id"));
                        bo.setReaderId(rs.getInt("reader_id"));
                        bo.setUserId(rs.getInt("user_id"));
                        bo.setBorrowDate(rs.getString("borrow_date"));
                        bo.setDueDate(rs.getString("due_date"));
                        bo.setReturnDate(rs.getString("return_date"));
                        bo.setStatus(rs.getString("status"));
                        bo.setFineAmount(rs.getDouble("fine_amount"));

                        // Get details
                        String detSql = "SELECT * FROM borrowing_details WHERE borrowing_id = ?";
                        PreparedStatement detPs = conn.prepareStatement(detSql);
                        detPs.setInt(1, borrowingId);
                        ResultSet detRs = detPs.executeQuery();
                        List<BorrowingDetail> details = new ArrayList<>();
                        while (detRs.next()) {
                            BorrowingDetail bd = new BorrowingDetail();
                            bd.setDetailId(detRs.getInt("detail_id"));
                            bd.setBorrowingId(borrowingId);
                            bd.setBookId(detRs.getInt("book_id"));
                            bd.setQuantity(detRs.getInt("quantity"));
                            bd.setReturnedQuantity(detRs.getInt("returned_quantity"));
                            bd.setStatus(detRs.getString("status"));
                            details.add(bd);
                        }
                        bo.setDetails(details);
                        return bo;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return borrowings.get(borrowingId);
    }

    public static boolean saveBorrowing(Borrowing b) {
        if (!DatabaseConfig.isInMemoryMode()) {
            try (Connection conn = DatabaseConfig.getConnection()) {
                if (conn != null) {
                    String sql = "INSERT INTO borrowings(reader_id, user_id, borrow_date, due_date, status, fine_amount) VALUES(?,?,?,?,?,?)";
                    PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    ps.setInt(1, b.getReaderId());
                    ps.setInt(2, b.getUserId());
                    ps.setString(3, b.getBorrowDate());
                    ps.setString(4, b.getDueDate());
                    ps.setString(5, b.getStatus());
                    ps.setDouble(6, b.getFineAmount());
                    int aff = ps.executeUpdate();
                    if (aff > 0) {
                        ResultSet rs = ps.getGeneratedKeys();
                        if (rs.next()) b.setBorrowingId(rs.getInt(1));

                        if (b.getDetails() != null) {
                            String detSql = "INSERT INTO borrowing_details(borrowing_id, book_id, quantity, returned_quantity, status) VALUES(?,?,?,?,?)";
                            PreparedStatement psDet = conn.prepareStatement(detSql);
                            for (BorrowingDetail bd : b.getDetails()) {
                                psDet.setInt(1, b.getBorrowingId());
                                psDet.setInt(2, bd.getBookId());
                                psDet.setInt(3, bd.getQuantity());
                                psDet.setInt(4, bd.getReturnedQuantity());
                                psDet.setString(5, bd.getStatus());
                                psDet.addBatch();
                            }
                            psDet.executeBatch();
                        }
                        borrowings.put(b.getBorrowingId(), b);
                        return true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (b.getBorrowingId() <= 0) {
            b.setBorrowingId(borrowingIdSeq.incrementAndGet());
        }
        borrowings.put(b.getBorrowingId(), b);
        return true;
    }

    public static boolean updateBorrowing(Borrowing b) {
        if (!DatabaseConfig.isInMemoryMode()) {
            try (Connection conn = DatabaseConfig.getConnection()) {
                if (conn != null) {
                    String sql = "UPDATE borrowings SET return_date=?, status=?, fine_amount=? WHERE borrowing_id=?";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, b.getReturnDate());
                    ps.setString(2, b.getStatus());
                    ps.setDouble(3, b.getFineAmount());
                    ps.setInt(4, b.getBorrowingId());
                    ps.executeUpdate();

                    String detSql = "UPDATE borrowing_details SET status='RETURNED', returned_quantity=quantity WHERE borrowing_id=?";
                    PreparedStatement psDet = conn.prepareStatement(detSql);
                    psDet.setInt(1, b.getBorrowingId());
                    psDet.executeUpdate();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        borrowings.put(b.getBorrowingId(), b);
        return true;
    }

    public static boolean renewBorrowing(int borrowingId, int extraDays) {
        if (!DatabaseConfig.isInMemoryMode()) {
            try (Connection conn = DatabaseConfig.getConnection()) {
                if (conn != null) {
                    String sql = "UPDATE borrowings SET due_date = DATE_ADD(due_date, INTERVAL ? DAY) WHERE borrowing_id = ? AND status = 'BORROWED'";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setInt(1, extraDays);
                    ps.setInt(2, borrowingId);
                    int aff = ps.executeUpdate();
                    if (aff > 0) {
                        Borrowing b = findBorrowingById(borrowingId);
                        if (b != null) borrowings.put(borrowingId, b);
                        return true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Borrowing b = borrowings.get(borrowingId);
        if (b != null && "BORROWED".equals(b.getStatus()) && b.getDueDate() != null) {
            try {
                java.time.LocalDate currentDue = java.time.LocalDate.parse(b.getDueDate());
                b.setDueDate(currentDue.plusDays(extraDays).toString());
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static List<Fine> getAllFines() {
        if (!DatabaseConfig.isInMemoryMode()) {
            List<Fine> list = new ArrayList<>();
            try (Connection conn = DatabaseConfig.getConnection()) {
                if (conn != null) {
                    String sql = "SELECT f.*, b.reader_id, r.full_name AS reader_name " +
                            "FROM fines f " +
                            "LEFT JOIN borrowings b ON f.borrowing_id = b.borrowing_id " +
                            "LEFT JOIN readers r ON b.reader_id = r.reader_id " +
                            "ORDER BY f.fine_id DESC";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        Fine f = new Fine(
                                rs.getInt("fine_id"),
                                rs.getInt("borrowing_id"),
                                rs.getDouble("amount"),
                                rs.getString("status"),
                                rs.getString("reason"),
                                rs.getString("created_at")
                        );
                        f.setReaderId(rs.getInt("reader_id"));
                        f.setReaderName(rs.getString("reader_name"));
                        list.add(f);
                    }
                    if (!list.isEmpty()) {
                        return list;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>(fines.values());
    }

    public static boolean saveFine(Fine fine) {
        if (!DatabaseConfig.isInMemoryMode()) {
            try (Connection conn = DatabaseConfig.getConnection()) {
                if (conn != null) {
                    String sql = "INSERT INTO fines(borrowing_id, amount, status, reason) VALUES(?,?,?,?)";
                    PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    ps.setInt(1, fine.getBorrowingId());
                    ps.setDouble(2, fine.getAmount());
                    ps.setString(3, fine.getStatus() != null ? fine.getStatus() : "UNPAID");
                    ps.setString(4, fine.getReason());
                    int aff = ps.executeUpdate();
                    if (aff > 0) {
                        ResultSet rs = ps.getGeneratedKeys();
                        if (rs.next()) fine.setFineId(rs.getInt(1));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (fine.getFineId() <= 0) {
            fine.setFineId(fineIdSeq.incrementAndGet());
        }
        if (fine.getCreatedAt() == null) {
            fine.setCreatedAt(java.time.LocalDateTime.now().toString().replace("T", " ").substring(0, 19));
        }
        Borrowing b = borrowings.get(fine.getBorrowingId());
        if (b != null) {
            fine.setReaderId(b.getReaderId());
            fine.setReaderName(b.getReaderName());
        }
        fines.put(fine.getFineId(), fine);
        return true;
    }

    public static boolean payFine(int fineId) {
        if (!DatabaseConfig.isInMemoryMode()) {
            try (Connection conn = DatabaseConfig.getConnection()) {
                if (conn != null) {
                    String sql = "UPDATE fines SET status = 'PAID' WHERE fine_id = ?";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setInt(1, fineId);
                    int aff = ps.executeUpdate();
                    if (aff > 0) {
                        String bSql = "UPDATE borrowings SET fine_amount = 0 WHERE borrowing_id = (SELECT borrowing_id FROM fines WHERE fine_id = ?)";
                        PreparedStatement bps = conn.prepareStatement(bSql);
                        bps.setInt(1, fineId);
                        bps.executeUpdate();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Fine f = fines.get(fineId);
        if (f != null) {
            f.setStatus("PAID");
            Borrowing b = borrowings.get(f.getBorrowingId());
            if (b != null) {
                b.setFineAmount(0.0);
            }
            return true;
        }
        return true;
    }
}
