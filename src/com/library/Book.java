package com.library;

import java.math.BigDecimal;

public final class Book {
    private final int bookId;
    private final String bookTitle;
    private final String authorName;
    private final String publisherName;
    private final String category;
    private final String edition;
    private final Integer publicationYear;
    private final BigDecimal price;
    private final Integer quantity;

    public Book(int bookId, String bookTitle, String authorName, String publisherName,
                String category, String edition, Integer publicationYear,
                BigDecimal price, Integer quantity) {
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.authorName = authorName;
        this.publisherName = publisherName;
        this.category = category;
        this.edition = edition;
        this.publicationYear = publicationYear;
        this.price = price;
        this.quantity = quantity;
    }

    public int getBookId() { return bookId; }
    public String getBookTitle() { return bookTitle; }
    public String getAuthorName() { return authorName; }
    public String getPublisherName() { return publisherName; }
    public String getCategory() { return category; }
    public String getEdition() { return edition; }
    public Integer getPublicationYear() { return publicationYear; }
    public BigDecimal getPrice() { return price; }
    public Integer getQuantity() { return quantity; }
}
