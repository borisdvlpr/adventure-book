package com.pictet.adventurebook.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "section_option",
        uniqueConstraints = @UniqueConstraint(name = "uq_option_section_ordinal", columnNames = {"section_id", "ordinal"})
)
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @Column(name = "ordinal", nullable = false)
    private int ordinal;

    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "goto_number", nullable = false)
    private int gotoNumber;

    @Embedded
    private Consequence consequence;

    public Option(String description, int gotoNumber, Consequence consequence) {
        this.description = description;
        this.gotoNumber = gotoNumber;
        this.consequence = consequence;
    }

    void assignTo(Section section, int ordinal) {
        this.section = section;
        this.ordinal = ordinal;
    }
}
