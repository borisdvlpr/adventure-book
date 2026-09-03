package com.pictet.adventurebook.exception;

import lombok.Getter;

@Getter
public class InvalidChoiceException extends RuntimeException {

    private final transient Integer ordinal;
    private final transient int availableOptions;

    public InvalidChoiceException(Integer ordinal, int sectionNumber, int availableOptions) {
        super("Section %d has no option %s. Available ordinals: %s."
                .formatted(sectionNumber, ordinal, ordinalRange(availableOptions)));

        this.ordinal = ordinal;
        this.availableOptions = availableOptions;
    }

    private static String ordinalRange(int availableOptions) {
        return availableOptions == 0 ? "none" : "0-" + (availableOptions - 1);
    }
}
