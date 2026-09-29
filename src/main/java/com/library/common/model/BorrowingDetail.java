package com.library.common.model;

import java.io.Serializable;

public class BorrowingDetail implements Serializable {
    private int detailId;
    private int borrowingId;
    private int bookId;
    private String bookTitle;
    private int quantity;
    private int returnedQuantity;
    private String status;

    public BorrowingDetail() {}

    public int getDetailId() { return detailId; }
    public void setDetailId(int detailId) { this.detailId = detailId; }

    public int getBorrowingId() { return borrowingId; }
    public void setBorrowingId(int borrowingId) { this.borrowingId = borrowingId; }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getReturnedQuantity() { return returnedQuantity; }
    public void setReturnedQuantity(int returnedQuantity) { this.returnedQuantity = returnedQuantity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
