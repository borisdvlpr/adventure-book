package com.pictet.adventurebook.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pictet.adventurebook.model.type.CategoryType;
import com.pictet.adventurebook.model.type.ConsequenceType;
import com.pictet.adventurebook.model.type.DifficultyType;
import com.pictet.adventurebook.model.type.SectionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BookImportDto(

        @NotBlank(message = "Title must not be blank")
        String title,

        @NotBlank(message = "Author must not be blank")
        String author,

        @NotNull(message = "Difficulty must be one of EASY, MEDIUM, HARD")
        DifficultyType difficulty,

        Set<CategoryType> categories,

        @NotEmpty(message = "A book must have at least one section")
        List<SectionDto> sections) {

    public BookImportDto {
        categories = categories == null ? Set.of() : Set.copyOf(categories);
        sections = sections == null ? List.of() : List.copyOf(sections);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SectionDto(
            int id,

            @NotBlank(message = "Section text must not be blank")
            String text,

            @NotNull(message = "Section type must be one of BEGIN, NODE, END")
            SectionType type,

            List<OptionDto> options) {

        public SectionDto {
            options = options == null ? List.of() : List.copyOf(options);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OptionDto(

            @NotBlank(message = "Option description must not be blank")
            String description,

            int gotoId,

            @Valid
            ConsequenceDto consequence) {

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ConsequenceDto(
            @NotNull(message = "Consequence type must be on of GAIN_HEALTH, LOSE_HEALTH")
            ConsequenceType type,

            @NotNull(message = "Consequence value must not be null")
            @Positive(message = "Consequence value must be a positive number")
            Integer value,

            String text) {

    }
}
