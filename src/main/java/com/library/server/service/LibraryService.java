package com.library.server.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.common.model.*;
import com.library.common.protocol.NetworkUtils;
import com.library.common.protocol.Request;
import com.library.common.protocol.Response;
import com.library.server.dao.InMemoryStore;

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

        return Response.success("Lấy thống kê thành công", stats);
    }
}
