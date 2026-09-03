package com.pictet.adventurebook.model.dto.response;

import com.pictet.adventurebook.model.type.CategoryType;
import com.pictet.adventurebook.model.type.DifficultyType;

import java.util.List;
import java.util.Set;

public record BookDetailsResponse(
        Long id,
        String title,
        String author,
        DifficultyType difficulty,
        Set<CategoryType> categories,
        boolean valid,
        List<String> validationErrors,
        List<String> warnings,
        Integer beginSectionNumber
) {

}