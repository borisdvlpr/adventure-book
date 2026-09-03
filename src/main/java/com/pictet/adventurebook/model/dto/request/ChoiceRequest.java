package com.pictet.adventurebook.model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ChoiceRequest(
        @NotNull(message = "ordinal must not be null")
        @PositiveOrZero(message = "ordinal must not be negative")
        Integer ordinal
) {

}
