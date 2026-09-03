package com.pictet.adventurebook.repository;

import com.pictet.adventurebook.model.entity.GameSession;
import com.pictet.adventurebook.model.type.GameStatusType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, Long> {

    @EntityGraph(attributePaths = "book")
    Page<GameSession> findByPlayerId(String playerId, Pageable pageable);

    @EntityGraph(attributePaths = "book")
    Page<GameSession> findByPlayerIdAndStatus(String playerId, GameStatusType status, Pageable pageable);
}
