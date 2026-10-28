package com.library.server.service;

import com.library.common.model.Book;
import com.library.common.model.User;
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

    @Test
    @DisplayName("Test User Management Lifecycle (CRUD, Reset Password, Role check)")
    public void testUserManagementLifecycle() {
        // 1. Get all users
        Request getReq = new Request("GET_USERS", null);
        Response getRes = service.handleRequest(getReq);
        assertEquals("SUCCESS", getRes.getStatus());
        assertTrue(getRes.getData() instanceof List);
        List<?> userList = (List<?>) getRes.getData();
        assertTrue(userList.size() >= 5);

        // 2. Add new user
        User newUser = new User(0, "staff_crud", "pass1234", "Thủ thư Thử nghiệm", "STAFF", "ACTIVE");
        Request addReq = new Request("ADD_USER", newUser);
        Response addRes = service.handleRequest(addReq);
        assertEquals("SUCCESS", addRes.getStatus());
        Map<?, ?> createdMap = (Map<?, ?>) com.library.common.protocol.NetworkUtils.getMapper().convertValue(addRes.getData(), Map.class);
        int createdId = Integer.parseInt(createdMap.get("userId").toString());
        assertTrue(createdId > 0);

        // 3. Test duplicate add fails
        Response dupRes = service.handleRequest(addReq);
        assertEquals("ERROR", dupRes.getStatus());

        // 4. Update user
        User toUpdate = new User(createdId, "staff_crud", null, "Thủ thư Cập nhật", "ADMIN", "LOCKED");
        Request updateReq = new Request("UPDATE_USER", toUpdate);
        Response updateRes = service.handleRequest(updateReq);
        assertEquals("SUCCESS", updateRes.getStatus());

        // 5. Reset password
        Map<String, Object> resetData = new HashMap<>();
        resetData.put("userId", createdId);
        resetData.put("newPassword", "newpass999");
        Request resetReq = new Request("RESET_PASSWORD", resetData);
        Response resetRes = service.handleRequest(resetReq);
        assertEquals("SUCCESS", resetRes.getStatus());

        // 6. Delete user 1 (root admin) should fail
        Request delAdminReq = new Request("DELETE_USER", 1);
        Response delAdminRes = service.handleRequest(delAdminReq);
        assertEquals("ERROR", delAdminRes.getStatus());

        // 7. Delete created user
        Request delReq = new Request("DELETE_USER", createdId);
        Response delRes = service.handleRequest(delReq);
        assertEquals("SUCCESS", delRes.getStatus());
    }

    @Test
    @DisplayName("Test Live Chat Broadcast and History Lifecycle")
    public void testLiveChatBroadcastAndHistory() {
        // 1. Send valid chat message
        Map<String, Object> chatMsg = new HashMap<>();
        chatMsg.put("sender", "admin");
        chatMsg.put("senderName", "Quản Trị Viên");
        chatMsg.put("senderRole", "ADMIN");
        chatMsg.put("message", "Xin chào toàn thể thủ thư ca trực sáng!");
        chatMsg.put("type", "CHAT");

        Request sendReq = new Request("SEND_CHAT_MESSAGE", chatMsg);
        Response sendRes = service.handleRequest(sendReq);
        assertEquals("SUCCESS", sendRes.getStatus());
        assertNotNull(sendRes.getData());

        // 2. Fetch history
        Request histReq = new Request("GET_CHAT_HISTORY", null);
        Response histRes = service.handleRequest(histReq);
        assertEquals("SUCCESS", histRes.getStatus());
        List<?> messages = (List<?>) histRes.getData();
        assertNotNull(messages);
        assertFalse(messages.isEmpty());

        // 3. Send empty chat message should fail
        Map<String, Object> emptyMsg = new HashMap<>();
        emptyMsg.put("message", "   ");
        Request emptyReq = new Request("SEND_CHAT_MESSAGE", emptyMsg);
        Response emptyRes = service.handleRequest(emptyReq);
        assertEquals("ERROR", emptyRes.getStatus());

        // 4. Clear chat history
        Request clearReq = new Request("CLEAR_CHAT_HISTORY", null);
        Response clearRes = service.handleRequest(clearReq);
        assertEquals("SUCCESS", clearRes.getStatus());
    }

    @Test
    @DisplayName("Test Real-time Reactive Broadcast on Borrow and Return")
    public void testRealtimeBroadcastOnBorrowAndReturn() {
        // Clear history first
        service.handleRequest(new Request("CLEAR_CHAT_HISTORY", null));

        // Borrow a book
        Map<String, Object> borrowData = new HashMap<>();
        borrowData.put("readerId", 1);
        borrowData.put("userId", 1);
        borrowData.put("bookIds", Arrays.asList(101));
        borrowData.put("days", 14);
        Request borrowReq = new Request("BORROW_BOOKS", borrowData);
        Response borrowRes = service.handleRequest(borrowReq);
        assertEquals("SUCCESS", borrowRes.getStatus());

        // Verify broadcast event was generated
        List<com.library.common.model.ChatMessage> history = com.library.server.net.ChatManager.getHistory();
        assertFalse(history.isEmpty());
        com.library.common.model.ChatMessage latestMsg = history.get(history.size() - 1);
        assertEquals("SYNC_BOOKS", latestMsg.getType());
        assertTrue(latestMsg.getMessage().contains("Kho sách đã cập nhật tự động"));

        // Extract borrowingId and return book
        Map<?, ?> borrowingObj = (Map<?, ?>) com.library.common.protocol.NetworkUtils.getMapper().convertValue(borrowRes.getData(), Map.class);
        int borrowingId = Integer.parseInt(borrowingObj.get("borrowingId").toString());
        Map<String, Object> returnData = new HashMap<>();
        returnData.put("borrowingId", borrowingId);
        Request returnReq = new Request("RETURN_BOOKS", returnData);
        Response returnRes = service.handleRequest(returnReq);
        assertEquals("SUCCESS", returnRes.getStatus());

        // Verify return broadcast event
        List<com.library.common.model.ChatMessage> updatedHistory = com.library.server.net.ChatManager.getHistory();
        com.library.common.model.ChatMessage returnMsg = updatedHistory.get(updatedHistory.size() - 1);
        assertEquals("SYNC_BOOKS", returnMsg.getType());
        assertTrue(returnMsg.getMessage().contains("Sách đã trở lại kho"));
    }
}


