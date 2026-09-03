package com.pictet.adventurebook.service;

import com.pictet.adventurebook.model.dto.BookImportDto;
import com.pictet.adventurebook.model.dto.BookImportDto.OptionDto;
import com.pictet.adventurebook.model.dto.BookImportDto.SectionDto;
import com.pictet.adventurebook.model.dto.BookValidationResult;
import com.pictet.adventurebook.model.entity.Book;
import com.pictet.adventurebook.model.entity.Consequence;
import com.pictet.adventurebook.model.entity.Option;
import com.pictet.adventurebook.model.entity.Section;
import com.pictet.adventurebook.repository.BookRepository;
import com.pictet.adventurebook.validation.BookImportValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class BookImportService {

    private final BookImportValidator validator;
    private final BookRepository bookRepository;

    @Transactional
    public Book importBook(BookImportDto dto) {
        BookValidationResult result = validator.validate(dto);

        Book book = new Book(dto.title(), dto.author(), dto.difficulty());
        dto.categories().forEach(book::addCategory);
        dto.sections().forEach(section -> book.addSection(toSection(section)));
        book.recordValidation(result.errors(), result.warnings());

        Book saved = bookRepository.save(book);

        if (!result.valid()) {
            log.warn("Imported '{}' as INVALID: {}", saved.getTitle(), result.errors());
        }

        if (!result.warnings().isEmpty()) {
            log.info("Imported '{}' with warnings: {}", saved.getTitle(), result.warnings());
        }

        return book;
    }

    private Section toSection(SectionDto dto) {
        Section section = new Section(dto.id(), dto.text(), dto.type());
        List<OptionDto> options = dto.options();

        for (OptionDto option : options) {
            section.addOption(toOption(option));
        }

        return section;
    }

    private Option toOption(OptionDto dto) {
        Consequence consequence = dto.consequence() == null
                ? null
                : new Consequence(dto.consequence().type(), dto.consequence().value(), dto.consequence().text());

        return new Option(dto.description(), dto.gotoId(), consequence);
    }
}
