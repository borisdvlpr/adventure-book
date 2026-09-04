package com.pictet.adventurebook.controller;

import com.pictet.adventurebook.model.dto.BookImportDto;
import com.pictet.adventurebook.model.dto.request.BookSearchCriteria;
import com.pictet.adventurebook.model.dto.response.BookDetailsResponse;
import com.pictet.adventurebook.model.dto.response.BookSummaryResponse;
import com.pictet.adventurebook.model.dto.response.PageResponse;
import com.pictet.adventurebook.model.dto.response.SectionResponse;
import com.pictet.adventurebook.model.type.CategoryType;
import com.pictet.adventurebook.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/books")
@Tag(name = "Books", description = "Browse the catalogue, read sections, and manage categories")
public class BookController {

    private final BookService bookService;

    @Operation(
            summary = "Search the catalogue",
            description = "Returns a page of books. All four filters are optional and combine with AND. "
                    + "Invalid books are listed like any other, flagged with `valid: false`.")
    @ApiResponse(responseCode = "200", description = "A page of books, possibly empty")
    @GetMapping
    public PageResponse<BookSummaryResponse> getAllBooks(
            @ParameterObject BookSearchCriteria criteria,
            @ParameterObject @PageableDefault(size = 20, sort = "title", direction = Sort.Direction.ASC) Pageable pageable) {

        return bookService.searchBook(criteria, pageable);
    }

    @Operation(
            summary = "Get one book",
            description = "Full detail, including the reasons a book is invalid and any structural warnings.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The book"),
            @ApiResponse(responseCode = "404", description = "No book with that id",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public BookDetailsResponse getBook(
            @Parameter(description = "Identifier of the book", example = "1") @PathVariable Long id) {

        return bookService.getBook(id);
    }

    @Operation(
            summary = "Add a book",
            description = "Accepts the same document shape the startup loader reads from disk. "
                    + "A structurally well-formed but incoherent adventure is still created, "
                    + "returned with `valid: false` and its reasons attached — validity is a "
                    + "property of the book, not of the request.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created, with a Location header"),
            @ApiResponse(responseCode = "400", description = "The document is malformed",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "A book with this title and author already exists",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/new")
    public ResponseEntity<BookDetailsResponse> createBook(@Valid @RequestBody BookImportDto request) {
        BookDetailsResponse newBook = bookService.createNewBook(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newBook.id())
                .toUri();

        return ResponseEntity.created(location).body(newBook);
    }

    @Operation(
            summary = "Read a section",
            description = "Pure reading, with no game state. An option's consequence is never "
                    + "exposed here — a reader sees where a choice leads, not what it costs. "
                    + "Invalid books are readable, which is how a broken graph gets diagnosed.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The section and its options, in ordinal order"),
            @ApiResponse(responseCode = "404", description = "No such book, or no such section within it",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{bookId}/sections/{sectionNumber}")
    public SectionResponse getSection(
            @Parameter(description = "Identifier of the book", example = "1") @PathVariable Long bookId,
            @Parameter(description = "Section number as printed in the book", example = "1") @PathVariable int sectionNumber) {

        return bookService.readSection(bookId, sectionNumber);
    }

    @Operation(
            summary = "Attach a category",
            description = "Idempotent: attaching a category a book already has changes nothing and still returns 200.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The updated book"),
            @ApiResponse(responseCode = "400", description = "Not a recognised category",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "No book with that id",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}/categories/{category}")
    public BookDetailsResponse addBookCategory(
            @Parameter(description = "Identifier of the book", example = "1") @PathVariable Long id,
            @Parameter(description = "Category to attach") @PathVariable CategoryType category) {

        return bookService.addBookCategory(id, category);
    }

    @Operation(
            summary = "Detach a category",
            description = "Idempotent: detaching a category a book does not have changes nothing and still returns 200.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The updated book"),
            @ApiResponse(responseCode = "400", description = "Not a recognised category",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "No book with that id",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/{id}/categories/{category}")
    public BookDetailsResponse deleteBookCategory(
            @Parameter(description = "Identifier of the book", example = "1") @PathVariable Long id,
            @Parameter(description = "Category to detach") @PathVariable CategoryType category) {

        return bookService.deleteBookCategory(id, category);
    }
}