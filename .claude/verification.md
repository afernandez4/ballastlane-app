# Verification record

Each entry says what was run and what it showed. Items marked **not verified** were not checked.

## Backend
- Unit tests: domain rules, use cases with Mockito, mappers.
- `@WebMvcTest`: controllers, status codes (200, 201, 204, 400, 401, 403, 404, 405, 409, 415, 502), ProblemDetail bodies, CORS preflight.
- WireMock with real PokeAPI fixtures captured in `src/test/resources/fixtures/pokeapi`: success, 404, 500, timeout, missing fields.
- Testcontainers (Postgres 16): repository behaviour, Flyway migrations with `ddl-auto: validate`, seed loading.
- ArchUnit: domain does not depend on application or infrastructure; domain and application do not use Spring, JPA or Jackson; controllers only use input ports.
- End-to-end (`EndToEndFlowTest`): register, log in, sync, edit with `If-Match` (409 on stale version), public read, DELETE forbidden for USER (403), DELETE allowed for ADMIN (204).
- Latest run: **158 tests, all passing**.
- Coverage gate (JaCoCo: 80% overall, 85% for domain and application): configured in `verify`. The last full `verify` run was not recorded in this file; **not verified** against the thresholds.

## Frontend
- Vitest with Testing Library and MSW: API client and 401 handling, token decoding, route guards, pagination, card, edit form (client validation and 400/409 handling), login.
- Latest run: **23 tests, all passing**, no console warnings in the test log.
- `npm run lint`, `npm run format:check`, `npm run build`: passing.
- `npm audit`: **0 vulnerabilities** after the vitest 4 and tinypool changes.
- Manual check in the browser against the local backend: catalogue pagination, detail with Spanish-then-English description (before the English-only change), login, edit save, mobile layout at 375 px.
- **Not verified**: layout at 768 px and 1280 px; screen reader behaviour beyond the ARIA attributes in the code.

## Docker
- `docker build --target build` for the frontend: passing (after copying `.npmrc`).
- `docker compose up --build` once on alternative host ports: `db` and `api` healthy; web served; nginx proxy reached the API (catalogue listing, admin login, actuator health UP).
- **Not verified**: a full `docker compose down -v && up --build` on a clean machine after the last changes; the backend image build after the English-only change.

## CI (GitHub Actions)
- Workflow added in T0.5 with a frontend job added in T5.1.
- Early PRs (#5 to #17) failed the Spotless check, because formatting was applied later in the stack. The final state passed the backend job in the `docs/T6.3-readme` branch.
- **Not verified** on the `ballastlane-app` repository: CI run results after the final consolidation.

## Not run
- Load or performance tests.
- Security scanning beyond `npm audit`.
- Manual testing with a real ADMIN account in the browser (the DELETE path is covered by the end-to-end test, not by a browser run).
