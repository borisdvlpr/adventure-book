CREATE TABLE game_session
(
    id                     BIGSERIAL PRIMARY KEY,
    player_id              VARCHAR(64) NOT NULL,
    book_id                BIGINT      NOT NULL REFERENCES book (id),
    current_section_number INT         NOT NULL,
    health                 INT         NOT NULL,
    status                 VARCHAR(16) NOT NULL,
    version                BIGINT      NOT NULL DEFAULT 0,
    CONSTRAINT ck_game_session_health CHECK ( health >= 0 )
);

CREATE INDEX idx_game_session_player ON game_session (player_id);
