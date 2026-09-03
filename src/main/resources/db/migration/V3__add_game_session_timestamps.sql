ALTER TABLE game_session
    ADD COLUMN started_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE;

UPDATE game_session
SET started_at = now(),
    updated_at = now()
WHERE started_at IS NULL;

ALTER TABLE game_session
    ALTER COLUMN started_at SET NOT NULL,
    ALTER COLUMN updated_at SET NOT NULL;

DROP INDEX idx_game_session_player;

CREATE INDEX idx_game_session_player_updated ON game_session (player_id, updated_at DESC);
