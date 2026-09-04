package com.pictet.adventurebook.service;

import com.pictet.adventurebook.exception.BookNotPlayableException;
import com.pictet.adventurebook.exception.GameAlreadyFinishedException;
import com.pictet.adventurebook.exception.GameSessionNotFoundException;
import com.pictet.adventurebook.exception.InvalidChoiceException;
import com.pictet.adventurebook.mapper.GameMapper;
import com.pictet.adventurebook.model.dto.request.ChoiceRequest;
import com.pictet.adventurebook.model.dto.request.StartGameRequest;
import com.pictet.adventurebook.model.entity.*;
import com.pictet.adventurebook.model.type.ConsequenceType;
import com.pictet.adventurebook.model.type.DifficultyType;
import com.pictet.adventurebook.model.type.GameStatusType;
import com.pictet.adventurebook.model.type.SectionType;
import com.pictet.adventurebook.repository.BookRepository;
import com.pictet.adventurebook.repository.GameSessionRepository;
import com.pictet.adventurebook.repository.SectionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private GameSessionRepository gameSessionRepository;

    @Mock
    private GameMapper gameMapper;

    @InjectMocks
    private GameService gameService;

    @Test
    @DisplayName("An invalid book is refused")
    void startGameRefusesInvalidBook() {
        Book invalid = new Book("Broken", "Author", DifficultyType.HARD);
        invalid.recordValidation(List.of("Section 2 is not an END but has no options."), List.of());
        when(bookRepository.findById(1L)).thenReturn(Optional.of(invalid));

        BookNotPlayableException thrown = assertThrows(BookNotPlayableException.class,
                () -> gameService.startGame(new StartGameRequest("anna", 1L)));

        assertEquals(1, thrown.getValidationErrors().size());
        verify(gameSessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("An unknown game raises GameSessionNotFoundException")
    void getGameThrowsWhenMissing() {
        when(gameSessionRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(GameSessionNotFoundException.class, () -> gameService.getGame(7L));
    }

    @Test
    @DisplayName("A finished game refuses further choices")
    void makeChoiceRefusesFinishedGame() {
        GameSession dead = new GameSession("anna", new Book("B", "A", DifficultyType.EASY), 1);
        dead.applyChoice(
                new Option("fall", 2, new Consequence(ConsequenceType.LOSE_HEALTH, 10, "You fall.")),
                new Section(2, "The bottom.", SectionType.NODE));
        when(gameSessionRepository.findById(1L)).thenReturn(Optional.of(dead));

        GameAlreadyFinishedException thrown = assertThrows(GameAlreadyFinishedException.class,
                () -> gameService.makeChoice(1L, new ChoiceRequest(0)));

        assertEquals(GameStatusType.DEAD, thrown.getStatus());
        verify(sectionRepository, never()).findByBookIdAndSectionNumber(any(), anyInt());
    }

    @Test
    @DisplayName("An ordinal the section does not offer is refused with the count that does exist")
    void makeChoiceRefusesUnknownOrdinal() {
        GameSession session = new GameSession("anna", new Book("B", "A", DifficultyType.EASY), 1);
        Section current = new Section(1, "A fork.", SectionType.BEGIN);
        current.addOption(new Option("only choice", 2, null));

        when(gameSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(sectionRepository.findByBookIdAndSectionNumber(any(), anyInt()))
                .thenReturn(Optional.of(current));

        InvalidChoiceException thrown = assertThrows(InvalidChoiceException.class,
                () -> gameService.makeChoice(1L, new ChoiceRequest(5)));

        assertEquals(5, thrown.getOrdinal());
        assertEquals(1, thrown.getAvailableOptions());
    }

    @Test
    @DisplayName("A valid choice advances the session and applies its consequence")
    void makeChoiceAdvancesTheSession() {
        GameSession session = new GameSession("anna", new Book("B", "A", DifficultyType.EASY), 1);
        Section current = new Section(1, "A fork.", SectionType.BEGIN);
        current.addOption(new Option("fight", 2,
                new Consequence(ConsequenceType.LOSE_HEALTH, 4, "The boar gores you.")));
        Section destination = new Section(2, "A clearing.", SectionType.NODE);

        when(gameSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(sectionRepository.findByBookIdAndSectionNumber(any(), anyInt()))
                .thenReturn(Optional.of(current), Optional.of(destination));

        gameService.makeChoice(1L, new ChoiceRequest(0));

        assertEquals(6, session.getHealth());
        assertEquals(2, session.getCurrentSectionNumber());
        assertEquals(GameStatusType.IN_PROGRESS, session.getStatus());
        verify(gameMapper).toGameStateResponse(any(), any(), any(), anyInt());
    }
}