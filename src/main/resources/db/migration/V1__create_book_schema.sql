CREATE TABLE book
(
    id         BIGSERIAL PRIMARY KEY,
    title      VARCHAR(255) NOT NULL,
    author     VARCHAR(255) NOT NULL,
    difficulty VARCHAR(16)  NOT NULL,
    valid      BOOLEAN      NOT NULL,
    version    BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uq_book_title_author UNIQUE (title, author)
);

CREATE INDEX idx_book_difficulty ON book (difficulty);

CREATE TABLE book_category
(
    book_id  BIGINT      NOT NULL REFERENCES book (id) ON DELETE CASCADE,
    category varchar(32) NOT NULL,
    CONSTRAINT uq_book_category UNIQUE (book_id, category)
);

CREATE INDEX idx_book_category_category ON book_category (category);

CREATE TABLE book_validation_error
(
    book_id BIGINT NOT NULL REFERENCES book (id) ON DELETE CASCADE,
    message TEXT   NOT NULL
);

CREATE INDEX idx_book_validation_error_book ON book_validation_error (book_id);

CREATE TABLE book_warning
(
    book_id BIGINT NOT NULL REFERENCES book (id) ON DELETE CASCADE,
    message TEXT   NOT NULL
);

CREATE INDEX idx_book_warning_book ON book_warning (book_id);

CREATE TABLE book_section
(
    id             BIGSERIAL PRIMARY KEY,
    book_id        BIGINT      NOT NULL REFERENCES book (id) ON DELETE CASCADE,
    section_number INT         NOT NULL,
    text           TEXT        NOT NULL,
    type           VARCHAR(16) NOT NULL,
    CONSTRAINT uq_section_book_number UNIQUE (book_id, section_number)
);

CREATE TABLE section_option
(
    id                BIGSERIAL PRIMARY KEY,
    section_id        BIGINT NOT NULL REFERENCES book_section (id) ON DELETE CASCADE,
    ordinal           INT    NOT NULL,
    description       TEXT   NOT NULL,
    goto_number       INT    NOT NULL,
    consequence_type  VARCHAR(16),
    consequence_value INT,
    consequence_text  TEXT,
    CONSTRAINT uq_option_section_ordinal UNIQUE (section_id, ordinal)
);
