package com.pictet.adventurebook.exception;

public class BookAlreadyExistsException extends RuntimeException {
    public BookAlreadyExistsException(String title, String author) {
        super(String.format("A book titled '%s' by '%s' is already in the catalogue.", title, author));
    }
}
