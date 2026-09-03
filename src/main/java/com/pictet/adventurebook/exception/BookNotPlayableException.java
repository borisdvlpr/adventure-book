package com.pictet.adventurebook.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class BookNotPlayableException extends RuntimeException {

    private final transient List<String> validationErrors;

    public BookNotPlayableException(Long bookId, List<String> validationErrors) {
        super("Book %d cannot be played because it is invalid.".formatted(bookId));
        this.validationErrors = List.copyOf(validationErrors);
    }
}
