package com.pictet.adventurebook.model.entity;

import com.pictet.adventurebook.model.type.GameStatusType;
import com.pictet.adventurebook.model.type.SectionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "game_session")
public class GameSession {

    public static final int MAX_HEALTH = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player_id", nullable = false, updatable = false, length = 64)
    private String playerId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false, updatable = false)
    private Book book;

    @Column(name = "current_section_number", nullable = false)
    private int currentSectionNumber;

    @Column(name = "health", nullable = false)
    private int health;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private GameStatusType status;

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public GameSession(String playerId, Book book, int beginSectionNumber) {
        this.playerId = playerId;
        this.book = book;
        this.currentSectionNumber = beginSectionNumber;
        this.health = MAX_HEALTH;
        this.status = GameStatusType.IN_PROGRESS;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.startedAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public void applyChoice(Option option, Section destination) {
        this.currentSectionNumber = destination.getSectionNumber();

        if (option.getConsequence() != null) {
            applyConsequence(option.getConsequence());
        }

        if (this.health <= 0) {
            this.status = GameStatusType.DEAD;
        } else if (destination.getType() == SectionType.END) {
            this.status = GameStatusType.COMPLETED;
        }
    }

    public boolean isFinished() {
        return this.status != GameStatusType.IN_PROGRESS;
    }

    private void applyConsequence(Consequence consequence) {
        this.health = switch (consequence.getType()) {
            case LOSE_HEALTH -> Math.max(0, this.health - consequence.getValue());
            case GAIN_HEALTH -> Math.min(MAX_HEALTH, this.health + consequence.getValue());
        };
    }
}