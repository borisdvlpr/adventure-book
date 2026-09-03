package com.pictet.adventurebook.service;

import com.pictet.adventurebook.exception.*;
import com.pictet.adventurebook.mapper.GameMapper;
import com.pictet.adventurebook.model.dto.request.ChoiceRequest;
import com.pictet.adventurebook.model.dto.request.StartGameRequest;
import com.pictet.adventurebook.model.dto.response.GameStateResponse;
import com.pictet.adventurebook.model.dto.response.GameSummaryResponse;
import com.pictet.adventurebook.model.dto.response.PageResponse;
import com.pictet.adventurebook.model.entity.Book;
import com.pictet.adventurebook.model.entity.GameSession;
import com.pictet.adventurebook.model.entity.Option;
import com.pictet.adventurebook.model.entity.Section;
import com.pictet.adventurebook.model.type.GameStatusType;
import com.pictet.adventurebook.model.type.SectionType;
import com.pictet.adventurebook.repository.BookRepository;
import com.pictet.adventurebook.repository.GameSessionRepository;
import com.pictet.adventurebook.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameService {

    private final BookRepository bookRepository;
    private final SectionRepository sectionRepository;
    private final GameSessionRepository gameSessionRepository;
    private final GameMapper gameMapper;

    @Transactional
    public GameStateResponse startGame(StartGameRequest request) {
        Long bookId = request.bookId();

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));

        if (!book.isValid()) {
            throw new BookNotPlayableException(bookId, book.getValidationErrors());
        }

        Section begin = findBeginSection(bookId);
        GameSession session = gameSessionRepository.save(
                new GameSession(request.playerId(), book, begin.getSectionNumber())
        );

        return gameMapper.toGameStateResponse(session, begin);
    }

    @Transactional(readOnly = true)
    public PageResponse<GameSummaryResponse> listGames(String playerId, GameStatusType status, Pageable pageable) {
        Page<GameSession> page = status == null
                ? gameSessionRepository.findByPlayerId(playerId, pageable)
                : gameSessionRepository.findByPlayerIdAndStatus(playerId, status, pageable);

        return PageResponse.from(page, gameMapper::toGameSummaryResponse);
    }

    @Transactional(readOnly = true)
    public GameStateResponse getGame(Long gameId) {
        GameSession session = findSession(gameId);
        Section current = findSection(session.getBook().getId(), session.getCurrentSectionNumber());

        return gameMapper.toGameStateResponse(session, current);
    }

    @Transactional
    public GameStateResponse makeChoice(Long gameId, ChoiceRequest request) {
        GameSession session = findSession(gameId);

        if (session.isFinished()) {
            throw new GameAlreadyFinishedException(gameId, session.getStatus());
        }

        Long bookId = session.getBook().getId();
        Section current = findSection(bookId, session.getCurrentSectionNumber());
        Option chosen = findOption(current, request.ordinal());
        Section destination = findSection(bookId, chosen.getGotoNumber());

        int healthBefore = session.getHealth();
        session.applyChoice(chosen, destination);

        return gameMapper.toGameStateResponse(session, destination, chosen.getConsequence(), healthBefore);
    }

    private GameSession findSession(Long gameId) {
        return gameSessionRepository.findById(gameId)
                .orElseThrow(() -> new GameSessionNotFoundException(gameId));
    }

    private Section findSection(Long bookId, int sectionNumber) {
        return sectionRepository.findByBookIdAndSectionNumber(bookId, sectionNumber)
                .orElseThrow(() -> new SectionNotFoundException(bookId, sectionNumber));
    }

    private Section findBeginSection(Long bookId) {
        List<Section> begins = sectionRepository.findByBookIdAndType(bookId, SectionType.BEGIN);

        return begins.stream()
                .findFirst()
                .orElseThrow(() -> new BookNotPlayableException(bookId, List.of("Book has no BEGIN section.")));
    }

    private Option findOption(Section section, Integer ordinal) {
        return section.getOptions().stream()
                .filter(option -> option.getOrdinal() == ordinal)
                .findFirst()
                .orElseThrow(() -> new InvalidChoiceException(
                        ordinal, section.getSectionNumber(), section.getOptions().size()
                ));
    }
}
