package com.library.server.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.common.model.*;
import com.library.common.protocol.NetworkUtils;
import com.library.common.protocol.Request;
import com.library.common.protocol.Response;
import com.library.server.dao.InMemoryStore;
import com.library.server.net.ChatManager;
import com.library.server.net.ClientSessionManager;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class LibraryService {
    private static final ObjectMapper mapper = NetworkUtils.getMapper();
    private static final Map<Integer, Borrowing> borrowingsStore = new ConcurrentHashMap<>();
    private static final AtomicInteger borrowingSeq = new AtomicInteger(500);

    public Response handleRequest(Request request) {
        if (request == null || request.getAction() == null) {
            return Response.error("Yêu cầu không hợp lệ.");
        }

        String action = request.getAction();
        Object data = request.getData();

        try {
            switch (action) {
                case "LOGIN":
                    return handleLogin(data);
                case "REGISTER":
                    return handleRegister(data);
                case "GET_BOOKS":
                    return Response.success("Lấy danh sách sách thành công", InMemoryStore.getAllBooks());
                case "SEARCH_BOOKS":
                    return handleSearchBooks(data);
                case "ADD_BOOK":
                case "UPDATE_BOOK":
                    return handleSaveBook(data);
                case "DELETE_BOOK":
                    return handleDeleteBook(data);
                case "GET_READERS":
                    return Response.success("Lấy danh sách độc giả thành công", InMemoryStore.getAllReaders());
                case "ADD_READER":
                case "UPDATE_READER":
                    return handleSaveReader(data);
                case "GET_CATEGORIES":
                    return Response.success("Lấy danh sách thể loại thành công", InMemoryStore.getAllCategories());
                case "BORROW_BOOKS":
                    return handleBorrowBooks(data);
                case "RETURN_BOOKS":
                    return handleReturnBooks(data);
                case "RENEW_BORROWING":
                    return handleRenewBorrowing(data);
                case "GET_BORROWINGS":
                    return Response.success("Lấy lịch sử mượn trả thành công", InMemoryStore.getAllBorrowings());
                case "GET_FINES":
                    return Response.success("Lấy danh sách phiếu phạt thành công", InMemoryStore.getAllFines());
                case "PAY_FINE":
                    return handlePayFine(data);
                case "GET_STATS":
                    return handleGetStats();
                case "EXPORT_BOOKS_CSV":
                    return handleExportBooksCsv();
                case "EXPORT_BORROWINGS_CSV":
                    return handleExportBorrowingsCsv();
                case "EXPORT_FINES_CSV":
                    return handleExportFinesCsv();
                case "GET_ACTIVE_SESSIONS":
                    return handleGetActiveSessions();
                case "KICK_SESSION":
                    return handleKickSession(data);
                case "GET_USERS":
                    return Response.success("Lấy danh sách người dùng thành công", InMemoryStore.getAllUsers());
                case "ADD_USER":
                    return handleAddUser(data);
                case "UPDATE_USER":
                    return handleUpdateUser(data);
                case "RESET_PASSWORD":
                    return handleResetPassword(data);
                case "DELETE_USER":
                    return handleDeleteUser(data);
                case "REGISTER_CHAT_LISTENER":
                    return Response.success("Đã đăng ký kênh Chat Listener", null);
                case "SEND_CHAT_MESSAGE":
                    return handleSendChatMessage(data);
                case "GET_CHAT_HISTORY":
                    return Response.success("Lấy lịch sử trò chuyện thành công", ChatManager.getHistory());
                case "CLEAR_CHAT_HISTORY":
                    ChatManager.clearHistory();
                    return Response.success("Đã xóa lịch sử trò chuyện", null);
                default:
                    return Response.error("Hành động không được hỗ trợ: " + action);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("Lỗi xử lý yêu cầu trên Server: " + e.getMessage());
        }
    }

    private Response handleLogin(Object data) {
        Map<?, ?> map = mapper.convertValue(data, Map.class);
        String username = (String) map.get("username");
        String password = (String) map.get("password");

        if (username == null || password == null) {
            return Response.error("Tên đăng nhập và mật khẩu không được trống.");
        }

        User user = InMemoryStore.findUserByUsername(username);
        if (user != null && user.getPassword() != null) {
            boolean matches = user.getPassword().equals(password);
            if (!matches && user.getPassword().startsWith("$2a$")) {
                try {
                    matches = org.mindrot.jbcrypt.BCrypt.checkpw(password, user.getPassword());
                } catch (Exception ignored) {}
            }
            if (matches) {
                User safeUser = new User(
                        user.getUserId(),
                        user.getUsername(),
                        null,
                        user.getFullName(),
                        user.getRole(),
                        user.getStatus()
                );
                return Response.success("Đăng nhập thành công", safeUser);
            }
        }
        return Response.error("Tài khoản hoặc mật khẩu không chính xác.");
    }

    private Response handleRegister(Object data) {
        if (data == null) {
            return Response.error("Dữ liệu đăng ký không hợp lệ.");
        }
        Map<?, ?> map = mapper.convertValue(data, Map.class);
        String username = map.get("username") != null ? map.get("username").toString().trim() : null;
        String password = map.get("password") != null ? map.get("password").toString() : null;
        String fullName = map.get("fullName") != null ? map.get("fullName").toString().trim() : null;
        String role = map.get("role") != null ? map.get("role").toString().trim() : "STAFF";

        if (username == null || username.isEmpty() || password == null || password.isEmpty() || fullName == null || fullName.isEmpty()) {
            return Response.error("Vui lòng điền đầy đủ họ tên, tên đăng nhập và mật khẩu.");
        }

        if (username.length() < 3) {
            return Response.error("Tên đăng nhập phải có ít nhất 3 ký tự.");
        }

        if (password.length() < 6) {
            return Response.error("Mật khẩu phải có ít nhất 6 ký tự.");
        }

        // Check if username already exists
        User existing = InMemoryStore.findUserByUsername(username);
        if (existing != null) {
            return Response.error("Tên đăng nhập '" + username + "' đã được sử dụng. Vui lòng chọn tên khác.");
        }

        User newUser = new User(0, username, password, fullName, role, "ACTIVE");
        boolean ok = InMemoryStore.registerUser(newUser);
        if (ok) {
            User safeUser = new User(newUser.getUserId(), newUser.getUsername(), null, newUser.getFullName(), newUser.getRole(), newUser.getStatus());
            return Response.success("Đăng ký tài khoản thành công!", safeUser);
        }
        return Response.error("Đăng ký tài khoản thất bại do lỗi hệ thống.");
    }

    private Response handleSearchBooks(Object data) {
        String keyword = data != null ? data.toString().trim().toLowerCase() : "";
        List<Book> books = InMemoryStore.getAllBooks();
        if (keyword.isEmpty()) {
            return Response.success("Tìm kiếm thành công", books);
        }

        List<Book> filtered = books.stream().filter(b ->
                b.getTitle().toLowerCase().contains(keyword) ||
                b.getAuthor().toLowerCase().contains(keyword) ||
                (b.getCategoryName() != null && b.getCategoryName().toLowerCase().contains(keyword)) ||
                (b.getIsbn() != null && b.getIsbn().toLowerCase().contains(keyword))
        ).collect(Collectors.toList());

        return Response.success("Tìm kiếm thành công", filtered);
    }

    private Response handleSaveBook(Object data) {
        Book book = mapper.convertValue(data, Book.class);
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            return Response.error("Tên sách không được để trống.");
        }
        boolean ok = InMemoryStore.saveBook(book);
        if (ok) {
            ChatManager.broadcast(new ChatMessage("SYSTEM", "Hệ thống", "SYSTEM", "Thông tin sách '" + book.getTitle() + "' đã được cập nhật.", "SYNC_BOOKS"));
            return Response.success("Lưu thông tin sách thành công", book);
        }
        return Response.error("Thất bại khi lưu thông tin sách.");
    }

    private Response handleDeleteBook(Object data) {
        int bookId = 0;
        if (data instanceof Integer) {
            bookId = (Integer) data;
        } else if (data instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) data;
            bookId = Integer.parseInt(map.get("bookId").toString());
        }

        boolean ok = InMemoryStore.deleteBook(bookId);
        if (ok) {
            ChatManager.broadcast(new ChatMessage("SYSTEM", "Hệ thống", "SYSTEM", "Một đầu sách (ID: " + bookId + ") đã được xóa khỏi hệ thống.", "SYNC_BOOKS"));
            return Response.success("Xóa sách thành công", bookId);
        }
        return Response.error("Không thể xóa sách.");
    }

    private Response handleSaveReader(Object data) {
        Reader reader = mapper.convertValue(data, Reader.class);
        if (reader.getFullName() == null || reader.getFullName().trim().isEmpty()) {
            return Response.error("Tên độc giả không được để trống.");
        }
        boolean ok = InMemoryStore.saveReader(reader);
        if (ok) {
            ChatManager.broadcast(new ChatMessage("SYSTEM", "Hệ thống", "SYSTEM", "Thông tin độc giả '" + reader.getFullName() + "' đã được lưu.", "SYNC_READERS"));
            return Response.success("Lưu thông tin độc giả thành công", reader);
        }
        return Response.error("Thất bại khi lưu độc giả.");
    }

    private Response handleBorrowBooks(Object data) {
        Map<?, ?> map = mapper.convertValue(data, Map.class);
        int readerId = Integer.parseInt(map.get("readerId").toString());
        int userId = Integer.parseInt(map.get("userId").toString());
        int days = map.containsKey("days") ? Integer.parseInt(map.get("days").toString()) : 14;

        List<Integer> bookIds = mapper.convertValue(map.get("bookIds"), new TypeReference<List<Integer>>() {});

        if (bookIds == null || bookIds.isEmpty()) {
            return Response.error("Danh sách sách mượn không được rỗng.");
        }

        List<Book> allBooks = InMemoryStore.getAllBooks();
        for (int bId : bookIds) {
            Book b = allBooks.stream().filter(x -> x.getBookId() == bId).findFirst().orElse(null);
            if (b == null) {
                return Response.error("Mã sách " + bId + " không tồn tại.");
            }
            if (b.getAvailableQuantity() <= 0) {
                return Response.error("Sách '" + b.getTitle() + "' đã hết bản khả dụng.");
            }
        }

        // Deduct quantity
        for (int bId : bookIds) {
            Book b = allBooks.stream().filter(x -> x.getBookId() == bId).findFirst().orElse(null);
            if (b != null) {
                b.setAvailableQuantity(b.getAvailableQuantity() - 1);
                InMemoryStore.saveBook(b);
            }
        }

        Borrowing borrowing = new Borrowing();
        borrowing.setBorrowingId(borrowingSeq.incrementAndGet());
        borrowing.setReaderId(readerId);
        borrowing.setUserId(userId);
        borrowing.setBorrowDate(LocalDate.now().toString());
        borrowing.setDueDate(LocalDate.now().plusDays(days).toString());
        borrowing.setStatus("BORROWED");

        List<BorrowingDetail> details = new ArrayList<>();
        for (int bId : bookIds) {
            Book b = allBooks.stream().filter(x -> x.getBookId() == bId).findFirst().orElse(null);
            BorrowingDetail bd = new BorrowingDetail();
            bd.setBorrowingId(borrowing.getBorrowingId());
            bd.setBookId(bId);
            bd.setBookTitle(b != null ? b.getTitle() : "Mã " + bId);
            bd.setQuantity(1);
            bd.setStatus("BORROWED");
            details.add(bd);
        }
        borrowing.setDetails(details);
        InMemoryStore.saveBorrowing(borrowing);
        borrowingsStore.put(borrowing.getBorrowingId(), borrowing);

        ChatManager.broadcast(new ChatMessage("SYSTEM", "Hệ thống", "SYSTEM", "Phiếu mượn #" + borrowing.getBorrowingId() + " vừa được tạo. Kho sách đã cập nhật tự động.", "SYNC_BOOKS"));

        return Response.success("Tạo phiếu mượn sách thành công", borrowing);
    }

    private Response handleReturnBooks(Object data) {
        Map<?, ?> map = mapper.convertValue(data, Map.class);
        int borrowingId = Integer.parseInt(map.get("borrowingId").toString());

        Borrowing b = InMemoryStore.findBorrowingById(borrowingId);
        if (b == null) {
            b = borrowingsStore.get(borrowingId);
        }
        if (b == null) {
            return Response.error("Không tìm thấy mã phiếu mượn: " + borrowingId);
        }

        if ("RETURNED".equals(b.getStatus())) {
            return Response.error("Phiếu mượn này đã được trả trước đó.");
        }

        LocalDate now = LocalDate.now();
        b.setStatus("RETURNED");
        b.setReturnDate(now.toString());

        // Check if overdue and calculate fine
        if (b.getDueDate() != null) {
            try {
                LocalDate dueDate = LocalDate.parse(b.getDueDate());
                if (now.isAfter(dueDate)) {
                    long overdueDays = java.time.temporal.ChronoUnit.DAYS.between(dueDate, now);
                    if (overdueDays > 0) {
                        double fineAmount = overdueDays * 5000.0;
                        b.setFineAmount(fineAmount);
                        Fine fine = new Fine(
                                0,
                                borrowingId,
                                fineAmount,
                                "UNPAID",
                                "Trễ hạn " + overdueDays + " ngày khi trả sách",
                                now.toString()
                        );
                        InMemoryStore.saveFine(fine);
                    }
                }
            } catch (Exception ignored) {}
        }

        // Restore quantity
        List<Book> allBooks = InMemoryStore.getAllBooks();
        if (b.getDetails() != null) {
            for (BorrowingDetail detail : b.getDetails()) {
                detail.setStatus("RETURNED");
                detail.setReturnedQuantity(detail.getQuantity());
                Book book = allBooks.stream().filter(x -> x.getBookId() == detail.getBookId()).findFirst().orElse(null);
                if (book != null) {
                    book.setAvailableQuantity(book.getAvailableQuantity() + detail.getQuantity());
                    InMemoryStore.saveBook(book);
                }
            }
        }

        InMemoryStore.updateBorrowing(b);
        borrowingsStore.put(borrowingId, b);

        ChatManager.broadcast(new ChatMessage("SYSTEM", "Hệ thống", "SYSTEM", "Phiếu mượn #" + borrowingId + " vừa hoàn tất trả sách. Sách đã trở lại kho.", "SYNC_BOOKS"));

        return Response.success("Xử lý trả sách thành công", b);
    }

    private Response handleRenewBorrowing(Object data) {
        if (data == null) {
            return Response.error("Dữ liệu gia hạn không hợp lệ.");
        }
        Map<?, ?> map = mapper.convertValue(data, Map.class);
        int borrowingId = Integer.parseInt(map.get("borrowingId").toString());
        int extraDays = map.containsKey("extraDays") ? Integer.parseInt(map.get("extraDays").toString()) : 7;

        Borrowing b = InMemoryStore.findBorrowingById(borrowingId);
        if (b == null) {
            b = borrowingsStore.get(borrowingId);
        }
        if (b == null) {
            return Response.error("Không tìm thấy phiếu mượn #" + borrowingId);
        }
        if (!"BORROWED".equals(b.getStatus())) {
            return Response.error("Chỉ có thể gia hạn phiếu mượn đang trong trạng thái 'Đang mượn'.");
        }

        boolean ok = InMemoryStore.renewBorrowing(borrowingId, extraDays);
        if (ok) {
            Borrowing updated = InMemoryStore.findBorrowingById(borrowingId);
            ChatManager.broadcast(new ChatMessage("SYSTEM", "Hệ thống", "SYSTEM", "Phiếu mượn #" + borrowingId + " vừa được gia hạn thêm " + extraDays + " ngày.", "SYNC_BOOKS"));
            return Response.success("Gia hạn mượn sách thành công! Hạn trả mới: " + (updated != null ? updated.getDueDate() : ""), updated);
        }
        return Response.error("Không thể gia hạn phiếu mượn.");
    }

    private Response handlePayFine(Object data) {
        int fineId = 0;
        if (data instanceof Integer) {
            fineId = (Integer) data;
        } else if (data instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) data;
            fineId = Integer.parseInt(map.get("fineId").toString());
        } else if (data != null) {
            fineId = Integer.parseInt(data.toString());
        }

        if (fineId <= 0) {
            return Response.error("Mã phiếu phạt không hợp lệ.");
        }

        boolean ok = InMemoryStore.payFine(fineId);
        if (ok) {
            ChatManager.broadcast(new ChatMessage("SYSTEM", "Hệ thống", "SYSTEM", "Phiếu phạt #" + fineId + " vừa được thanh toán thành công.", "SYNC_FINES"));
            return Response.success("Thu tiền phạt thành công!", fineId);
        }
        return Response.error("Không thể cập nhật trạng thái phiếu phạt.");
    }

    private Response handleGetStats() {
        List<Book> books = InMemoryStore.getAllBooks();
        List<Reader> readers = InMemoryStore.getAllReaders();
        List<Borrowing> borrowings = InMemoryStore.getAllBorrowings();

        int totalBooks = books.stream().mapToInt(Book::getQuantity).sum();
        int availableBooks = books.stream().mapToInt(Book::getAvailableQuantity).sum();
        int borrowedCount = (int) borrowings.stream().filter(b -> "BORROWED".equals(b.getStatus())).count();
        int overdueCount = 0;
        LocalDate now = LocalDate.now();

        for (Borrowing b : borrowings) {
            if ("OVERDUE".equals(b.getStatus())) {
                overdueCount++;
            } else if ("BORROWED".equals(b.getStatus()) && b.getDueDate() != null) {
                try {
                    LocalDate due = LocalDate.parse(b.getDueDate());
                    if (now.isAfter(due)) overdueCount++;
                } catch (Exception ignored) {}
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBookTitles", books.size());
        stats.put("totalBooks", totalBooks);
        stats.put("availableBooks", availableBooks);
        stats.put("totalReaders", readers.size());
        stats.put("activeBorrowings", borrowedCount);
        stats.put("overdueBorrowings", overdueCount);
        stats.put("categoryDistribution", InMemoryStore.getCategoryDistribution());
        stats.put("topBorrowedBooks", InMemoryStore.getTopBorrowedBooks());

        return Response.success("Lấy thống kê thành công", stats);
    }

    private String escapeCsv(Object val) {
        if (val == null) return "";
        String str = String.valueOf(val);
        if (str.contains(",") || str.contains("\"") || str.contains("\n") || str.contains("\r")) {
            str = "\"" + str.replace("\"", "\"\"") + "\"";
        }
        return str;
    }

    private Response handleExportBooksCsv() {
        List<Book> list = InMemoryStore.getAllBooks();
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF'); // UTF-8 BOM for MS Excel compatibility
        sb.append("Mã sách,Tên sách,Tác giả,Thể loại,Nhà xuất bản,Năm xuất bản,Tổng số,Khả dụng,ISBN\n");
        for (Book b : list) {
            sb.append(escapeCsv(b.getBookId())).append(",")
              .append(escapeCsv(b.getTitle())).append(",")
              .append(escapeCsv(b.getAuthor())).append(",")
              .append(escapeCsv(b.getCategoryName())).append(",")
              .append(escapeCsv(b.getPublisher())).append(",")
              .append(escapeCsv(b.getPublishYear())).append(",")
              .append(escapeCsv(b.getQuantity())).append(",")
              .append(escapeCsv(b.getAvailableQuantity())).append(",")
              .append(escapeCsv(b.getIsbn())).append("\n");
        }

        Map<String, String> res = new HashMap<>();
        res.put("fileName", "danh_sach_sach_" + LocalDate.now() + ".csv");
        res.put("csvData", sb.toString());
        return Response.success("Xuất danh sách sách CSV thành công", res);
    }

    private Response handleExportBorrowingsCsv() {
        List<Borrowing> list = InMemoryStore.getAllBorrowings();
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');
        sb.append("Mã phiếu,Mã độc giả,Tên độc giả,Thủ thư,Ngày mượn,Hạn trả,Ngày trả,Trạng thái,Tiền phạt\n");
        for (Borrowing b : list) {
            sb.append(escapeCsv(b.getBorrowingId())).append(",")
              .append(escapeCsv(b.getReaderId())).append(",")
              .append(escapeCsv(b.getReaderName())).append(",")
              .append(escapeCsv(b.getUserName())).append(",")
              .append(escapeCsv(b.getBorrowDate())).append(",")
              .append(escapeCsv(b.getDueDate())).append(",")
              .append(escapeCsv(b.getReturnDate())).append(",")
              .append(escapeCsv(b.getStatus())).append(",")
              .append(escapeCsv(b.getFineAmount())).append("\n");
        }

        Map<String, String> res = new HashMap<>();
        res.put("fileName", "lich_su_muon_tra_" + LocalDate.now() + ".csv");
        res.put("csvData", sb.toString());
        return Response.success("Xuất lịch sử mượn trả CSV thành công", res);
    }

    private Response handleExportFinesCsv() {
        List<Fine> list = InMemoryStore.getAllFines();
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');
        sb.append("Mã phiếu phạt,Mã phiếu mượn,Mã độc giả,Tên độc giả,Số tiền (VNĐ),Trạng thái,Lý do vi phạm,Ngày tạo\n");
        for (Fine f : list) {
            sb.append(escapeCsv(f.getFineId())).append(",")
              .append(escapeCsv(f.getBorrowingId())).append(",")
              .append(escapeCsv(f.getReaderId())).append(",")
              .append(escapeCsv(f.getReaderName())).append(",")
              .append(escapeCsv(f.getAmount())).append(",")
              .append(escapeCsv(f.getStatus())).append(",")
              .append(escapeCsv(f.getReason())).append(",")
              .append(escapeCsv(f.getCreatedAt())).append("\n");
        }

        Map<String, String> res = new HashMap<>();
        res.put("fileName", "danh_sach_tien_phat_" + LocalDate.now() + ".csv");
        res.put("csvData", sb.toString());
        return Response.success("Xuất danh sách tiền phạt CSV thành công", res);
    }

    private Response handleGetActiveSessions() {
        Map<String, Object> map = new HashMap<>();
        map.put("sessions", ClientSessionManager.getAllSessions());
        map.put("activeCount", ClientSessionManager.getActiveSessionCount());
        map.put("totalRequests", ClientSessionManager.getTotalRequests());
        map.put("serverPort", 8888);
        return Response.success("Lấy danh sách phiên kết nối thành công", map);
    }

    private Response handleKickSession(Object data) {
        String sessionId = null;
        if (data instanceof String) {
            sessionId = (String) data;
        } else if (data instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) data;
            Object sid = map.get("sessionId");
            if (sid != null) sessionId = sid.toString();
        }

        if (sessionId == null || sessionId.trim().isEmpty()) {
            return Response.error("Mã phiên kết nối không hợp lệ.");
        }

        boolean ok = ClientSessionManager.kickSession(sessionId.trim());
        if (ok) {
            return Response.success("Đã ngắt kết nối thành công phiên: " + sessionId, sessionId);
        } else {
            return Response.error("Không tìm thấy phiên kết nối đang mở với mã: " + sessionId);
        }
    }

    private Response handleAddUser(Object data) {
        User user = mapper.convertValue(data, User.class);
        if (user.getUsername() == null || user.getUsername().trim().length() < 3) {
            return Response.error("Tên đăng nhập phải có ít nhất 3 ký tự.");
        }
        if (user.getPassword() == null || user.getPassword().trim().length() < 4) {
            return Response.error("Mật khẩu phải có ít nhất 4 ký tự.");
        }
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            return Response.error("Họ và tên không được để trống.");
        }

        User existing = InMemoryStore.findUserByUsername(user.getUsername().trim());
        if (existing != null) {
            return Response.error("Tên đăng nhập '" + user.getUsername() + "' đã tồn tại!");
        }

        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            user.setRole("STAFF");
        }
        if (user.getStatus() == null || user.getStatus().trim().isEmpty()) {
            user.setStatus("ACTIVE");
        }

        boolean ok = InMemoryStore.registerUser(user);
        if (ok) {
            User safe = new User(user.getUserId(), user.getUsername(), null, user.getFullName(), user.getRole(), user.getStatus());
            return Response.success("Tạo tài khoản người dùng thành công!", safe);
        }
        return Response.error("Không thể tạo tài khoản trên máy chủ.");
    }

    private Response handleUpdateUser(Object data) {
        User user = mapper.convertValue(data, User.class);
        if (user.getUserId() <= 0) {
            return Response.error("Mã người dùng không hợp lệ.");
        }
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            return Response.error("Họ và tên không được để trống.");
        }

        boolean ok = InMemoryStore.updateUser(user);
        if (ok) {
            return Response.success("Cập nhật thông tin tài khoản thành công!", user);
        }
        return Response.error("Không tìm thấy người dùng để cập nhật.");
    }

    private Response handleResetPassword(Object data) {
        Map<?, ?> map = mapper.convertValue(data, Map.class);
        Object uidObj = map.get("userId");
        Object passObj = map.get("newPassword");
        if (uidObj == null || passObj == null) {
            return Response.error("Mã người dùng và mật khẩu mới không được để trống.");
        }
        int userId = Integer.parseInt(uidObj.toString());
        String newPassword = passObj.toString().trim();
        if (newPassword.length() < 4) {
            return Response.error("Mật khẩu mới phải có ít nhất 4 ký tự.");
        }

        boolean ok = InMemoryStore.resetPassword(userId, newPassword);
        if (ok) {
            return Response.success("Đổi mật khẩu người dùng thành công!", userId);
        }
        return Response.error("Không tìm thấy người dùng để đổi mật khẩu.");
    }

    private Response handleDeleteUser(Object data) {
        int userId = 0;
        if (data instanceof Integer) {
            userId = (Integer) data;
        } else if (data instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) data;
            Object idObj = map.get("userId");
            if (idObj != null) userId = Integer.parseInt(idObj.toString());
        } else if (data != null) {
            userId = Integer.parseInt(data.toString());
        }

        if (userId <= 0) {
            return Response.error("Mã người dùng không hợp lệ.");
        }
        if (userId == 1) {
            return Response.error("Không thể xóa tài khoản Quản trị viên gốc (admin)!");
        }

        boolean ok = InMemoryStore.deleteUser(userId);
        if (ok) {
            return Response.success("Đã xóa tài khoản thành công!", userId);
        }
        return Response.error("Không tìm thấy người dùng để xóa.");
    }

    private Response handleSendChatMessage(Object data) {
        if (data == null) {
            return Response.error("Dữ liệu tin nhắn rỗng.");
        }
        Map<?, ?> map = mapper.convertValue(data, Map.class);
        String sender = (String) map.get("sender");
        String senderName = (String) map.get("senderName");
        String senderRole = (String) map.get("senderRole");
        String message = (String) map.get("message");
        String type = (String) map.get("type");

        if (message == null || message.trim().isEmpty()) {
            return Response.error("Nội dung tin nhắn không được để trống.");
        }

        if (sender == null) sender = "Unknown";
        if (senderName == null) senderName = sender;
        if (senderRole == null) senderRole = "STAFF";
        if (type == null) type = "CHAT";

        ChatMessage chatMsg = new ChatMessage(sender, senderName, senderRole, message.trim(), type);
        ChatManager.broadcast(chatMsg);
        return Response.success("Gửi tin nhắn thành công", chatMsg);
    }
}
