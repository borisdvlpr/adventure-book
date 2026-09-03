package com.pictet.adventurebook.mapper;

import com.pictet.adventurebook.model.dto.response.BookDetailsResponse;
import com.pictet.adventurebook.model.dto.response.BookSummaryResponse;
import com.pictet.adventurebook.model.dto.response.SectionResponse;
import com.pictet.adventurebook.model.entity.Book;
import com.pictet.adventurebook.model.entity.Option;
import com.pictet.adventurebook.model.entity.Section;
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

    public BookDetailsResponse toBookDetailsResponse(Book book, Integer beginSectionNumber) {
        return new BookDetailsResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getDifficulty(),
                new TreeSet<>(book.getCategories()),
                book.isValid(),
                List.copyOf(book.getValidationErrors()),
                List.copyOf(book.getWarnings()),
                beginSectionNumber
        );
    }

    public SectionResponse toSectionResponse(Long bookId, Section section) {
        return new SectionResponse(
                bookId,
                section.getSectionNumber(),
                section.getText(),
                section.getType(),
                section.getOptions().stream().map(this::toOptionResponse).toList()
        );
    }

    private SectionResponse.OptionResponse toOptionResponse(Option option) {
        return new SectionResponse.OptionResponse(
                option.getOrdinal(),
                option.getDescription(),
                option.getGotoNumber()
        );
    }
}
