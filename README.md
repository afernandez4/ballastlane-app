# ballastlane-challenge

Pokédex: a Spring Boot API with hexagonal architecture and a React web app. It browses Pokémon from PokeAPI, stores a local copy in Postgres, and lets users edit their own data.

## Contents

- [User stories](#user-stories)
- [Architecture](#architecture)
- [Run with Docker](#run-with-docker)
- [Run locally](#run-locally)
- [URLs](#urls)
- [Demo credentials](#demo-credentials)
- [Environment variables](#environment-variables)
- [Tests and coverage](#tests-and-coverage)
- [Technical decisions and trade-offs](#technical-decisions-and-trade-offs)
- [Project status](#project-status)

## User stories

| Story | Endpoint | Screen |
| --- | --- | --- |
| US01 Paginated listing (cached) | `GET /api/v1/pokeapi/pokemon?page&size` | Pokédex (`/pokedex`) |
| US02 Detail view | `GET /api/v1/pokeapi/pokemon/{idOrName}` | Detail (`/pokedex/:idOrName`) |
| US03 Sync to the relational database | `POST /api/v1/pokemon/sync/{idOrName}`; `POST /api/v1/pokemon/sync?from&to` (ADMIN) | "Save to my catalogue" button on the detail page |
| US04 Local edit with 404/400/409 | `PUT` and `PATCH /api/v1/pokemon/{id}` (`If-Match`) | My Pokémon (`/my-pokemon`) and edit (`/my-pokemon/:id/edit`) |
| Local CRUD | `GET`, `POST`, `PUT`, `PATCH`, `DELETE /api/v1/pokemon` (DELETE is ADMIN only) | My Pokémon |
| Users and JWT | `POST /api/v1/auth/register`, `POST /api/v1/auth/login`, `GET /api/v1/auth/me` | Log in and sign up |

Interactive API docs (Swagger UI): `http://localhost:8080/swagger-ui.html` with Docker, or `http://localhost:8090/swagger-ui.html` with the `local` profile.

## Architecture

```
backend/
  domain/                       models and rules (no Spring)
  application/
    port/in                     use cases (inbound)
    port/out                    ports to PokeAPI, database, hashing and tokens
    usecase/                    application services
  infrastructure/
    adapter/in/web              REST controllers, DTOs, ProblemDetail
    adapter/out/pokeapi         RestClient, PokeAPI DTOs and mapper
    adapter/out/persistence     JPA, Flyway, entity <-> domain mapper
    security                    JWT, filter, access rules, CORS
    config                      use case wiring, cache and properties
frontend/                       React 18 + Vite + TypeScript + Tailwind
task-api/                       reserved for the GenAI exercise (pending)
docs/                           HTTP examples
```

Dependency rules are checked with ArchUnit (`ArchitectureTest`): the domain does not depend on the application or infrastructure layers, the domain and application do not use frameworks, and controllers only use inbound ports.

## Run with Docker

Requirements: Docker with Compose.

```bash
cp .env.example .env
# set JWT_SECRET in .env (at least 32 bytes); compose will not start without it
docker compose up --build
```

Services: `db` (Postgres 16), `api` (Spring Boot with the `docker` profile) and `web` (React served by nginx, which forwards `/api` to the backend).

### Ports

| Service | Host port (in `.env`) | Container port | This setup |
| --- | --- | --- | --- |
| Postgres (`db`) | `POSTGRES_PORT` | 5432 | **5433** |
| API in Docker (`api`) | `API_PORT` | 8080 | 8080 |
| API run locally (`local` profile) | `server.port` in `application-local.yml` | — | **8090** |
| Web (`web`, nginx) | `WEB_PORT` | 80 | 8081 |
| Vite dev server (not Docker) | fixed | — | 5173 |

Containers talk to each other by service name, so the Postgres container always listens on 5432 internally; only the host port changes.

## Run locally

Requirements: Java 21, Maven (or `./mvnw`), Node 22, Docker (for the database and tests).

1. Database:
   ```bash
   docker compose up -d db
   ```
   The `local` profile starts the API on port **8090** (not 8080, as in Docker).

   Postgres is published on host port **5433**, and the `local` profile connects there (`jdbc:postgresql://localhost:5433/pokedex`). Set `POSTGRES_PORT=5433` in `.env` (see the port table above) so compose publishes it on that port.
2. Backend (`local` profile, with demo seed data):
   ```bash
   cd backend
   SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
   ```
3. Frontend (Vite serves on port 5173 and forwards `/api` to `localhost:8080` by default):
   ```bash
   cd frontend
   npm ci
   VITE_PROXY_TARGET=http://localhost:8090 npm run dev
   ```
   Use `VITE_PROXY_TARGET` because the local backend listens on 8090. Without it, `/api` requests go to port 8080 and fail.

## URLs

| What | Where |
| --- | --- |
| Application (Docker) | <http://localhost:8081> |
| Postgres (host) | `localhost:5433` |
| Application (development) | <http://localhost:5173> |
| API (Docker) | <http://localhost:8080/api/v1> |
| API (`local` profile) | <http://localhost:8090/api/v1> |
| Swagger UI (Docker) | <http://localhost:8080/swagger-ui.html> |
| Swagger UI (`local` profile) | <http://localhost:8090/swagger-ui.html> |
| Health (`local` profile) | <http://localhost:8090/actuator/health> |

Example requests for any HTTP client: [`docs/api.http`](docs/api.http).

## Demo credentials

They only exist in the `local` and `docker` profiles (seed data).

| Username | Password | Role |
| --- | --- | --- |
| `admin` | `Admin123!` | ADMIN (can delete and sync in bulk) |
| `demo` | `Demo1234!` | USER |

The seed also loads Pokémon #1 to #20 with PokeAPI data and some own fields.

## Environment variables

| Variable | Default | Purpose |
| --- | --- | --- |
| `JWT_SECRET` | — (required in `docker`) | Token signing key; at least 32 bytes |
| `JWT_EXPIRATION_MINUTES` | `60` | Token lifetime |
| `POKEAPI_BASE_URL` | `https://pokeapi.co/api/v2` | Data source |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Allowed origins (comma-separated) |
| `POSTGRES_PORT` | `5432` in compose; `5433` in this setup | Host port for Postgres |
| `API_PORT` | `8080` | Host port for the API (Docker only) |
| `VITE_PROXY_TARGET` | `http://localhost:8080` | Where the Vite dev server forwards `/api`; set to `http://localhost:8090` for the `local` backend |
| `WEB_PORT` | `8081` | Host port for the web app |
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | `pokedex` | Database |
| `DB_URL` | `jdbc:postgresql://db:5432/pokedex` | `docker` profile only |
| `VITE_API_URL` | empty | Backend URL for the frontend build (empty = same origin) |

## Tests and coverage

```bash
cd backend && ./mvnw verify     # tests, Testcontainers, ArchUnit, Spotless and JaCoCo
cd frontend && npm test         # Vitest + Testing Library + MSW
```

- Backend: unit tests for domain and use cases (Mockito), the PokeAPI adapter with WireMock and real fixtures, controllers with `@WebMvcTest`, persistence against Postgres 16 with Testcontainers, and an end-to-end test (`EndToEndFlowTest`) covering register → log in → sync → edit → delete.
- JaCoCo requires 80% line coverage overall and 85% for `domain` + `application`.
- Frontend: HTTP client and 401 handling, token decoding, protected routes, pagination, card, edit form (validation and 400/409 errors), and login.
- Testcontainers tests need Docker running.

## Technical decisions and trade-offs

- **Hexagonal architecture with a framework-free core.** Use cases are plain Java classes; Spring only wires them. Cost: more files and mappings. Benefit: the domain is tested without a context.
- **Two persistence models.** The JPA entity is separate from the domain model. The mapper is hand-written because combining detail, species and evolution chain does not fit MapStruct without heavy configuration.
- **Caching in the infrastructure layer.** Caffeine in the PokeAPI adapter and client, with configurable TTL and size. Trade-off: each instance has its own cache.
- **Sync as an upsert.** Syncing never overwrites own fields (`localName`, `region`, `tags`); only base data is refreshed.
- **Optimistic concurrency.** `@Version` in the database, `ETag` on responses and `If-Match` on PUT. A stale token returns 409.
- **Stateless security.** HS256 JWTs signed with a secret of at least 32 bytes; startup fails if it is missing in `docker`. The frontend keeps the token in memory and `sessionStorage`, and reads it only to show the user name and hide admin buttons. Real authorization is enforced by the backend.
- **RFC 7807 errors.** Every error response is a `ProblemDetail`; 500 responses do not expose details and include a `correlationId`.
- **Seed data outside tests.** Demo data lives in `db/seed` and only loads in `local` and `docker`, so persistence tests run on a clean schema.
- **English only.** Code, UI, documentation and seed data are in English. PokeAPI descriptions are requested in English.
- **React 18** with TanStack Query for server state; forms use React Hook Form with Zod, with the same rules as the backend.

## Project status

- Done: backend (phases 0 to 4), frontend (phase 5), Docker and documentation.
- Pending: the GenAI exercise in `task-api/` (T6.1). It depends on the author's own process, and the "How I used AI" section must reflect it; it is not generated.
- Pending: presentation material and expert Q&A (phase 7), and ADRs.
- Pending CI verification: frontend and backend jobs on GitHub with Docker available.
