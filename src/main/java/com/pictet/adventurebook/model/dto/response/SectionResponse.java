package com.pictet.adventurebook.model.dto.response;

import com.pictet.adventurebook.model.type.SectionType;

import java.util.List;

public record SectionResponse(
        Long bookId,
        int sectionNumber,
        String text,
        SectionType type,
        List<OptionResponse> options) {

    public record OptionResponse(
            int ordinal,
            String description,
            int gotoSection) {

    }
}
