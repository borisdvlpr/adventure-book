package com.pictet.adventurebook.mapper;

import com.pictet.adventurebook.model.dto.response.BookDetailsResponse;
import com.pictet.adventurebook.model.dto.response.BookSummaryResponse;
import com.pictet.adventurebook.model.entity.Book;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.TreeSet;

@Component
public class BookMapper {

    public BookSummaryResponse toBookSummaryResponse(Book book) {
        return new BookSummaryResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getDifficulty(),
                new TreeSet<>(book.getCategories()),
                book.isValid()
        );
    }

    public BookDetailsResponse toBookDetailsResponse(Book book) {
        return new BookDetailsResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getDifficulty(),
                new TreeSet<>(book.getCategories()),
                book.isValid(),
                List.copyOf(book.getValidationErrors()),
                List.copyOf(book.getWarnings())
        );
    }
}
