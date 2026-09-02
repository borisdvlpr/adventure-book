package com.pictet.adventurebook.service;

import com.pictet.adventurebook.mapper.BookMapper;
import com.pictet.adventurebook.model.dto.request.BookSearchCriteria;
import com.pictet.adventurebook.model.dto.response.BookSummaryResponse;
import com.pictet.adventurebook.model.dto.response.PageResponse;
import com.pictet.adventurebook.model.entity.Book;
import com.pictet.adventurebook.repository.BookRepository;

import com.pictet.adventurebook.repository.BookSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Transactional(readOnly = true)
    public PageResponse<BookSummaryResponse> search(BookSearchCriteria criteria, Pageable pageable) {
        Page<Book> page = bookRepository.findAll(BookSpecification.matching(criteria), pageable);

        return PageResponse.from(page, bookMapper::toBookSummaryResponse);
    }
}