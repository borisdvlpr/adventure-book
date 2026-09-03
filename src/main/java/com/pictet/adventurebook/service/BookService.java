package com.pictet.adventurebook.service;

import com.pictet.adventurebook.exception.BookAlreadyExistsException;
import com.pictet.adventurebook.exception.BookNotFoundException;
import com.pictet.adventurebook.exception.SectionNotFoundException;
import com.pictet.adventurebook.mapper.BookMapper;
import com.pictet.adventurebook.model.dto.BookImportDto;
import com.pictet.adventurebook.model.dto.request.BookSearchCriteria;
import com.pictet.adventurebook.model.dto.response.BookDetailsResponse;
import com.pictet.adventurebook.model.dto.response.BookSummaryResponse;
import com.pictet.adventurebook.model.dto.response.PageResponse;
import com.pictet.adventurebook.model.dto.response.SectionResponse;
import com.pictet.adventurebook.model.entity.Book;
import com.pictet.adventurebook.model.entity.Section;
import com.pictet.adventurebook.model.type.CategoryType;
import com.pictet.adventurebook.model.type.SectionType;
import com.pictet.adventurebook.repository.BookRepository;
import com.pictet.adventurebook.repository.BookSpecification;
import com.pictet.adventurebook.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookImportService bookImportService;
    private final BookRepository bookRepository;
    private final SectionRepository sectionRepository;
    private final BookMapper bookMapper;

    @Transactional(readOnly = true)
    public PageResponse<BookSummaryResponse> searchBook(BookSearchCriteria criteria, Pageable pageable) {
        Page<Book> page = bookRepository.findAll(BookSpecification.matching(criteria), pageable);

        return PageResponse.from(page, bookMapper::toBookSummaryResponse);
    }

    @Transactional(readOnly = true)
    // @Cacheable(cacheNames = CacheConfig.BOOKS_CACHE, key = "#id")
    public BookDetailsResponse getBook(Long id) {
        Book book = findBook(id);
        return bookMapper.toBookDetailsResponse(book, findBeginSectionNumber(id));
    }

    @Transactional(readOnly = true)
    public SectionResponse readSection(Long bookId, int sectionNumber) {
        if (!bookRepository.existsById(bookId)) {
            throw new BookNotFoundException(bookId);
        }

        Section section = sectionRepository.findByBookIdAndSectionNumber(bookId, sectionNumber)
                .orElseThrow(() -> new SectionNotFoundException(bookId, sectionNumber));

        return bookMapper.toSectionResponse(bookId, section);
    }

    @Transactional
    // @CacheEvict(cacheNames = CacheConfig.BOOKS_CACHE, key = "#id")
    public BookDetailsResponse addBookCategory(Long id, CategoryType category) {
        Book book = findBook(id);
        book.addCategory(category);

        return bookMapper.toBookDetailsResponse(book, findBeginSectionNumber(id));
    }

    @Transactional
    public BookDetailsResponse createNewBook(BookImportDto dto) {
        String title = dto.title();
        String author = dto.author();

        if (bookRepository.existsByTitleAndAuthor(title, author)) {
            throw new BookAlreadyExistsException(title, author);
        }

        Book book = bookImportService.importBook(dto);

        return bookMapper.toBookDetailsResponse(book, findBeginSectionNumber(book.getId()));
    }

    @Transactional
    // @CacheEvict(cacheNames = CacheConfig.BOOKS_CACHE, key = "#id")
    public BookDetailsResponse deleteBookCategory(Long id, CategoryType category) {
        Book book = findBook(id);
        book.removeCategory(category);

        return bookMapper.toBookDetailsResponse(book, findBeginSectionNumber(id));
    }

    private Book findBook(Long id) {
        return bookRepository.findById(id).orElseThrow(() ->
                new BookNotFoundException(id)
        );
    }

    private Integer findBeginSectionNumber(Long bookId) {
        List<Section> begins = sectionRepository.findByBookIdAndType(bookId, SectionType.BEGIN);

        return begins.size() == 1 ? begins.getFirst().getSectionNumber() : null;
    }
}