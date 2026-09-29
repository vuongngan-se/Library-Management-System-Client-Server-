package com.library.common.model;

import java.io.Serializable;

public class Book implements Serializable {
    private int bookId;
    private String title;
    private String author;
    private int categoryId;
    private String categoryName;
    private String publisher;
    private int publishYear;
    private int quantity;
    private int availableQuantity;
    private String isbn;

    public Book() {}

    public Book(int bookId, String title, String author, int categoryId, String publisher, int publishYear, int quantity, int availableQuantity, String isbn) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.categoryId = categoryId;
        this.publisher = publisher;
        this.publishYear = publishYear;
        this.quantity = quantity;
        this.availableQuantity = availableQuantity;
        this.isbn = isbn;
    }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public int getPublishYear() { return publishYear; }
    public void setPublishYear(int publishYear) { this.publishYear = publishYear; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
}
