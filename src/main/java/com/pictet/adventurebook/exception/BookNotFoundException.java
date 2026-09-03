package com.pictet.adventurebook.exception;

public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(Long bookId) {
        super(String.format("No book found with id '%d'.", bookId));
    }
}
