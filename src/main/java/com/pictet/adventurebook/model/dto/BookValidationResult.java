package com.pictet.adventurebook.model.dto;

import java.util.List;

public record BookValidationResult(List<String> errors, List<String> warnings) {

    public BookValidationResult {
        errors = List.copyOf(errors);
        warnings = List.copyOf(warnings);
    }

    public boolean valid() {
        return errors.isEmpty();
    }
}
