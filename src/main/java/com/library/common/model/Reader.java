package com.library.common.model;

import java.io.Serializable;

public class Reader implements Serializable {
    private int readerId;
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private String cardStatus;
    private String createdAt;

    public Reader() {}

    public Reader(int readerId, String fullName, String phone, String email, String address, String cardStatus) {
        this.readerId = readerId;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.cardStatus = cardStatus;
    }

    public int getReaderId() { return readerId; }
    public void setReaderId(int readerId) { this.readerId = readerId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCardStatus() { return cardStatus; }
    public void setCardStatus(String cardStatus) { this.cardStatus = cardStatus; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
