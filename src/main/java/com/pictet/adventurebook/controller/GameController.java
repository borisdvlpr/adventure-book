package com.pictet.adventurebook.controller;

import com.pictet.adventurebook.model.dto.request.ChoiceRequest;
import com.pictet.adventurebook.model.dto.request.StartGameRequest;
import com.pictet.adventurebook.model.dto.response.GameStateResponse;
import com.pictet.adventurebook.service.GameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/games")
public class GameController {

    private final GameService gameService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GameStateResponse startGame(@Valid @RequestBody StartGameRequest request) {
        return gameService.startGame(request);
    }

    @GetMapping("/{id}")
    public GameStateResponse getGame(@PathVariable Long id) {
        return gameService.getGame(id);
    }

    @PostMapping("/{id}/choices")
    public GameStateResponse makeChoice(@PathVariable Long id, @Valid @RequestBody ChoiceRequest request) {
        return gameService.makeChoice(id, request);
    }
}
