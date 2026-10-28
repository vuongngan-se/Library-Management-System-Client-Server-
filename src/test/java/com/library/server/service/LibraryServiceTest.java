package com.library.server.service;

import com.library.common.model.Book;
import com.library.common.protocol.Request;
import com.library.common.protocol.Response;
import com.library.server.net.ClientSessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class LibraryServiceTest {

    private LibraryService service;

    @BeforeEach
    public void setUp() {
        service = new LibraryService();
    }

    @Test
    @DisplayName("Test Login Success")
    public void testLoginSuccess() {
        Map<String, String> data = new HashMap<>();
        data.put("username", "admin");
        data.put("password", "admin123");
        Request req = new Request("LOGIN", data);

        Response res = service.handleRequest(req);
        assertEquals("SUCCESS", res.getStatus());
        assertNotNull(res.getData());

        // Test second consecutive login (checks password is not wiped in memory)
        Response res2 = service.handleRequest(req);
        assertEquals("SUCCESS", res2.getStatus());
        assertNotNull(res2.getData());
    }

    @Test
    @DisplayName("Test Login Failure - Wrong Password")
    public void testLoginFailure() {
        Map<String, String> data = new HashMap<>();
        data.put("username", "admin");
        data.put("password", "wrongpass");
        Request req = new Request("LOGIN", data);

        Response res = service.handleRequest(req);
        assertEquals("ERROR", res.getStatus());
    }

    @Test
    @DisplayName("Test Search Books")
    public void testSearchBooks() {
        Request req = new Request("SEARCH_BOOKS", "Java");
        Response res = service.handleRequest(req);
        assertEquals("SUCCESS", res.getStatus());
        assertTrue(res.getData() instanceof List);
    }

    @Test
    @DisplayName("Test Add and Delete Book")
    public void testAddAndDeleteBook() {
        Book book = new Book();
        book.setBookId(999);
        book.setTitle("Test Unit Book");
        book.setAuthor("Test Author");
        book.setQuantity(5);
        book.setAvailableQuantity(5);

        Request addReq = new Request("ADD_BOOK", book);
        Response addRes = service.handleRequest(addReq);
        assertEquals("SUCCESS", addRes.getStatus());

        Request deleteReq = new Request("DELETE_BOOK", 999);
        Response deleteRes = service.handleRequest(deleteReq);
        assertEquals("SUCCESS", deleteRes.getStatus());
    }

    @Test
    @DisplayName("Test Borrow and Return Books Cycle")
    public void testBorrowAndReturnBooks() {
        // Prepare borrowing request
        Map<String, Object> borrowData = new HashMap<>();
        borrowData.put("readerId", 1);
        borrowData.put("userId", 1);
        borrowData.put("days", 7);
        borrowData.put("bookIds", Arrays.asList(101));

        Request borrowReq = new Request("BORROW_BOOKS", borrowData);
        Response borrowRes = service.handleRequest(borrowReq);
        assertEquals("SUCCESS", borrowRes.getStatus());

        // Extract borrowingId
        Map<?, ?> borrowingObj = (Map<?, ?>) com.library.common.protocol.NetworkUtils.getMapper().convertValue(borrowRes.getData(), Map.class);
        int borrowingId = Integer.parseInt(borrowingObj.get("borrowingId").toString());

        // Return borrowing
        Map<String, Object> returnData = new HashMap<>();
        returnData.put("borrowingId", borrowingId);
        Request returnReq = new Request("RETURN_BOOKS", returnData);
        Response returnRes = service.handleRequest(returnReq);
        assertEquals("SUCCESS", returnRes.getStatus());
    }

    @Test
    @DisplayName("Test Get Dashboard Statistics")
    public void testGetStats() {
        Request req = new Request("GET_STATS", null);
        Response res = service.handleRequest(req);
        assertEquals("SUCCESS", res.getStatus());
        assertNotNull(res.getData());
    }

    @Test
    @DisplayName("Test User Registration Success & Login")
    public void testRegisterSuccess() {
        String testUser = "testuser_" + System.currentTimeMillis();
        Map<String, String> regData = new HashMap<>();
        regData.put("fullName", "Người Dùng Thử Nghiệm");
        regData.put("username", testUser);
        regData.put("password", "pass1234");
        regData.put("role", "STAFF");

        Request regReq = new Request("REGISTER", regData);
        Response regRes = service.handleRequest(regReq);
        assertEquals("SUCCESS", regRes.getStatus());

        // Attempt duplicate register
        Response dupRes = service.handleRequest(regReq);
        assertEquals("ERROR", dupRes.getStatus());

        // Login with newly registered user
        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", testUser);
        loginData.put("password", "pass1234");
        Request loginReq = new Request("LOGIN", loginData);
        Response loginRes = service.handleRequest(loginReq);
        assertEquals("SUCCESS", loginRes.getStatus());
    }

    @Test
    @DisplayName("Test User Registration Validation")
    public void testRegisterValidation() {
        // Missing fields
        Map<String, String> regData = new HashMap<>();
        regData.put("username", "ab");
        regData.put("password", "123");
        Request regReq = new Request("REGISTER", regData);
        Response res = service.handleRequest(regReq);
        assertEquals("ERROR", res.getStatus());
    }

    @Test
    @DisplayName("Test Get Fines and Pay Fine")
    public void testGetFinesAndPayFine() {
        Request req = new Request("GET_FINES", null);
        Response res = service.handleRequest(req);
        assertEquals("SUCCESS", res.getStatus());
        assertTrue(res.getData() instanceof List);

        Request payReq = new Request("PAY_FINE", 1);
        Response payRes = service.handleRequest(payReq);
        assertEquals("SUCCESS", payRes.getStatus());
    }

    @Test
    @DisplayName("Test Renew Borrowing")
    public void testRenewBorrowing() {
        Map<String, Object> renewData = new HashMap<>();
        renewData.put("borrowingId", 501);
        renewData.put("extraDays", 7);
        Request renewReq = new Request("RENEW_BORROWING", renewData);
        Response renewRes = service.handleRequest(renewReq);
        assertEquals("SUCCESS", renewRes.getStatus());
    }

    @Test
    @DisplayName("Test Dashboard Stats with Category Distribution and Top Borrowed Books")
    public void testDashboardStatsAndCharts() {
        Request req = new Request("GET_STATS", null);
        Response res = service.handleRequest(req);
        assertEquals("SUCCESS", res.getStatus());
        assertTrue(res.getData() instanceof Map);

        Map<?, ?> stats = (Map<?, ?>) res.getData();
        assertNotNull(stats.get("totalBooks"));
        assertNotNull(stats.get("availableBooks"));
        assertNotNull(stats.get("totalReaders"));
        assertNotNull(stats.get("activeBorrowings"));

        Object catDist = stats.get("categoryDistribution");
        assertNotNull(catDist);
        assertTrue(catDist instanceof Map);
        Map<?, ?> catMap = (Map<?, ?>) catDist;
        assertFalse(catMap.isEmpty(), "Category distribution should not be empty");

        Object topBooks = stats.get("topBorrowedBooks");
        assertNotNull(topBooks);
        assertTrue(topBooks instanceof Map);
        Map<?, ?> topMap = (Map<?, ?>) topBooks;
        assertFalse(topMap.isEmpty(), "Top borrowed books should not be empty");
    }

    @Test
    @DisplayName("Test Export Books CSV")
    public void testExportBooksCsv() {
        Request req = new Request("EXPORT_BOOKS_CSV", null);
        Response res = service.handleRequest(req);
        assertEquals("SUCCESS", res.getStatus());
        assertTrue(res.getData() instanceof Map);

        Map<?, ?> map = (Map<?, ?>) res.getData();
        assertNotNull(map.get("fileName"));
        String csv = (String) map.get("csvData");
        assertNotNull(csv);
        assertTrue(csv.startsWith("\uFEFF"), "CSV must include UTF-8 BOM");
        assertTrue(csv.contains("Mã sách,Tên sách,Tác giả"));
    }

    @Test
    @DisplayName("Test Export Borrowings CSV")
    public void testExportBorrowingsCsv() {
        Request req = new Request("EXPORT_BORROWINGS_CSV", null);
        Response res = service.handleRequest(req);
        assertEquals("SUCCESS", res.getStatus());
        assertTrue(res.getData() instanceof Map);

        Map<?, ?> map = (Map<?, ?>) res.getData();
        assertNotNull(map.get("fileName"));
        String csv = (String) map.get("csvData");
        assertNotNull(csv);
        assertTrue(csv.startsWith("\uFEFF"), "CSV must include UTF-8 BOM");
        assertTrue(csv.contains("Mã phiếu,Mã độc giả,Tên độc giả"));
    }

    @Test
    @DisplayName("Test Export Fines CSV")
    public void testExportFinesCsv() {
        Request req = new Request("EXPORT_FINES_CSV", null);
        Response res = service.handleRequest(req);
        assertEquals("SUCCESS", res.getStatus());
        assertTrue(res.getData() instanceof Map);

        Map<?, ?> map = (Map<?, ?>) res.getData();
        assertNotNull(map.get("fileName"));
        String csv = (String) map.get("csvData");
        assertNotNull(csv);
        assertTrue(csv.startsWith("\uFEFF"), "CSV must include UTF-8 BOM");
        assertTrue(csv.contains("Mã phiếu phạt,Mã phiếu mượn"));
    }

    @Test
    @DisplayName("Test Active Sessions and Kick Session")
    public void testActiveSessionsAndKickSession() {
        ClientSessionManager.register("TEST-SES-001", "192.168.1.50", 61234, null);
        ClientSessionManager.updateUser("TEST-SES-001", "staff_test", "Staff Tester", "STAFF");
        ClientSessionManager.recordRequest("TEST-SES-001", "GET_BOOKS");

        Request req = new Request("GET_ACTIVE_SESSIONS", null);
        Response res = service.handleRequest(req);
        assertEquals("SUCCESS", res.getStatus());
        assertTrue(res.getData() instanceof Map);

        Map<?, ?> data = (Map<?, ?>) res.getData();
        assertTrue(((Number) data.get("activeCount")).intValue() >= 1);
        assertTrue(((Number) data.get("totalRequests")).intValue() >= 1);

        // Test Kick session
        Request kickReq = new Request("KICK_SESSION", "TEST-SES-001");
        Response kickRes = service.handleRequest(kickReq);
        assertEquals("SUCCESS", kickRes.getStatus());

        // Kick non-existent session
        Request kickInvalid = new Request("KICK_SESSION", "NON-EXISTENT");
        Response invalidRes = service.handleRequest(kickInvalid);
        assertEquals("ERROR", invalidRes.getStatus());
    }
}

