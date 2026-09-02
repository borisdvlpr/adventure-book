package com.pictet.adventurebook.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pictet.adventurebook.model.type.CategoryType;
import com.pictet.adventurebook.model.type.ConsequenceType;
import com.pictet.adventurebook.model.type.DifficultyType;
import com.pictet.adventurebook.model.type.SectionType;

import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BookImportDto(
        String title,
        String author,
        DifficultyType difficulty,
        Set<CategoryType> categories,
        List<SectionDto> sections) {

    public BookImportDto {
        categories = categories == null ? Set.of() : Set.copyOf(categories);
        sections = sections == null ? List.of() : List.copyOf(sections);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SectionDto(int id, String text, SectionType type, List<OptionDto> options) {

        public SectionDto {
            options = options == null ? List.of() : List.copyOf(options);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OptionDto(String description, int gotoId, ConsequenceDto consequence) {

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ConsequenceDto(ConsequenceType type, Integer value, String text) {

    }
}
