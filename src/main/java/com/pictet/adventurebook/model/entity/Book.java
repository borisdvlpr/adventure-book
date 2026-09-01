package com.pictet.adventurebook.model.entity;

import com.pictet.adventurebook.model.type.CategoryType;
import com.pictet.adventurebook.model.type.DifficultyType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "book",
        uniqueConstraints = @UniqueConstraint(name = "uq_book_title_author", columnNames = {"title", "author"})
)
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, updatable = false)
    private String title;

    @Column(name = "author", nullable = false, updatable = false)
    private String author;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, updatable = false, length = 16)
    private DifficultyType difficulty;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 32)
    @CollectionTable(name = "book_category", joinColumns = @JoinColumn(name = "book_id"))
    private Set<CategoryType> categories = new HashSet<>();

    @Column(name = "valid", nullable = false)
    private boolean valid;

    @ElementCollection
    @Column(name = "message", nullable = false, columnDefinition = "text")
    @CollectionTable(name = "book_validation_error", joinColumns = @JoinColumn(name = "book_id"))
    private List<String> validationErrors = new ArrayList<>();

    @ElementCollection
    @Column(name = "message", nullable = false, columnDefinition = "text")
    @CollectionTable(name = "book_warning", joinColumns = @JoinColumn(name = "book_id"))
    private List<String> warnings = new ArrayList<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sectionNumber ASC")
    private List<Section> sections = new ArrayList<>();

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public Book(String title, String author, DifficultyType difficulty) {
        this.title = title;
        this.author = author;
        this.difficulty = difficulty;
    }

    public void addSection(Section section) {
        section.assignTo(this);
        this.sections.add(section);
    }

    public void addCategory(CategoryType category) {
        this.categories.add(category);
    }

    public void removeCategory(CategoryType category) {
        this.categories.remove(category);
    }

    public void recordValidation(List<String> errors, List<String> warnings) {
        this.validationErrors.clear();
        this.warnings.clear();
        this.validationErrors.addAll(errors);
        this.warnings.addAll(warnings);
        this.valid = errors.isEmpty();
    }

    public Set<CategoryType> getCategories() {
        return Collections.unmodifiableSet(categories);
    }

    public List<String> getValidationErrors() {
        return Collections.unmodifiableList(validationErrors);
    }

    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    public List<Section> getSections() {
        return Collections.unmodifiableList(sections);
    }
}
