package com.pictet.adventurebook.controller;

import com.pictet.adventurebook.model.dto.BookImportDto;
import com.pictet.adventurebook.model.dto.request.BookSearchCriteria;
import com.pictet.adventurebook.model.dto.response.BookDetailsResponse;
import com.pictet.adventurebook.model.dto.response.BookSummaryResponse;
import com.pictet.adventurebook.model.dto.response.PageResponse;
import com.pictet.adventurebook.model.type.CategoryType;
import com.pictet.adventurebook.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/books")
public class BookController {

    private final BookService bookService;

    @GetMapping
    public PageResponse<BookSummaryResponse> getAllBooks(
            BookSearchCriteria criteria,
            @PageableDefault(size = 20, sort = "title", direction = Sort.Direction.ASC) Pageable pageable) {

        return bookService.searchBook(criteria, pageable);
    }

    @GetMapping("/{id}")
    // @Cacheable(cacheNames = CacheConfig.BOOKS_CACHE, key = "#id")
    public BookDetailsResponse getBook(@PathVariable Long id) {
        return bookService.getBook(id);
    }

    @PostMapping("/new")
    public ResponseEntity<BookDetailsResponse> createBook(@Valid @RequestBody BookImportDto request) {
        BookDetailsResponse newBook = bookService.createNewBook(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newBook.id())
                .toUri();

        return ResponseEntity.created(location).body(newBook);
    }

    @PutMapping("/{id}/categories/{category}")
    // @CacheEvict(cacheNames = CacheConfig.BOOKS_CACHE, key = "#id")
    public BookDetailsResponse addBookCategory(@PathVariable Long id, @PathVariable CategoryType category) {
        return bookService.addBookCategory(id, category);
    }

    @DeleteMapping("/{id}/categories/{category}")
    // @CacheEvict(cacheNames = CacheConfig.BOOKS_CACHE, key = "#id")
    public BookDetailsResponse deleteBookCategory(@PathVariable Long id, @PathVariable CategoryType category) {
        return bookService.deleteBookCategory(id, category);
    }
}
