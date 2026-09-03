package com.pictet.adventurebook.exception;

public class GameSessionNotFoundException extends RuntimeException {
    public GameSessionNotFoundException(Long id) {
        super("No game session found with id %d.".formatted(id));
    }
}
