package com.pictet.adventurebook.mapper;

import com.pictet.adventurebook.model.dto.BookSummaryResponse;
import com.pictet.adventurebook.model.entity.Book;
import org.springframework.stereotype.Component;

import java.util.TreeSet;

@Component
public class BookMapper {

    public BookSummaryResponse toSummary(Book book) {
        return new BookSummaryResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getDifficulty(),
                new TreeSet<>(book.getCategories()),
                book.isValid()
        );
    }
}
