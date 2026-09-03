package com.pictet.adventurebook.validation;

import com.pictet.adventurebook.model.dto.BookImportDto;
import com.pictet.adventurebook.model.dto.BookImportDto.OptionDto;
import com.pictet.adventurebook.model.dto.BookImportDto.SectionDto;
import com.pictet.adventurebook.model.dto.BookValidationResult;
import com.pictet.adventurebook.model.type.DifficultyType;
import com.pictet.adventurebook.model.type.SectionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookImportValidatorTest {

    private final BookImportValidator validator = new BookImportValidator();

    @Nested
    @DisplayName("Validity rules")
    class Rules {

        @Test
        @DisplayName("A well-formed book is valid")
        void acceptsWellFormedBook() {
            BookValidationResult result = validator.validate(book(
                    section(1, SectionType.BEGIN, option("go", 2)),
                    section(2, SectionType.END)));

            assertTrue(result.valid(), () -> "unexpected errors: " + result.errors());
            assertTrue(result.warnings().isEmpty());
        }

        @Test
        @DisplayName("A book with no beginning is invalid")
        void rejectsNoBeginning() {
            BookValidationResult result = validator.validate(book(
                    section(1, SectionType.NODE, option("go", 2)),
                    section(2, SectionType.END)));

            assertFalse(result.valid());
            assertTrue(result.errors().stream().anyMatch(e -> e.contains("exactly one BEGIN")));
        }

        @Test
        @DisplayName("A book with two beginnings is invalid")
        void rejectsTwoBeginnings() {
            BookValidationResult result = validator.validate(book(
                    section(1, SectionType.BEGIN, option("go", 3)),
                    section(2, SectionType.BEGIN, option("go", 3)),
                    section(3, SectionType.END)));

            assertFalse(result.valid());
            assertTrue(result.errors().stream().anyMatch(e -> e.contains("found 2")));
        }

        @Test
        @DisplayName("A book with no ending is invalid")
        void rejectsNoEnding() {
            BookValidationResult result = validator.validate(book(
                    section(1, SectionType.BEGIN, option("go", 2)),
                    section(2, SectionType.NODE, option("back", 1))));

            assertFalse(result.valid());
            assertTrue(result.errors().stream().anyMatch(e -> e.contains("at least one END")));
        }

        @Test
        @DisplayName("Several endings are fine")
        void acceptsSeveralEndings() {
            assertTrue(validator.validate(book(
                    section(1, SectionType.BEGIN, option("left", 2), option("right", 3)),
                    section(2, SectionType.END),
                    section(3, SectionType.END))).valid());
        }

        @Test
        @DisplayName("An option pointing at a section that does not exist is invalid")
        void rejectsDanglingReference() {
            BookValidationResult result = validator.validate(book(
                    section(1, SectionType.BEGIN, option("go", 999)),
                    section(2, SectionType.END)));

            assertFalse(result.valid());
            assertTrue(result.errors().stream().anyMatch(e -> e.contains("unknown section 999")));
        }

        @Test
        @DisplayName("A non-ending section with no options is invalid")
        void rejectsNonEndingWithoutOptions() {
            BookValidationResult result = validator.validate(book(
                    section(1, SectionType.BEGIN, option("go", 2)),
                    section(2, SectionType.NODE),
                    section(3, SectionType.END)));

            assertFalse(result.valid());
            assertTrue(result.errors().stream().anyMatch(e -> e.contains("Section 2 is not an END")));
        }

        @Test
        @DisplayName("An ending with no options is fine, that is what an ending is")
        void acceptsEndingWithoutOptions() {
            assertTrue(validator.validate(book(
                    section(1, SectionType.BEGIN, option("go", 2)),
                    section(2, SectionType.END))).valid());
        }

        @Test
        @DisplayName("A loop back to the beginning is legal")
        void acceptsCycles() {
            assertTrue(validator.validate(book(
                    section(1, SectionType.BEGIN, option("try the door", 2), option("look", 3)),
                    section(2, SectionType.NODE, option("gather your thoughts", 1)),
                    section(3, SectionType.END))).valid());
        }
    }

    @Nested
    @DisplayName("Reachability is reported")
    class Reachability {

        @Test
        @DisplayName("An unreachable section is a warning, not an error")
        void unreachableSectionIsWarning() {
            BookValidationResult result = validator.validate(book(
                    section(1, SectionType.BEGIN, option("go", 2)),
                    section(2, SectionType.END),
                    section(666, SectionType.NODE, option("nowhere", 2))));

            assertTrue(result.valid(), () -> "unexpected errors: " + result.errors());
            assertEquals(1, result.warnings().size());
            assertTrue(result.warnings().getFirst().contains("666"));
        }

        @Test
        @DisplayName("A book whose ending cannot be reached is still valid by the four rules")
        void unreachableEndingIsWarning() {
            BookValidationResult result = validator.validate(book(
                    section(1, SectionType.BEGIN, option("dead end", 2)),
                    section(2, SectionType.NODE, option("back", 1)),
                    section(3, SectionType.END)));

            assertTrue(result.valid(), () -> "unexpected errors: " + result.errors());
            assertTrue(result.warnings().stream().anyMatch(w -> w.contains("cannot be completed")));
        }
    }

    private static BookImportDto book(SectionDto... sections) {
        return new BookImportDto("Title", "Author", DifficultyType.EASY, null, List.of(sections));
    }

    private static SectionDto section(int id, SectionType type, OptionDto... options) {
        return new SectionDto(id, "text", type, List.of(options));
    }

    private static OptionDto option(String description, int gotoId) {
        return new OptionDto(description, gotoId, null);
    }
}