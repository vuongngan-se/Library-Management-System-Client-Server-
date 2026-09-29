package com.library.common.model;

import java.io.Serializable;
import java.util.List;

public class Borrowing implements Serializable {
    private int borrowingId;
    private int readerId;
    private String readerName;
    private int userId;
    private String userName;
    private String borrowDate;
    private String dueDate;
    private String returnDate;
    private String status;
    private double fineAmount;
    private List<BorrowingDetail> details;

    public Borrowing() {}

    public int getBorrowingId() { return borrowingId; }
    public void setBorrowingId(int borrowingId) { this.borrowingId = borrowingId; }

    public int getReaderId() { return readerId; }
    public void setReaderId(int readerId) { this.readerId = readerId; }

    public String getReaderName() { return readerName; }
    public void setReaderName(String readerName) { this.readerName = readerName; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getBorrowDate() { return borrowDate; }
    public void setBorrowDate(String borrowDate) { this.borrowDate = borrowDate; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getReturnDate() { return returnDate; }
    public void setReturnDate(String returnDate) { this.returnDate = returnDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getFineAmount() { return fineAmount; }
    public void setFineAmount(double fineAmount) { this.fineAmount = fineAmount; }

    public List<BorrowingDetail> getDetails() { return details; }
    public void setDetails(List<BorrowingDetail> details) { this.details = details; }
}
