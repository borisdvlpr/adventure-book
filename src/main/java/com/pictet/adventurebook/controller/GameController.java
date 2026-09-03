package com.pictet.adventurebook.controller;

import com.pictet.adventurebook.model.dto.request.ChoiceRequest;
import com.pictet.adventurebook.model.dto.request.StartGameRequest;
import com.pictet.adventurebook.model.dto.response.GameStateResponse;
import com.pictet.adventurebook.model.dto.response.GameSummaryResponse;
import com.pictet.adventurebook.model.dto.response.PageResponse;
import com.pictet.adventurebook.model.type.GameStatusType;
import com.pictet.adventurebook.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/games")
@Tag(name = "Games", description = "Play a book: start a session, make choices, resume later")
public class GameController {

    private final GameService gameService;

    @Operation(
            summary = "List a player's games",
            description = "Most recently played first. `playerId` is required — this is \"my games\", "
                    + "not \"all games\", and without authentication there is nothing to derive it from.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "A page of sessions, possibly empty"),
            @ApiResponse(responseCode = "400", description = "playerId is missing, or status is not a recognised value",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    public PageResponse<GameSummaryResponse> listGames(
            @Parameter(description = "Opaque player identifier", required = true, example = "alice")
            @RequestParam String playerId,
            @Parameter(description = "Optional status filter")
            @RequestParam(required = false) GameStatusType status,
            @ParameterObject @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return gameService.listGames(playerId, status, pageable);
    }

    @Operation(
            summary = "Get one game",
            description = "Current health, position and status, together with the section the player is standing on.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The game state"),
            @ApiResponse(responseCode = "404", description = "No game with that id",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public GameStateResponse getGame(
            @Parameter(description = "Identifier of the game session", example = "1") @PathVariable Long id) {

        return gameService.getGame(id);
    }

    @Operation(
            summary = "Start a game",
            description = "The one place the `valid` flag does something: an invalid book is refused "
                    + "with 422 and its validation errors attached. Everywhere else invalid books "
                    + "are listed, searched and read like any other.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "A new session, positioned at the book's BEGIN section"),
            @ApiResponse(responseCode = "400", description = "playerId is blank, or bookId is missing",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "No book with that id",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "The book is not playable; `validationErrors` says why",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/new")
    @ResponseStatus(HttpStatus.CREATED)
    public GameStateResponse startGame(@Valid @RequestBody StartGameRequest request) {
        return gameService.startGame(request);
    }

    @Operation(
            summary = "Make a choice",
            description = "Advances to the chosen option's destination, then applies its consequence. "
                    + "A POST rather than a PUT because it is not idempotent: replaying a choice "
                    + "applies the consequence again, which is what makes loops like `1 → 500 → 1` "
                    + "a death spiral. The response carries the consequence the player just triggered.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The new game state"),
            @ApiResponse(responseCode = "400", description = "ordinal is missing or negative",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "No game with that id, or the option leads nowhere",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Another choice was submitted against this session first",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "The game is already finished, or the ordinal is not an option here",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/{id}/choices")
    public GameStateResponse makeChoice(
            @Parameter(description = "Identifier of the game session", example = "1") @PathVariable Long id,
            @Valid @RequestBody ChoiceRequest request) {

        return gameService.makeChoice(id, request);
    }
}