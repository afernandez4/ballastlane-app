# Decisions and deviations from the plan

Task IDs (T0.1, T2.4, ...) refer to [`planning/planification.md`](planning/planification.md).

## Process
- **Branches and PRs**: one branch per task (`type/T<id>-slug`), stacked, because later tasks depend on earlier code. Each PR's base is the previous branch. The stack was consolidated into a single branch, `feature/challenge-ballastlane`, in the `ballastlane-app` repository.
- **Commits**: test-first where practical (a `test(...)` commit in the red state, then `feat(...)`). Not every task followed this strictly: some adapters were committed together with their tests (noted in PR #18).
- **Commit author**: `Andres <andres_sdq@hotmail.es>` set locally, to avoid publishing the work email.

## Deviations from the plan
| Plan item | What was done | Why |
| --- | --- | --- |
| Spring Boot "3.x" | Spring Boot 3.5.9 | Latest 3.x at implementation time |
| T1.2 and T1.3 order | Exceptions (T1.3) implemented before rules (T1.2) | Rules throw these exceptions |
| T2.1 `findSpecies` and `findEvolutionChain` ports | Merged into `findDetail` | Species and evolution chain come from the same PokeAPI call sequence |
| T2.4.4 MapStruct mapper | Hand-written mapper | Composition of detail, species and evolution chain does not fit MapStruct. MapStruct stays in the pom, unused |
| T0.2.5 Checkstyle | Not added; Spotless only | Time |
| Description language | Spanish then English, later English only | Project is in English; PokeAPI descriptions are requested in `en` |
| Seeds (T4.4) in `db/migration` (V4, V5) | Moved to `db/seed`, loaded only in `local` and `docker` profiles | Keeps persistence tests on a clean schema |
| `profiles.default: local` | Removed | Would load seeds in tests. Start with `SPRING_PROFILES_ACTIVE=local` |
| Local backend port | 8090 (`application-local.yml`) | Set in the local profile; Docker uses 8080 |
| Local Postgres port | 5433 (local profile and `.env`) | Port 5432 was in use on the development machine |
| T5.1 React 18 | React 18.3.1 | As in the plan (React 19 exists) |
| T5.x vitest | vitest 4.1.11 | Fixes an advisory in `@vitest/mocker` present in vitest 2 and 3 |
| npm install | `frontend/.npmrc` with `legacy-peer-deps=true` | npm 10.9 crashes resolving peers for vitest 4 with jsdom |
| T6.1 GenAI exercise (`task-api/`) | **Not done** | Requires the author's own process; the "How I used AI" section cannot be written from the project alone |
| T6.3.5 ADRs | **Not done** | Pending |
| T7 closing (presentation, Q&A, rehearsal) | **Not done** | Pending |
| T0.5.3 CI and coverage badges | **Not done** | Pending until CI is confirmed green on GitHub |

## Bugs found by verification and fixed
- `LazyInitializationException` when mapping element collections outside a transaction. Found by the end-to-end test; `@DataJpaTest` did not catch it because each test runs in one transaction.
- Ambiguous injection of auth use cases (separate beans per interface). Found by the end-to-end test.
- A 403 from method security turned into a 401 through the `/error` dispatch. Found by the end-to-end test.
- `@OrderColumn` seed rows used a 1-based position, leaving a null element. Found by running the app.
- `frontend/package-lock.json` out of sync with `package.json`, which broke `npm ci` in Docker and CI.
- Docker build copied `package.json` and the lock but not `.npmrc`, so `npm ci` failed in the image.
