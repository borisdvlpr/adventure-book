package com.pictet.adventurebook.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StartGameRequest(
        @NotBlank(message = "playerId must not be blank")
        String playerId,

        @NotNull(message = "bookId must not be null")
        Long bookId
) {

}
