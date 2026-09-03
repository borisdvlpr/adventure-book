package com.pictet.adventurebook.controller;

import com.jayway.jsonpath.JsonPath;
import com.pictet.adventurebook.TestcontainersConfiguration;
import com.pictet.adventurebook.model.dto.BookImportDto;
import com.pictet.adventurebook.model.dto.BookImportDto.ConsequenceDto;
import com.pictet.adventurebook.model.dto.BookImportDto.OptionDto;
import com.pictet.adventurebook.model.dto.BookImportDto.SectionDto;
import com.pictet.adventurebook.model.entity.Book;
import com.pictet.adventurebook.model.type.ConsequenceType;
import com.pictet.adventurebook.model.type.DifficultyType;
import com.pictet.adventurebook.model.type.SectionType;
import com.pictet.adventurebook.repository.BookRepository;
import com.pictet.adventurebook.service.BookImportService;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class GameControllerTest {

    private static final String TITLE = "The Fork in the Road";
    private static final String AUTHOR = "Integration Test";

    private static final int WALK_ON = 0;
    private static final int FIGHT_THE_BOAR = 1;
    private static final int FALL_DOWN = 2;
    private static final int REST = 0;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookImportService importService;

    @BeforeEach
    void importPlayableBook() {
        if (!bookRepository.existsByTitleAndAuthor(TITLE, AUTHOR)) {
            importService.importBook(playableBook());
        }
    }

    @Nested
    @DisplayName("objective 4: playing")
    class Playing {

        @Test
        @DisplayName("a new game starts at the beginning on full health")
        void startsAtBeginning() throws Exception {
            start("tester")
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                    .andExpect(jsonPath("$.health").value(10))
                    .andExpect(jsonPath("$.maxHealth").value(10))
                    .andExpect(jsonPath("$.section.type").value("BEGIN"))
                    .andExpect(jsonPath("$.consequence").isEmpty());
        }

        @Test
        @DisplayName("an invalid book cannot be played, and the response says why")
        void refusesInvalidBook() throws Exception {
            mockMvc.perform(post("/api/v1/games/new")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"playerId\":\"tester\",\"bookId\":%d}"
                                    .formatted(idOf("Pirates of the Jade Sea"))))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.validationErrors.length()").value(2));
        }

        @Test
        @DisplayName("a consequence is applied and revealed only after the choice is made")
        void appliesConsequence() throws Exception {
            long game = startGame("tester");

            choose(game, FIGHT_THE_BOAR)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.health").value(6))
                    .andExpect(jsonPath("$.section.sectionNumber").value(2))
                    .andExpect(jsonPath("$.consequence.type").value("LOSE_HEALTH"))
                    .andExpect(jsonPath("$.consequence.healthBefore").value(10))
                    .andExpect(jsonPath("$.consequence.healthAfter").value(6));
        }

        @Test
        @DisplayName("healing cannot push a player above the starting maximum")
        void capsHealth() throws Exception {
            long game = startGame("tester");

            choose(game, FIGHT_THE_BOAR).andExpect(jsonPath("$.health").value(6));

            choose(game, REST)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.health").value(10));
        }

        @Test
        @DisplayName("health reaching zero ends the adventure, and it stays ended")
        void deathEndsTheGame() throws Exception {
            long game = startGame("tester");

            choose(game, FALL_DOWN)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.health").value(0))
                    .andExpect(jsonPath("$.status").value("DEAD"));

            choose(game, REST)
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("reaching an ending completes the game and closes it to further choices")
        void completionEndsTheGame() throws Exception {
            long game = startGame("tester");

            choose(game, WALK_ON).andExpect(jsonPath("$.status").value("IN_PROGRESS"));

            choose(game, REST)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.section.type").value("END"));

            choose(game, REST)
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("an option the section does not offer is rejected with the valid range")
        void rejectsUnknownOrdinal() throws Exception {
            long game = startGame("tester");

            choose(game, 99)
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.availableOptions").value(3));
        }

        @Test
        @DisplayName("an unknown game is a 404 problem detail")
        void unknownGameIsNotFound() throws Exception {
            mockMvc.perform(get("/api/v1/games/{id}", 999999))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("a game cannot be started without a player")
        void requiresPlayerId() throws Exception {
            mockMvc.perform(post("/api/v1/games/new")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"bookId\":%d}".formatted(idOf(TITLE))))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("objective 5: players and saved progress")
    class Players {

        @Test
        @DisplayName("two players on the same book keep entirely separate progress")
        void progressIsIndependent() throws Exception {
            long anna = startGame("anna");
            long bruno = startGame("bruno");

            choose(anna, FIGHT_THE_BOAR).andExpect(status().isOk());

            mockMvc.perform(get("/api/v1/games/{id}", anna))
                    .andExpect(jsonPath("$.health").value(6))
                    .andExpect(jsonPath("$.section.sectionNumber").value(2));

            mockMvc.perform(get("/api/v1/games/{id}", bruno))
                    .andExpect(jsonPath("$.health").value(10))
                    .andExpect(jsonPath("$.section.sectionNumber").value(1));
        }

        @Test
        @DisplayName("re-reading a game shows saved progress without replaying the consequence")
        void resumesWithoutReplaying() throws Exception {
            long game = startGame("carla");
            choose(game, FIGHT_THE_BOAR).andExpect(status().isOk());

            mockMvc.perform(get("/api/v1/games/{id}", game))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.health").value(6))
                    .andExpect(jsonPath("$.consequence").isEmpty());
        }

        @Test
        @DisplayName("a player sees only their own games, with enough to rebuild a resume screen")
        void listsOnlyOwnGames() throws Exception {
            startGame("diogo");
            startGame("diogo");
            startGame("elena");

            mockMvc.perform(get("/api/v1/games").param("playerId", "diogo"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.content[0].bookTitle").value(TITLE))
                    .andExpect(jsonPath("$.content[0].currentSectionNumber").value(1))
                    .andExpect(jsonPath("$.content[0].updatedAt").isNotEmpty());

            mockMvc.perform(get("/api/v1/games").param("playerId", "elena"))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("the list can be narrowed to games still in progress")
        void filtersByStatus() throws Exception {
            long finished = startGame("fabio");
            startGame("fabio");
            choose(finished, FALL_DOWN).andExpect(jsonPath("$.status").value("DEAD"));

            mockMvc.perform(get("/api/v1/games")
                            .param("playerId", "fabio")
                            .param("status", "IN_PROGRESS"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(1));

            mockMvc.perform(get("/api/v1/games")
                            .param("playerId", "fabio")
                            .param("status", "DEAD"))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.content[0].gameId").value((int) finished));
        }

        @Test
        @DisplayName("a player with no games gets an empty page, not an error")
        void unknownPlayerHasNoGames() throws Exception {
            mockMvc.perform(get("/api/v1/games").param("playerId", "nobody-at-all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        @DisplayName("listing without a player is rejected: this is 'my games', not 'all games'")
        void playerIdIsRequired() throws Exception {
            mockMvc.perform(get("/api/v1/games"))
                    .andExpect(status().isBadRequest());
        }
    }

    private ResultActions start(String playerId) throws Exception {
        return mockMvc.perform(post("/api/v1/games/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"playerId\":\"%s\",\"bookId\":%d}".formatted(playerId, idOf(TITLE))));
    }

    private long startGame(String playerId) throws Exception {
        String body = start(playerId)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return ((Number) JsonPath.read(body, "$.gameId")).longValue();
    }

    private ResultActions choose(long gameId, int ordinal) throws Exception {
        return mockMvc.perform(post("/api/v1/games/{id}/choices", gameId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ordinal\":%d}".formatted(ordinal)));
    }

    private Long idOf(String title) {
        return bookRepository.findAll().stream()
                .filter(book -> title.equals(book.getTitle()))
                .findFirst()
                .map(Book::getId)
                .orElseThrow(() -> new AssertionError("Book not loaded: " + title));
    }

    private static BookImportDto playableBook() {
        return new BookImportDto(TITLE, AUTHOR, DifficultyType.EASY, null, List.of(
                new SectionDto(1, "You stand at a fork in the road.", SectionType.BEGIN, List.of(
                        new OptionDto("Walk on", 2, null),
                        new OptionDto("Fight the boar", 2,
                                new ConsequenceDto(ConsequenceType.LOSE_HEALTH, 4, "The boar gores your leg.")),
                        new OptionDto("Climb down the ravine", 2,
                                new ConsequenceDto(ConsequenceType.LOSE_HEALTH, 10, "You do not get up again."))
                )),
                new SectionDto(2, "A quiet clearing, with the embers of a fire.", SectionType.NODE, List.of(
                        new OptionDto("Rest by the fire", 3,
                                new ConsequenceDto(ConsequenceType.GAIN_HEALTH, 5, "The warmth revives you."))
                )),
                new SectionDto(3, "You reach the far side of the valley.", SectionType.END, null)
        ));
    }
}