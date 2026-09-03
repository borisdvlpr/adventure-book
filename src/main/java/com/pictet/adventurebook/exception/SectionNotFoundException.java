package com.pictet.adventurebook.exception;

public class SectionNotFoundException extends RuntimeException {
    public SectionNotFoundException(Long bookId, int sectionNumber) {
        super("Book %d has no section %d.".formatted(bookId, sectionNumber));
    }
}
