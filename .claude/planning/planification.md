# Implementation plan – Pokédex API (Java / Spring Boot)

> Backlog of tasks and subtasks to implement the technical exercise.
> Execution order: inside out (domain → use cases → adapters → frontend → delivery).
> Each code subtask follows TDD: **failing test → minimal implementation → refactor**, with one commit per step
> (`test: …`, `feat: …`, `refactor: …`) so the history shows it.

## Conventions

- **Status**: `[ ]` pending · `[x]` done
- **ID**: `T<phase>.<task>` and `T<phase>.<task>.<subtask>`
- **Global DoD (Definition of Done)**: compiles without warnings, tests green, coverage not below the threshold, code formatted, no loose TODOs, README updated where applicable.
- **Branches**: `main` protected; one branch per task (`feat/T2.3-list-pokemon`) with a PR and optional squash (or direct commits on `main` if you prefer simplicity; see open questions).

---

## Phase 0 – Project foundations
### T0.1 Repository and base structure
- [ ] T0.1.1 Create public GitHub repository (`pokedex-clean-architecture`)
- [ ] T0.1.2 Monorepo structure: `backend/`, `frontend/`, `task-api/`, `docs/`, `docker-compose.yml`
- [ ] T0.1.3 `.gitignore`, `.editorconfig`, `.env.example`, MIT license
- [ ] T0.1.4 Skeleton README with empty sections (completed in Phase 6)

### T0.2 Spring Boot project
- [ ] T0.2.1 Generate Java 21 + Spring Boot 3.x project (Maven wrapper)
  - Dependencies: web, validation, data-jpa, security, cache, actuator, postgresql, flyway, springdoc-openapi, mapstruct, caffeine, jjwt
  - Test: junit5, mockito, assertj, spring-security-test, wiremock, testcontainers, archunit
- [ ] T0.2.2 Base packages: `domain`, `application.port.in`, `application.port.out`, `application.usecase`, `infrastructure.adapter.in.web`, `infrastructure.adapter.out.pokeapi`, `infrastructure.adapter.out.persistence`, `infrastructure.security`, `infrastructure.config`
- [ ] T0.2.3 Profiles `application.yml` / `application-local.yml` / `application-docker.yml` / `application-test.yml`
- [ ] T0.2.4 JaCoCo with thresholds (80% global, 85% in `domain` + `application`) that breaks the build
- [ ] T0.2.5 Format plugin (Spotless with google-java-format) and basic Checkstyle

### T0.3 Local database
- [ ] T0.3.1 `docker-compose.yml` with `db` service (postgres:16, volume, healthcheck)
- [ ] T0.3.2 Configure Flyway and datasource per profile
- [ ] T0.3.3 Empty migration `V1__baseline.sql` to validate the connection

### T0.4 Architecture guards
- [ ] T0.4.1 ArchUnit test: `domain` does not depend on `application` or `infrastructure`
- [ ] T0.4.2 ArchUnit test: `domain` and `application` do not import `org.springframework..`, `jakarta.persistence..`, `com.fasterxml..`
- [ ] T0.4.3 ArchUnit test: controllers only depend on input ports (not on repositories or adapters)

### T0.5 Continuous integration
- [ ] T0.5.1 GitHub Actions: `mvn verify` (tests + JaCoCo) on every push/PR
- [ ] T0.5.2 Frontend job (lint + test + build) — activated in Phase 5
- [ ] T0.5.3 CI and coverage badge in the README

**Phase 0 exit criterion:** `./mvnw verify` green in CI, ArchUnit green, Postgres starts with compose.

---

## Phase 1 – Domain
### T1.1 Domain models (immutable records)
- [ ] T1.1.1 `PokemonSummary` (id, name, spriteUrl, category, weight, abilities) — US01
- [ ] T1.1.2 `PokemonDetail` (id, name, imageUrl, height, weight, types, stats, description, evolutionChain) — US02
- [ ] T1.1.3 `Stat` (name, baseValue), `Ability` (name, hidden), `PokemonType` (name, slot)
- [ ] T1.1.4 `EvolutionStage` (speciesName, pokeApiId, stage, minLevel/trigger) + `EvolutionChain`
- [ ] T1.1.5 `LocalPokemon` (id, pokeApiId, base data + own fields: `localName`, `region`, `tags`, `version`, timestamps) — US03/US04
- [ ] T1.1.6 `User` (id, username, email, passwordHash, role) and enum `Role` (USER, ADMIN)
- [ ] T1.1.7 `PageRequest` / `PageResult<T>` owned by the domain (no Spring Data dependency)

### T1.2 Domain rules and validations (TDD)
- [ ] T1.2.1 Pagination validation: `page ≥ 0`, `1 ≤ size ≤ 50`
- [ ] T1.2.2 Own field validation: `localName` 1–100, not only spaces; `region` ≤ 100; `tags` max. 10, no duplicates, each ≤ 30
- [ ] T1.2.3 Normalization: trim texts, tags in lowercase
- [ ] T1.2.4 `pokeApiId` immutable after creation
- [ ] T1.2.5 Weight and height > 0 when provided
- [ ] T1.2.6 Flattening of the evolution tree into an ordered list, supporting branches (Eevee case) — test with fixture

### T1.3 Domain exceptions
- [ ] T1.3.1 `PokemonNotFoundException`, `UserNotFoundException`
- [ ] T1.3.2 `InvalidPokemonDataException` (with a list of field/message violations)
- [ ] T1.3.3 `InvalidPaginationException`, `DuplicateResourceException`
- [ ] T1.3.4 `ExternalServiceException` (PokeAPI down / timeout)
- [ ] T1.3.5 `InvalidCredentialsException`, `ConcurrentModificationException`

**Phase 1 exit criterion:** 100% of domain rules covered by pure unit tests (no Spring).

---

## Phase 2 – Use cases and PokeAPI adapter (US01, US02)
### T2.1 Ports
- [ ] T2.1.1 Input ports: `ListPokemonUseCase`, `GetPokemonDetailUseCase`
- [ ] T2.1.2 Output port: `PokemonCatalogPort` (`findPage`, `findDetail`, `findSpecies`, `findEvolutionChain`)

### T2.2 Use case US01 – Listing (TDD with Mockito)
- [ ] T2.2.1 Test: returns a page with sprite, category, weight and abilities
- [ ] T2.2.2 Test: invalid pagination → `InvalidPaginationException`
- [ ] T2.2.3 Test: page out of range → empty page with correct metadata
- [ ] T2.2.4 Implementation `ListPokemonService`

### T2.3 Use case US02 – Detail (TDD)
- [ ] T2.3.1 Test: combines detail + species + evolution chain
- [ ] T2.3.2 Test: lookup by id or by name (normalized to lowercase)
- [ ] T2.3.3 Test: unknown Pokémon → `PokemonNotFoundException`
- [ ] T2.3.4 Test: description language selection (es → en → empty)
- [ ] T2.3.5 Implementation `GetPokemonDetailService`

### T2.4 PokeAPI adapter
- [ ] T2.4.1 Save real JSON fixtures: `/pokemon?offset=0&limit=20`, `/pokemon/1`, `/pokemon/133`, `/pokemon-species/1`, `/evolution-chain/1`, `/evolution-chain/67`
- [ ] T2.4.2 PokeAPI DTOs (only the fields used, `@JsonIgnoreProperties(ignoreUnknown = true)`)
- [ ] T2.4.3 `RestClient` client with configurable base URL and timeouts (connect 2 s, read 5 s)
- [ ] T2.4.4 PokeAPI → domain mapper (MapStruct + logic for official artwork, English genus, flavour text cleaned of `\n` / `\f`)
- [ ] T2.4.5 Parallel loading of each item's detail in a page (virtual threads / `CompletableFuture`)
- [ ] T2.4.6 Error translation: 404 → `PokemonNotFoundException`; 5xx / timeout → `ExternalServiceException`
- [ ] T2.4.7 Tests with WireMock: success, 404, 500, timeout, response with missing fields

### T2.5 Cache
- [ ] T2.5.1 `CacheConfig` with Caffeine: caches `pokemonPage`, `pokemonDetail`, `species`, `evolutionChain`
- [ ] T2.5.2 TTL and maximum size configurable through properties
- [ ] T2.5.3 `@Cacheable` in the adapter (never in the domain)
- [ ] T2.5.4 Test: a second call does not hit WireMock (check the number of requests)

### T2.6 Catalogue controller
- [ ] T2.6.1 Response DTOs (`PokemonSummaryResponse`, `PokemonDetailResponse`, `PageResponse<T>`)
- [ ] T2.6.2 `GET /api/v1/pokeapi/pokemon?page&size` — `@WebMvcTest` test (200, 400)
- [ ] T2.6.3 `GET /api/v1/pokeapi/pokemon/{idOrName}` — test (200, 404, 502)
- [ ] T2.6.4 OpenAPI annotations (descriptions, examples, response codes)

**Phase 2 exit criterion:** US01 and US02 working in Swagger against the real PokeAPI; tests green.

---

## Phase 3 – Persistence, synchronization and local CRUD (US03, US04)
### T3.1 Schema and entities
- [ ] T3.1.1 `V2__create_pokemon.sql`: table `pokemon` (unique `pokeapi_id`, `version`, timestamps) + `pokemon_ability`, `pokemon_type`, `pokemon_tag`
- [ ] T3.1.2 Indexes: `pokeapi_id`, `lower(name)`
- [ ] T3.1.3 JPA entities (`PokemonEntity` + collections) with `@Version`
- [ ] T3.1.4 Spring Data repository with paginated search by name
- [ ] T3.1.5 Entity ↔ domain mapper
- [ ] T3.1.6 `PokemonPersistenceAdapter` implementing `PokemonRepositoryPort`
- [ ] T3.1.7 `@DataJpaTest` + Testcontainers tests: save, find, uniqueness, optimistic locking, controlled N+1 (`@EntityGraph`)

### T3.2 US03 – Synchronization (TDD)
- [ ] T3.2.1 Port `SyncPokemonUseCase`
- [ ] T3.2.2 Test: sync a new Pokémon → stored, returns `created = true`
- [ ] T3.2.3 Test: existing Pokémon → updates base data **without overwriting own fields**, `created = false`
- [ ] T3.2.4 Test: Pokémon missing in PokeAPI → `PokemonNotFoundException`
- [ ] T3.2.5 Implementation `SyncPokemonService` (transactional in the adapter / config)
- [ ] T3.2.6 Bulk sync by range (`from`, `to`, max. 151 per call) with summary `{synced, updated, failed[]}`
- [ ] T3.2.7 Endpoints: `POST /api/v1/pokemon/sync/{idOrName}` (201/200) and `POST /api/v1/pokemon/sync?from&to` (ADMIN)

### T3.3 Local CRUD + US04 (TDD)
- [ ] T3.3.1 Ports: `GetLocalPokemonUseCase`, `ListLocalPokemonUseCase`, `CreateLocalPokemonUseCase`, `UpdateLocalPokemonUseCase`, `DeleteLocalPokemonUseCase`
- [ ] T3.3.2 Test + implementation: paginated list with filter by name / region / tag
- [ ] T3.3.3 Test + implementation: get by id (404 if it does not exist)
- [ ] T3.3.4 Test + implementation: manual create (409 if `pokeApiId` is duplicated)
- [ ] T3.3.5 Test + implementation: **PUT** full replacement of editable fields (404, 400, 409 version)
- [ ] T3.3.6 Test + implementation: **PATCH** partial update (distinguish absent field from explicit `null`)
- [ ] T3.3.7 Test + implementation: DELETE (204, 404)
- [ ] T3.3.8 Request DTOs with Bean Validation (`@NotBlank`, `@Size`, `@Positive`, `@Pattern`)
- [ ] T3.3.9 Reject unknown properties and empty body
- [ ] T3.3.10 Controller `/api/v1/pokemon` with `Location` on 201 and ETag/version in the response
- [ ] T3.3.11 `@WebMvcTest` tests covering all status codes (200, 201, 204, 400, 404, 409)

**Phase 3 exit criterion:** idempotent sync and full CRUD with validations; persistence tests against real Postgres.

---

## Phase 4 – Users, security and error handling
### T4.1 Users
- [ ] T4.1.1 `V3__create_users.sql` (unique username/email)
- [ ] T4.1.2 Entity, repository and adapter `UserRepositoryPort`
- [ ] T4.1.3 Ports `PasswordHasherPort` (BCrypt adapter) and `TokenProviderPort` (JJWT adapter)
- [ ] T4.1.4 `AuthUseCase` – registration (TDD): success, duplicate username/email → 409, weak password → 400
- [ ] T4.1.5 `AuthUseCase` – login (TDD): success → token; invalid credentials → 401 without revealing which field failed
- [ ] T4.1.6 Endpoints `POST /auth/register`, `POST /auth/login`, `GET /auth/me`

### T4.2 Spring Security
- [ ] T4.2.1 `JwtAuthenticationFilter` (Bearer, signature and expiry validation)
- [ ] T4.2.2 Stateless `SecurityConfig`: public routes (auth, GET catalogue, GET local, Swagger, health) and protected routes (the rest)
- [ ] T4.2.3 Roles: DELETE and bulk sync ADMIN only (`@PreAuthorize`)
- [ ] T4.2.4 `AuthenticationEntryPoint` (401) and `AccessDeniedHandler` (403) in ProblemDetail format
- [ ] T4.2.5 CORS for the frontend origin (configurable)
- [ ] T4.2.6 Secret and expiry from environment variables; startup fails if the secret is missing in the docker profile
- [ ] T4.2.7 Tests: no token → 401, expired token → 401, USER on DELETE → 403, ADMIN → 204

### T4.3 Global error handling
- [ ] T4.3.1 `GlobalExceptionHandler` with `ProblemDetail` (RFC 7807) for all domain exceptions
- [ ] T4.3.2 Validation errors with `errors[]` (field, message, rejected value)
- [ ] T4.3.3 Malformed JSON, wrong type in path/query, method not allowed (405), media type (415)
- [ ] T4.3.4 500 fallback without stack trace + log with correlation id (MDC filter + `X-Correlation-Id` header)
- [ ] T4.3.5 One test per exception → HTTP status mapping

### T4.4 Seed data
- [ ] T4.4.1 `V4__seed_users.sql`: `admin / Admin123!` (ADMIN), `demo / Demo1234!` (USER) with BCrypt hash
- [ ] T4.4.2 `V5__seed_pokemon.sql`: Pokémon #1–#20 preloaded, some with `localName`, `region` and `tags`
- [ ] T4.4.3 `@SpringBootTest` end-to-end integration test: login → sync → update → get → delete

**Phase 4 exit criterion:** complete backend, Swagger with JWT Authorize, coverage ≥ threshold.

---

## Phase 5 – React frontend
### T5.1 Setup
- [ ] T5.1.1 Vite + React 18 + TypeScript, ESLint + Prettier, route aliases
- [ ] T5.1.2 Tailwind CSS, React Router, TanStack Query, React Hook Form + Zod, axios
- [ ] T5.1.3 Vitest + React Testing Library + MSW (handlers for all endpoints)
- [ ] T5.1.4 `VITE_API_URL` variable; dev proxy

### T5.2 Application infrastructure
- [ ] T5.2.1 HTTP client with JWT interceptor and 401 handling (logout + redirect)
- [ ] T5.2.2 TypeScript types for the API responses (and for `ProblemDetail`)
- [ ] T5.2.3 `AuthContext` (token in memory + `sessionStorage`), `ProtectedRoute`, `AdminOnly`
- [ ] T5.2.4 Responsive layout (navbar, mobile menu), components `Pagination`, `Spinner`, `ErrorState`, `EmptyState`, `Toast`

### T5.3 Screens
- [ ] T5.3.1 Login and registration (client validation + server errors per field)
- [ ] T5.3.2 Pokédex (US01): card grid, pagination, skeletons, error/empty states
- [ ] T5.3.3 Detail (US02): image, stat bars, types, description, navigable evolution chain, "Sync" button (US03)
- [ ] T5.3.4 My Pokémon: local list with search, filters, pagination, edit/delete actions (delete ADMIN only, with confirmation)
- [ ] T5.3.5 Edit (US04): own fields form, handling 400/404/409
- [ ] T5.3.6 Frontend 404 page

### T5.4 Quality
- [ ] T5.4.1 Tests for key components (card, pagination, edit form, protected routes)
- [ ] T5.4.2 Basic accessibility: labels, `alt`, visible focus, contrast
- [ ] T5.4.3 Zero console warnings (keys, StrictMode, duplicated effects)
- [ ] T5.4.4 Responsive check at 360 px, 768 px and 1280 px

**Phase 5 exit criterion:** all stories operable from the UI, no warnings, tests green.

---

## Phase 6 – GenAI, containers and documentation
### T6.1 GenAI exercise (Task API)
- [ ] T6.1.1 Write the final prompt in `docs/GENAI.md`
- [ ] T6.1.2 Generate the `task-api/` scaffold and keep the original output unedited (`docs/genai/raw-output/`)
- [ ] T6.1.3 Critical review: compile, run tests, look for IDOR, `dueDate` validation, state transitions, PATCH null vs absent, N+1, obsolete APIs
- [ ] T6.1.4 Apply fixes in separate commits and document before/after
- [ ] T6.1.5 Add missing tests (user B cannot access tasks of user A, etc.)
- [ ] T6.1.6 "How I used AI in the main project" section and where I did not trust it

### T6.2 Docker
- [ ] T6.2.1 `backend/Dockerfile` multi-stage (Maven → JRE alpine), non-root user, `HEALTHCHECK`
- [ ] T6.2.2 `frontend/Dockerfile` multi-stage (Node → Nginx) with `try_files` and `/api` proxy
- [ ] T6.2.3 Final `docker-compose.yml`: `db`, `api` (depends_on healthy), `web`; `.env.example`
- [ ] T6.2.4 Run `docker compose down -v && docker compose up --build` from scratch

### T6.3 Documentation
- [ ] T6.3.1 README: description, screenshots/GIF, architecture (diagram), stories → endpoint + screen
- [ ] T6.3.2 README: run with Docker and locally, demo credentials, URLs, environment variables
- [ ] T6.3.3 README: tests and coverage, technical decisions and trade-offs, future improvements
- [ ] T6.3.4 Postman collection / `.http` file with all example requests
- [ ] T6.3.5 Short ADRs in `docs/adr/` (architecture, JWT, cache, local replica)

---

## Phase 7 – Closing and presentation

- [ ] T7.1 Final code review: names, duplication, long methods, compiler warnings
- [ ] T7.2 Coverage review and missing edge cases
- [ ] T7.3 Check the requirements checklist against the brief
- [ ] T7.4 Presentation script (architecture → demo → tests → GenAI → trade-offs) in `docs/PRESENTATION.md`
- [ ] T7.5 List of probable panel questions with prepared answers
- [ ] T7.6 Full rehearsal of the demo from scratch

---

## Requirements → tasks traceability

| Brief requirement | Tasks |
| --- | --- |
| US01 Paginated listing + cache | T1.1.1, T1.2.1, T2.2, T2.4, T2.5, T2.6.2, T5.3.2 |
| US02 Detailed view | T1.1.2–T1.1.4, T1.2.6, T2.3, T2.4, T2.6.3, T5.3.3 |
| US03 Sync to relational database | T3.1, T3.2, T5.3.3 |
| US04 Local edit with 404/400 | T1.2.2–T1.2.5, T3.3.5–T3.3.11, T4.3, T5.3.5 |
| Full CRUD | T3.3, T5.3.4 |
| Users, authentication, protected routes | T4.1, T4.2, T5.2.3, T5.3.1 |
| Dedicated data layer | T3.1 |
| Independent business logic | T0.4, T1, T2.1–T2.3, T3.2–T3.3 |
| Unit tests for the whole core | All TDD subtasks + T0.2.4 |
| Error handling | T1.3, T4.3 |
| Responsive frontend with CRUD | T5 |
| Seed data / demo credentials | T4.4 |
| Dockerfile | T6.2 |
| README | T0.1.4, T6.3 |
| GenAI section | T6.1 |
| Public repository | T0.1.1 |

---

## Open questions (to confirm before starting)

1. **Repository**: do you create it in your GitHub account and give me access, or do I work locally and you push? (I need write access to push.)
2. **Delivery deadline**: how many days do you have? This decides whether the "extras" (CI, ADRs, PATCH, bulk sync, multi-module) are included.
3. **Build tool**: Maven or Gradle? (Assumed Maven.)
4. **Frontend**: is React fine, or does the company use Vue/Angular?
5. **Language**: code, commits and README in English (assumed) and the presentation in Spanish?
6. **Own fields (US03)**: do `localName`, `region` and `tags` work for you, or do you prefer others?
7. **Edit scope (US04)**: only own fields editable, or also replicated name/weight/height?
8. **Public vs protected reads**: I assume public GETs and writes with JWT. Correct?
9. **GenAI tool to cite**: does the section mention Claude Code / Claude, or another tool you use?
10. **Git flow**: branches + PR per task, or direct commits on `main`?
