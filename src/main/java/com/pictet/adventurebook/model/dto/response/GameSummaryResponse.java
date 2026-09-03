package com.pictet.adventurebook.model.dto.response;

import com.pictet.adventurebook.model.type.GameStatusType;

import java.time.Instant;

public record GameSummaryResponse(
        Long gameId,
        String playerId,
        Long bookId,
        String bookTitle,
        GameStatusType status,
        int health,
        int maxHealth,
        int currentSectionNumber,
        Instant startedAt,
        Instant updatedAt
) {

}
