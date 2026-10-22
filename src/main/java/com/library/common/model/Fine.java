package com.library.common.model;

import java.io.Serializable;

public class Fine implements Serializable {
    private int fineId;
    private int borrowingId;
    private int readerId;
    private String readerName;
    private double amount;
    private String status;
    private String reason;
    private String createdAt;

    public Fine() {}

    public Fine(int fineId, int borrowingId, double amount, String status, String reason, String createdAt) {
        this.fineId = fineId;
        this.borrowingId = borrowingId;
        this.amount = amount;
        this.status = status;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public int getFineId() {
        return fineId;
    }

    public void setFineId(int fineId) {
        this.fineId = fineId;
    }

    public int getBorrowingId() {
        return borrowingId;
    }

    public void setBorrowingId(int borrowingId) {
        this.borrowingId = borrowingId;
    }

    public int getReaderId() {
        return readerId;
    }

    public void setReaderId(int readerId) {
        this.readerId = readerId;
    }

    public String getReaderName() {
        return readerName;
    }

    public void setReaderName(String readerName) {
        this.readerName = readerName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
