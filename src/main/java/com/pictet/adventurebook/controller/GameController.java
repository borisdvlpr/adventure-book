package com.pictet.adventurebook.controller;

import com.pictet.adventurebook.model.dto.request.ChoiceRequest;
import com.pictet.adventurebook.model.dto.request.StartGameRequest;
import com.pictet.adventurebook.model.dto.response.GameStateResponse;
import com.pictet.adventurebook.model.dto.response.GameSummaryResponse;
import com.pictet.adventurebook.model.dto.response.PageResponse;
import com.pictet.adventurebook.model.type.GameStatusType;
import com.pictet.adventurebook.service.GameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/games")
public class GameController {

    private final GameService gameService;

    @GetMapping
    public PageResponse<GameSummaryResponse> listGames(
            @RequestParam String playerId,
            @RequestParam(required = false)GameStatusType status,
            @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC)Pageable pageable) {

        return gameService.listGames(playerId, status, pageable);
    }

    @GetMapping("/{id}")
    public GameStateResponse getGame(@PathVariable Long id) {
        return gameService.getGame(id);
    }

    @PostMapping("/new")
    @ResponseStatus(HttpStatus.CREATED)
    public GameStateResponse startGame(@Valid @RequestBody StartGameRequest request) {
        return gameService.startGame(request);
    }

    @PostMapping("/{id}/choices")
    public GameStateResponse makeChoice(@PathVariable Long id, @Valid @RequestBody ChoiceRequest request) {
        return gameService.makeChoice(id, request);
    }
}
