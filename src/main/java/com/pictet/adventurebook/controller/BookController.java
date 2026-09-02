package com.pictet.adventurebook.controller;

import com.pictet.adventurebook.model.dto.request.BookSearchCriteria;
import com.pictet.adventurebook.model.dto.response.BookDetailsResponse;
import com.pictet.adventurebook.model.dto.response.BookSummaryResponse;
import com.pictet.adventurebook.model.dto.response.PageResponse;
import com.pictet.adventurebook.model.type.CategoryType;
import com.pictet.adventurebook.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/books")
public class BookController {

    private final BookService bookService;

    @GetMapping
    public PageResponse<BookSummaryResponse> getAllBooks(
            BookSearchCriteria criteria,
            @PageableDefault(size = 20, sort = "title", direction = Sort.Direction.ASC) Pageable pageable) {

        return bookService.search(criteria, pageable);
    }

    @GetMapping("/{id}")
    public BookDetailsResponse getBook(@PathVariable Long id) {
        return bookService.getBook(id);
    }

    @PutMapping("/{id}/categories/{category}")
    public BookDetailsResponse addBookCategory(@PathVariable Long id, @PathVariable CategoryType category) {
        return bookService.addBookCategory(id, category);
    }

    @DeleteMapping("/{id}/categories/{category}")
    public BookDetailsResponse deleteBookCategory(@PathVariable Long id, @PathVariable CategoryType category) {
        return bookService.deleteBookCategory(id, category);
    }
}
