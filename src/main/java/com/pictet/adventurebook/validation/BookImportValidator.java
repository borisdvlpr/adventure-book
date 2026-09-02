package com.pictet.adventurebook.validation;

import com.pictet.adventurebook.model.dto.BookImportDto;
import com.pictet.adventurebook.model.dto.BookImportDto.SectionDto;
import com.pictet.adventurebook.model.dto.BookValidationResult;
import com.pictet.adventurebook.model.type.SectionType;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class BookImportValidator {

    public BookValidationResult validate(BookImportDto book) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        List<SectionDto> sections = book.sections();
        if (sections.isEmpty()) {
            errors.add("Book has no sections.");
            return new BookValidationResult(errors, warnings);
        }

        Set<Long> ids = sections.stream().map(SectionDto::id).collect(Collectors.toSet());
        if (ids.size() != sections.size()) {
            warnings.add("Book contains duplicate section ids.");
        }

        // rule 1 - exactly one beginning.
        List<Long> begins = sections.stream()
                .filter(s -> s.type() == SectionType.BEGIN)
                .map(SectionDto::id)
                .toList();

        if (begins.size() != 1) {
            errors.add("Book must have exactly one BEGIN section, found %d.".formatted(begins.size()));
        }

        // rule 2 - at least one ending
        List<Long> ends = sections.stream()
                .filter(s -> s.type() == SectionType.END)
                .map(SectionDto::id)
                .toList();

        if (ends.isEmpty()) {
            errors.add("Book must have at least one END section.");
        }

        for (SectionDto section : sections) {
            // rule 3 - every option points at a section that exists
            section.options().stream()
                    .map(BookImportDto.OptionDto::gotoId)
                    .filter(goToId -> !ids.contains(goToId))
                    .forEach(goToId -> errors.add(
                            "Section %d has an option pointing at unknown section %d."
                                    .formatted(section.id(), goToId)));

            // rule 4 - every non-ending section offers at least one option
            if (section.type() != SectionType.END && section.options().isEmpty()) {
                errors.add("Section %d is not an END but has no options.".formatted(section.id()));
            }
        }

        if (begins.size() == 1) {
            addReachabilityWarnings(sections, ids, begins.getFirst(), ends, warnings);
        }

        return new BookValidationResult(errors, warnings);
    }

    private void addReachabilityWarnings(List<SectionDto> sections, Set<Long> ids, long begin,
                                         List<Long> ends, List<String> warnings) {
        Set<Long> reached = traverseFrom(sections, ids, begin);
        List<Long> unreachable = ids.stream().filter(id -> !reached.contains(id)).sorted().toList();

        if (!unreachable.isEmpty()) {
            warnings.add("Sections unreachable from the beginning: %s.".formatted(unreachable));
        }

        if (!ends.isEmpty() && ends.stream().noneMatch(reached::contains)) {
            warnings.add("No END section is reachable from the beginning; the book cannot be completed.");
        }
    }

    private Set<Long> traverseFrom(List<SectionDto> sections, Set<Long> ids, long begin) {
        Map<Long, List<BookImportDto.OptionDto>> optionsById = sections.stream()
                .collect(Collectors.toMap(SectionDto::id, SectionDto::options, (a, b) -> a));
        Set<Long> reached = new HashSet<>();
        Deque<Long> queue = new ArrayDeque<>();
        queue.push(begin);

        while (!queue.isEmpty()) {
            Long current = queue.pop();

            if (!reached.add(current)) {
                continue;
            }

            optionsById.getOrDefault(current, List.of()).stream()
                    .map(BookImportDto.OptionDto::gotoId)
                    .filter(ids::contains)
                    .forEach(queue::push);
        }

        return reached;
    }
}
