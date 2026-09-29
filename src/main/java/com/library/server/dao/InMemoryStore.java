package com.library.server.dao;

import com.library.common.model.Book;
import com.library.common.model.Category;
import com.library.common.model.Reader;
import com.library.common.model.User;
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

    private static final AtomicInteger userIdSeq = new AtomicInteger(100);
    private static final AtomicInteger catIdSeq = new AtomicInteger(100);
    private static final AtomicInteger bookIdSeq = new AtomicInteger(100);
    private static final AtomicInteger readerIdSeq = new AtomicInteger(100);

    static {
        // Seed default memory data
        User admin = new User(1, "admin", "admin123", "Quản trị viên", "ADMIN", "ACTIVE");
        User staff = new User(2, "staff", "staff123", "Nhân viên thủ thư", "STAFF", "ACTIVE");
        users.put(1, admin);
        users.put(2, staff);

        Category c1 = new Category(1, "Lập trình", "Công nghệ thông tin & Thuật toán");
        Category c2 = new Category(2, "Văn học", "Tiểu thuyết & Truyện ngắn");
        categories.put(1, c1);
        categories.put(2, c2);

        Book b1 = new Book(101, "Lập trình Java Socket", "Nguyễn Văn A", 1, "NXB Giáo Dục", 2023, 10, 10, "978-12345");
        b1.setCategoryName("Lập trình");
        Book b2 = new Book(102, "Cơ sở dữ liệu MySQL", "Trần Thị B", 1, "NXB Thống Kê", 2022, 5, 4, "978-67890");
        b2.setCategoryName("Lập trình");
        books.put(101, b1);
        books.put(102, b2);

        Reader r1 = new Reader(1, "Phạm Văn Đồng", "0912345678", "dong@gmail.com", "Đà Nẵng", "ACTIVE");
        Reader r2 = new Reader(2, "Nguyễn Thị Mai", "0987654321", "mai@gmail.com", "Quảng Nam", "ACTIVE");
        readers.put(1, r1);
        readers.put(2, r2);
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
        return users.values().stream().filter(u -> u.getUsername().equals(username)).findFirst().orElse(null);
    }

    public static List<Book> getAllBooks() {
        if (!DatabaseConfig.isInMemoryMode()) {
            List<Book> list = new ArrayList<>();
            try (Connection conn = DatabaseConfig.getConnection()) {
                if (conn != null) {
                    String sql = "SELECT b.*, c.category_name FROM books b LEFT JOIN categories c ON b.category_id = c.category_id";
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
                    return list;
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
                        PreparedStatement ps = conn.prepareStatement(sql);
                        ps.setString(1, b.getTitle());
                        ps.setString(2, b.getAuthor());
                        ps.setInt(3, b.getCategoryId());
                        ps.setString(4, b.getPublisher());
                        ps.setInt(5, b.getPublishYear());
                        ps.setInt(6, b.getQuantity());
                        ps.setInt(7, b.getQuantity());
                        ps.setString(8, b.getIsbn());
                        return ps.executeUpdate() > 0;
                    } else {
                        String sql = "UPDATE books SET title=?, author=?, category_id=?, publisher=?, publish_year=?, quantity=?, isbn=? WHERE book_id=?";
                        PreparedStatement ps = conn.prepareStatement(sql);
                        ps.setString(1, b.getTitle());
                        ps.setString(2, b.getAuthor());
                        ps.setInt(3, b.getCategoryId());
                        ps.setString(4, b.getPublisher());
                        ps.setInt(5, b.getPublishYear());
                        ps.setInt(6, b.getQuantity());
                        ps.setString(7, b.getIsbn());
                        ps.setInt(8, b.getBookId());
                        return ps.executeUpdate() > 0;
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
        books.put(b.getBookId(), b);
        return true;
    }

    public static boolean deleteBook(int bookId) {
        if (!DatabaseConfig.isInMemoryMode()) {
            try (Connection conn = DatabaseConfig.getConnection()) {
                if (conn != null) {
                    PreparedStatement ps = conn.prepareStatement("DELETE FROM books WHERE book_id = ?");
                    ps.setInt(1, bookId);
                    return ps.executeUpdate() > 0;
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
                    PreparedStatement ps = conn.prepareStatement("SELECT * FROM readers");
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
                    return list;
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
                        PreparedStatement ps = conn.prepareStatement(sql);
                        ps.setString(1, r.getFullName());
                        ps.setString(2, r.getPhone());
                        ps.setString(3, r.getEmail());
                        ps.setString(4, r.getAddress());
                        ps.setString(5, r.getCardStatus());
                        return ps.executeUpdate() > 0;
                    } else {
                        String sql = "UPDATE readers SET full_name=?, phone=?, email=?, address=?, card_status=? WHERE reader_id=?";
                        PreparedStatement ps = conn.prepareStatement(sql);
                        ps.setString(1, r.getFullName());
                        ps.setString(2, r.getPhone());
                        ps.setString(3, r.getEmail());
                        ps.setString(4, r.getAddress());
                        ps.setString(5, r.getCardStatus());
                        ps.setInt(6, r.getReaderId());
                        return ps.executeUpdate() > 0;
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
                    PreparedStatement ps = conn.prepareStatement("SELECT * FROM categories");
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        list.add(new Category(
                                rs.getInt("category_id"),
                                rs.getString("category_name"),
                                rs.getString("description")
                        ));
                    }
                    return list;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>(categories.values());
    }
}
