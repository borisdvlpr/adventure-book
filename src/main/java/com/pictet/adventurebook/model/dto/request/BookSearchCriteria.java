
package com.pictet.adventurebook.model.dto.request;

import com.pictet.adventurebook.model.type.CategoryType;
import com.pictet.adventurebook.model.type.DifficultyType;

public record BookSearchCriteria(
        String title,
        String author,
        CategoryType category,
        DifficultyType difficulty) {

    public BookSearchCriteria {
        title = blankToNull(title);
        author = blankToNull(author);
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
