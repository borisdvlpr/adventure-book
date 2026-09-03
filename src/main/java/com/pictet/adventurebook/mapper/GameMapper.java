package com.pictet.adventurebook.mapper;

import com.pictet.adventurebook.model.dto.response.GameStateResponse;
import com.pictet.adventurebook.model.dto.response.GameStateResponse.ConsequenceResponse;
import com.pictet.adventurebook.model.entity.Consequence;
import com.pictet.adventurebook.model.entity.GameSession;
import com.pictet.adventurebook.model.entity.Section;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GameMapper {

    private final BookMapper bookMapper;

    public GameStateResponse toGameStateResponse(GameSession session, Section section) {
        return toGameStateResponse(session, section, null, session.getHealth());
    }

    public GameStateResponse toGameStateResponse(GameSession session, Section section, Consequence consequence, int healthBefore) {
        Long bookId = session.getBook().getId();

        return new GameStateResponse(
                section.getId(),
                session.getPlayerId(),
                bookId,
                session.getStatus(),
                session.getHealth(),
                GameSession.MAX_HEALTH,
                bookMapper.toSectionResponse(bookId, section),
                toConsequenceResponse(consequence, healthBefore, session.getHealth())
        );
    }

    private ConsequenceResponse toConsequenceResponse(Consequence consequence, int healthBefore, int healthAfter) {
        if (consequence == null) {
            return null;
        }

        return new  ConsequenceResponse(
                consequence.getType(),
                consequence.getValue(),
                consequence.getText(),
                healthBefore,
                healthAfter
        );
    }
}
