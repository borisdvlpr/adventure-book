# Adventure Book API

A REST API for browsing and playing choose-your-own-adventure books, built with:

- REST API with RFC 9457 problem details
- PostgreSQL persistence
- Flyway migrations
- Unit and integration tests

## Getting Started

To run this project locally, you need:

- Java 25
- Docker + Docker Compose (required for running the application and integration tests)
- Maven 3.9 (only if you do not use the bundled `./mvnw` wrapper)

## The Domain

A **book** is a graph of **sections**. Each section has text and a list of **options**; each option points at another
section and may carry a **consequence** that changes the player's health.

A book is **invalid** if any of these hold:

1. It has no beginning, or more than one.
2. It has no ending (several are fine).
3. An option points at a section id that does not exist.
4. A non-ending section has no options.

Invalid books are **stored, listed, searched and readable**. They are refused at exactly one point:
when someone tries to play one. Rejecting at import was considered and rejected — three of the four supplied sample
books fail these rules, so the catalogue would have been empty.

A player starts with **10 health**, which is also the cap. Consequences apply when an option is taken and re-apply if
the same option is taken again. Reaching zero ends the adventure, and death is evaluated before completion: arriving at
an ending with no health left is a death, not a win.

## Architecture (Simplified)

```
HTTP  ──▶  controller  ──▶  service  ──▶  repository  ──▶  PostgreSQL
                │              
                └──▶  mapper  ──▶  DTO
```

## Project Structure & Patterns

The project follows a standard **layered architecture** with a clear separation of concerns, using the **DTO** and
**Specification** patterns.

### Core Packages

- `controller`: REST API endpoints and routing.
- `service`: Business logic — catalogue, reading, playing, and import.
- `validation`: The four book validity rules and the reachability warnings.
- `repository`: Spring Data JPA interfaces, plus the search specification.
- `mapper`: Entity ↔ DTO conversion.
- `model`: Domain models, split into `entity`, `dto` (`request` / `response`), and `type`.
- `config`: Startup book loader.
- `exception`: Global error handling, custom exceptions, and problem type URIs.

### DTO Pattern & Mappers

Internal entities (`model.entity`) are never exposed through the controllers.

- **DTOs (`model.dto`)**: All incoming and outgoing payloads (`BookImportDto`, `StartGameRequest`,
  `GameStateResponse`). Request and response shapes are separated because they genuinely diverge —
  `BookSearchCriteria` normalises blank input, `BookDetailResponse` carries validity information no request supplies.
- **Mappers (`mapper`)**: Convert between entities and DTOs, keeping the API contract decoupled from the database
  schema.

Pagination uses an explicit `PageResponse` record rather than returning Spring Data's `Page`, whose JSON structure
Spring itself warns is unstable.

## REST API

| Method   | Path                                       | Purpose                                                |
|----------|--------------------------------------------|--------------------------------------------------------|
| `GET`    | `/api/v1/books`                            | List and search by title, author, category, difficulty |
| `POST`   | `/api/v1/books/new`                        | Add a book to the collection                           |
| `GET`    | `/api/v1/books/{id}`                       | Book detail, validity, warnings, entry point           |
| `GET`    | `/api/v1/books/{id}/sections/{n}`          | Read one section                                       |
| `PUT`    | `/api/v1/books/{id}/categories/{category}` | Attach a category                                      |
| `DELETE` | `/api/v1/books/{id}/categories/{category}` | Detach a category                                      |
| `POST`   | `/api/v1/games/new`                        | Start a game                                           |
| `GET`    | `/api/v1/games?playerId=`                  | List a player's games                                  |
| `GET`    | `/api/v1/games/{id}`                       | Resume: current state of a game                        |
| `POST`   | `/api/v1/games/{id}/choices`               | Take one of the current section's options              |

**Reading and playing are separate resources.** `/books/{id}/sections/{n}` is a stateless `GET` over the template; every
state change lives behind a `POST` on `/games`. That separation is what stops a state-mutating move from ending up
behind a `GET`.

Ready-to-run request collections are in `api-collections/` (IntelliJ HTTP Client format).

### Errors

All errors are RFC 9457 problem details (`application/problem+json`). No `type` was implemented, but game-state failures
are `422` with a distinct message.

## Databases and Data Usage

### PostgreSQL (Primary Database)

PostgreSQL is the **source of truth**.

It stores:

- `book`, `book_category`, `book_validation_error`, `book_warning`
- `book_section`, `section_option`
- `game_session`

Usage:

- Search uses **JPA Specifications** — four independently optional filters compose into one expression rather than a
  query littered with `(:param IS NULL OR ...)`.
- Schema changes are versioned and applied with **Flyway**. Hibernate runs with
  `ddl-auto=validate`, so a mapping that disagrees with a migration fails at startup rather than at the first query.

Two schema decisions worth knowing:

- **Section ids are book-scoped, not global.** Two supplied books both contain sections `1`, `20`
  and `666`, so the primary key is a surrogate with a unique constraint on
  `(book_id, section_number)`.
- **`section_option.goto_number` is deliberately not a foreign key.** Invalid books are stored rather than refused, and
  one supplied book points at a section that does not exist — a foreign key would make it impossible to persist.
  Referential integrity of the graph is enforced by the validator instead.

## Book Import

One ingestion path, reachable two ways. A startup runner reads `classpath:books/*.json` and feeds the **same** import
service that backs `POST /books/new`, so a submitted book is validated by exactly the same four rules as a seeded one. A
request body is byte-for-byte one of the files in
`src/main/resources/books/`.

A file that cannot be read or parsed is logged and skipped, never fatal — one of the supplied fixtures is a zero-byte
file, and a loader that dies on one bad file is one that fails in production.

## Test Infrastructure

### Unit Tests

Written with **JUnit 5** and **Mockito**, covering components in isolation with no external dependencies.

- `BookImportValidatorTest` — the four validity rules exhaustively, plus proof that reachability lands in warnings and
  never in errors. No mocks needed; the validator takes a DTO and returns a value.
- `GameSessionTest` — the game rules where they live: damage, the healing cap, the floor at zero, and death being
  evaluated before completion.
- `BookServiceTest`, `GameServiceTest` — mocked collaborators, covering the failure branches and verifying that a
  refused operation writes nothing.

### Controller Integration Tests

`BookControllerTest` and `GameControllerTest` use **`@SpringBootTest`** with **MockMvc** and **Testcontainers**, running
the whole application against a real PostgreSQL instance with Flyway building the schema.

**Testcontainers rather than H2**, so migrations run against the same engine in the build as in production and a
Postgres-specific migration cannot pass CI and fail on deploy. The image tag is pinned — `postgres:latest` makes the
build non-reproducible, which is the one thing integration tests exist to rule out.

**The sample books are the test matrix.** Each of the four supplied files fails differently — empty file, options-less
node, dangling reference, mixed-type ids — which is exactly the coverage the validator needs. Since none of them is
playable, `GameControllerTest` imports its own small valid book through the real import path rather than adding a
fabricated file to the seeded catalogue.

> **Note:** Docker must be running locally for the controller integration tests to execute.

## Run Locally

1. Start infrastructure with Docker Compose:
    - `docker compose up -d`

2. Run the application:
    - `./mvnw spring-boot:run`

The application seeds itself from `src/main/resources/books/` on startup.

## Run Tests

- `./mvnw test`

> **Note:** Docker must be running before executing the full test suite, as the controller
> integration tests spin up a PostgreSQL container via Testcontainers.
