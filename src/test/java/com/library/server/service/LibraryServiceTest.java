package com.library.server.service;

import com.library.common.model.Book;
import com.library.common.model.Reader;
import com.library.common.model.User;
import com.library.common.protocol.Request;
import com.library.common.protocol.Response;
import com.library.server.dao.InMemoryStore;
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
}
