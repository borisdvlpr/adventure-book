package com.pictet.adventurebook.model.dto;

import com.pictet.adventurebook.model.type.CategoryType;
import com.pictet.adventurebook.model.type.DifficultyType;

import java.util.Set;

public record BookSummaryResponse(
        Long id,
        String title,
        String author,
        DifficultyType difficulty,
        Set<CategoryType> categories,
        boolean valid) {

}