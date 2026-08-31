package com.pictet.adventurebook.model.entity;

import com.pictet.adventurebook.model.type.ConsequenceType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Consequence {

    @Enumerated(EnumType.STRING)
    @Column(name = "consequence_type", length = 16)
    private ConsequenceType type;

    @Column(name = "consequence_value")
    private Integer value;

    @Column(name = "consequence_text")
    private String text;

    public Consequence(ConsequenceType type, Integer value, String text) {
        this.type = Objects.requireNonNull(type, "consequence type must be not null");
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("consequence value must be a positive number, but was: " + value);
        }

        this.value = value;
        this.text = text;
    }
}
