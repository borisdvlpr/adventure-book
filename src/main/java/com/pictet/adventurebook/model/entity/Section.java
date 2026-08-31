package com.pictet.adventurebook.model.entity;

import com.pictet.adventurebook.model.type.SectionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "book_section",
        uniqueConstraints = @UniqueConstraint(name = "uq_section_book_number", columnNames = {"book_id", "section_number"})
)
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "section_number", nullable = false)
    private int sectionNumber;

    @Column(name = "text", nullable = false, columnDefinition = "text")
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 16)
    private SectionType type;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordinal ASC")
    private List<Option> options = new ArrayList<>();

    public Section(int sectionNumber, String text, SectionType type) {
        this.sectionNumber = sectionNumber;
        this.text = text;
        this.type = type;
    }

    public void addOption(Option option) {
        option.assignTo(this, this.options.size());
        this.options.add(option);
    }

    void assignTo(Book book) {
        this.book = book;
    }

    public List<Option> getOptions() {
        return Collections.unmodifiableList(options);
    }
}
