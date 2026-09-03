package com.pictet.adventurebook.exception;

import com.pictet.adventurebook.model.type.GameStatusType;
import lombok.Getter;

@Getter
public class GameAlreadyFinishedException extends RuntimeException {

    private final transient GameStatusType status;

    public GameAlreadyFinishedException(Long gameId, GameStatusType status) {
        super("Game %d is already finished with status %s.".formatted(gameId, status));
        this.status = status;
    }
}
