package com.pictet.adventurebook.model.dto.response;

import com.pictet.adventurebook.model.type.ConsequenceType;
import com.pictet.adventurebook.model.type.GameStatusType;

public record GameStateResponse(
        Long gameId,
        String playerId,
        Long bookId,
        GameStatusType status,
        int health,
        int maxHealth,
        SectionResponse section,
        ConsequenceResponse consequence) {

    public record ConsequenceResponse(
            ConsequenceType type,
            int value,
            String text,
            int healthBefore,
            int healthAfter) {

    }
}
