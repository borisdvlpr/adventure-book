package com.pictet.adventurebook.repository;

import com.pictet.adventurebook.model.dto.BookSearchCriteria;
import com.pictet.adventurebook.model.entity.Book;
import com.pictet.adventurebook.model.type.CategoryType;
import com.pictet.adventurebook.model.type.DifficultyType;
import jakarta.persistence.criteria.JoinType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BookSpecification {

    private static final char ESCAPE = '\\';

    public static Specification<Book> matching(BookSearchCriteria criteria) {
        List<Specification<Book>> specs = new ArrayList<>();

        if (criteria.title() != null) {
            specs.add(titleContains(criteria.title()));
        }

        if (criteria.author() != null) {
            specs.add(authorContains(criteria.author()));
        }

        if (criteria.category() != null) {
            specs.add(hasCategory(criteria.category()));
        }

        if (criteria.difficulty() != null) {
            specs.add(hasDifficulty(criteria.difficulty()));
        }

        return specs.isEmpty() ? Specification.unrestricted() : Specification.allOf(specs);
    }

    static Specification<Book> titleContains(String fragment) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("title")), containsPattern(fragment), ESCAPE);
    }

    static Specification<Book> authorContains(String fragment) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("author")), containsPattern(fragment), ESCAPE);
    }

    static Specification<Book> hasDifficulty(DifficultyType difficulty) {
        return (root, query, cb) -> cb.equal(root.get("difficulty"), difficulty);
    }

    static Specification<Book> hasCategory(CategoryType category) {
        return (root, query, cb) ->
                cb.equal(root.join("categories", JoinType.INNER), category);
    }

    private static String containsPattern(String fragment) {
        String escaped = fragment.toLowerCase()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
