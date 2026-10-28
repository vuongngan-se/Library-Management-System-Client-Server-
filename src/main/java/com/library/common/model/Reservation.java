package com.library.common.model;

import java.io.Serializable;
import java.time.LocalDate;

public class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;

    private int reservationId;
    private int readerId;
    private String readerName;
    private int bookId;
    private String bookTitle;
    private String reserveDate;
    private String status; // PENDING, NOTIFIED, FULFILLED, CANCELLED
    private String notes;

    public Reservation() {
        this.reserveDate = LocalDate.now().toString();
        this.status = "PENDING";
    }

    public Reservation(int reservationId, int readerId, String readerName, int bookId, String bookTitle, String reserveDate, String status, String notes) {
        this.reservationId = reservationId;
        this.readerId = readerId;
        this.readerName = readerName;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.reserveDate = reserveDate != null ? reserveDate : LocalDate.now().toString();
        this.status = status != null ? status : "PENDING";
        this.notes = notes;
    }

    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
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

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getReserveDate() {
        return reserveDate;
    }

    public void setReserveDate(String reserveDate) {
        this.reserveDate = reserveDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "Reservation #" + reservationId + " [Độc giả: " + readerName + ", Sách: " + bookTitle + ", Trạng thái: " + status + "]";
    }
}
