package com.pictet.adventurebook.controller;

import com.pictet.adventurebook.TestcontainersConfiguration;
import com.pictet.adventurebook.model.entity.Book;
import com.pictet.adventurebook.repository.BookRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Nested
    @DisplayName("Browsing and searching")
    class Catalogue {

        @Test
        @DisplayName("Lists every book that parsed, invalid ones included and flagged")
        void listsBooks() throws Exception {
            mockMvc.perform(get("/api/v1/books"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.content[*].title", hasItems(
                            "The Crystal Caverns", "Pirates of the Jade Sea", "The Prisoner")))
                    .andExpect(jsonPath("$.content[?(@.title == 'Pirates of the Jade Sea')].valid").value(false))
                    .andExpect(jsonPath("$.content[?(@.title == 'Dragon Quest')]").isEmpty());
        }

        @Test
        @DisplayName("Searches by author")
        void searchesByAuthor() throws Exception {
            mockMvc.perform(get("/api/v1/books").param("author", "blackwood"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].author").value("Marina Blackwood"));
        }

        @Test
        @DisplayName("Searches by difficulty, exactly")
        void searchesByDifficulty() throws Exception {
            mockMvc.perform(get("/api/v1/books").param("difficulty", "HARD"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].difficulty", everyItem(is("HARD"))))
                    .andExpect(jsonPath("$.content[*].title", hasItem("The Prisoner")));
        }

        @Test
        @DisplayName("Filters combine with AND")
        void combinesFilters() throws Exception {
            mockMvc.perform(get("/api/v1/books")
                            .param("title", "caverns")
                            .param("difficulty", "EASY"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.content[0].title").value("The Crystal Caverns"));
        }

        @Test
        @DisplayName("An unrecognised difficulty is a problem detail")
        void rejectsUnknownDifficulty() throws Exception {
            mockMvc.perform(get("/api/v1/books").param("difficulty", "IMPOSSIBLE"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Detail and categories")
    class Detail {

        @Test
        @DisplayName("The detail response carries the reasons a book is invalid")
        void returnsDetail() throws Exception {
            mockMvc.perform(get("/api/v1/books/{id}", idOf("Pirates of the Jade Sea")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.author").value("Marina Blackwood"))
                    .andExpect(jsonPath("$.difficulty").value("MEDIUM"))
                    .andExpect(jsonPath("$.valid").value(false))
                    .andExpect(jsonPath("$.validationErrors.length()").value(2))
                    .andExpect(jsonPath("$.warnings.length()").value(2))
                    .andExpect(jsonPath("$.beginSectionNumber").value(1));
        }

        @Test
        @DisplayName("An unknown book is a 404 problem detail")
        void unknownBookIsNotFound() throws Exception {
            mockMvc.perform(get("/api/v1/books/{id}", 999999))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("An unrecognised category is rejected")
        void rejectsUnknownCategory() throws Exception {
            mockMvc.perform(put("/api/v1/books/{id}/categories/{c}", idOf("The Prisoner"), "NOPE"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Reading")
    class Reading {

        @Test
        @DisplayName("A section comes back with its options in ordinal order")
        void readsSection() throws Exception {
            mockMvc.perform(get("/api/v1/books/{id}/sections/{n}", idOf("The Crystal Caverns"), 1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.options.length()").value(2))
                    .andExpect(jsonPath("$.options[0].ordinal").value(0))
                    .andExpect(jsonPath("$.options[0].gotoSection").value(100))
                    .andExpect(jsonPath("$.options[1].gotoSection").value(20));
        }

        @Test
        @DisplayName("An ending has no options")
        void readsEnding() throws Exception {
            mockMvc.perform(get("/api/v1/books/{id}/sections/{n}", idOf("The Crystal Caverns"), 800))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.type").value("END"))
                    .andExpect(jsonPath("$.options.length()").value(0));
        }

        @Test
        @DisplayName("An invalid book is readable")
        void readsInvalidBook() throws Exception {
            mockMvc.perform(get("/api/v1/books/{id}/sections/{n}", idOf("Pirates of the Jade Sea"), 1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.options[1].gotoSection").value(999));
        }

        @Test
        @DisplayName("Following a dangling option is a section-level 404")
        void danglingOptionIsNotFound() throws Exception {
            mockMvc.perform(get("/api/v1/books/{id}/sections/{n}", idOf("Pirates of the Jade Sea"), 999))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Adding books")
    class Creation {

        @Test
        @DisplayName("A valid book is created")
        void createsBook() throws Exception {
            create(book("A Brand New Adventure"))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Location"))
                    .andExpect(jsonPath("$.valid").value(true))
                    .andExpect(jsonPath("$.beginSectionNumber").value(1))
                    .andExpect(jsonPath("$.categories[0]").value("THRILLER"));

            mockMvc.perform(get("/api/v1/books").param("title", "Brand New"))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("An invalid book is still created")
        void createsInvalidBookFlagged() throws Exception {
            create("""
                    {
                      "title": "A Broken Adventure", "author": "Objective Six", "difficulty": "HARD",
                      "sections": [
                        { "id": 1, "text": "A door that goes nowhere.", "type": "BEGIN",
                          "options": [ { "description": "Walk through", "gotoId": 2 },
                                       { "description": "Vanish", "gotoId": 404 } ] },
                        { "id": 2, "text": "A dead end.", "type": "NODE" },
                        { "id": 3, "text": "The exit.", "type": "END" }
                      ]
                    }
                    """)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.valid").value(false))
                    .andExpect(jsonPath("$.validationErrors.length()").value(2))
                    .andExpect(jsonPath("$.warnings.length()").value(2));
        }

        @Test
        @DisplayName("The same book cannot be added twice")
        void rejectsDuplicate() throws Exception {
            create(book("A Repeated Adventure")).andExpect(status().isCreated());

            create(book("A Repeated Adventure"))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("A malformed body is a 400")
        void rejectsMalformedBody() throws Exception {
            create("""
                    { "author": "Objective Six", "difficulty": "EASY", "sections": [] }
                    """)
                    .andExpect(status().isBadRequest());
        }

        private ResultActions create(String body) throws Exception {
            return mockMvc.perform(post("/api/v1/books/new")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));
        }

        private String book(String title) {
            return """
                    {
                      "title": "%s", "author": "Objective Six", "difficulty": "MEDIUM",
                      "categories": ["THRILLER"],
                      "sections": [
                        { "id": 1, "text": "The road forks.", "type": "BEGIN",
                          "options": [ { "description": "Take the left path", "gotoId": 2 } ] },
                        { "id": 2, "text": "You arrive.", "type": "END" }
                      ]
                    }
                    """.formatted(title);
        }
    }

    private Long idOf(String title) {
        return bookRepository.findAll().stream()
                .filter(book -> title.equals(book.getTitle()))
                .findFirst()
                .map(Book::getId)
                .orElseThrow(() -> new AssertionError("Fixture book not loaded: " + title));
    }
}
