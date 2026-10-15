# Planificación de implementación – Pokédex API (Java / Spring Boot)

> Backlog de tareas y subtareas que ejecutaré para implementar el ejercicio técnico.
> Orden de ejecución: de adentro hacia afuera (dominio → casos de uso → adaptadores → frontend → entrega).
> Cada subtarea de código sigue el ciclo TDD: **test en rojo → implementación mínima → refactor**, con un commit por paso
> (`test: …`, `feat: …`, `refactor: …`) para que el historial lo evidencie.

## Convenciones

- **Estado**: `[ ]` pendiente · `[x]` hecho
- **ID**: `T<fase>.<tarea>` y `T<fase>.<tarea>.<subtarea>`
- **DoD (Definition of Done) global**: compila sin warnings, tests en verde, cobertura no baja del umbral, código formateado, sin TODOs sueltos, README actualizado si aplica.
- **Ramas**: `main` protegida; una rama por tarea (`feat/T2.3-list-pokemon`) con PR y squash opcional (o commits directos en `main` si prefieres simplicidad; ver dudas).

---

## Fase 0 – Fundaciones del proyecto

### T0.1 Repositorio y estructura base
- [ ] T0.1.1 Crear repositorio público en GitHub (`pokedex-clean-architecture`)
- [ ] T0.1.2 Estructura monorepo: `backend/`, `frontend/`, `task-api/`, `docs/`, `docker-compose.yml`
- [ ] T0.1.3 `.gitignore`, `.editorconfig`, `.env.example`, licencia MIT
- [ ] T0.1.4 README esqueleto con secciones vacías (se completa en Fase 6)

### T0.2 Proyecto Spring Boot
- [ ] T0.2.1 Generar proyecto Java 21 + Spring Boot 3.x (Maven wrapper)
  - Dependencias: web, validation, data-jpa, security, cache, actuator, postgresql, flyway, springdoc-openapi, mapstruct, caffeine, jjwt
  - Test: junit5, mockito, assertj, spring-security-test, wiremock, testcontainers, archunit
- [ ] T0.2.2 Paquetes base: `domain`, `application.port.in`, `application.port.out`, `application.usecase`, `infrastructure.adapter.in.web`, `infrastructure.adapter.out.pokeapi`, `infrastructure.adapter.out.persistence`, `infrastructure.security`, `infrastructure.config`
- [ ] T0.2.3 Perfiles `application.yml` / `application-local.yml` / `application-docker.yml` / `application-test.yml`
- [ ] T0.2.4 Configurar JaCoCo con umbral (80 % global, 85 % en `domain` + `application`) que rompa el build
- [ ] T0.2.5 Plugin de formato (Spotless con google-java-format) y Checkstyle básico

### T0.3 Base de datos local
- [ ] T0.3.1 `docker-compose.yml` con servicio `db` (postgres:16, volumen, healthcheck)
- [ ] T0.3.2 Configurar Flyway y datasource por perfil
- [ ] T0.3.3 Migración vacía `V1__baseline.sql` para validar la conexión

### T0.4 Guardas de arquitectura
- [ ] T0.4.1 Test ArchUnit: `domain` no depende de `application` ni `infrastructure`
- [ ] T0.4.2 Test ArchUnit: `domain` y `application` no importan `org.springframework..`, `jakarta.persistence..`, `com.fasterxml..`
- [ ] T0.4.3 Test ArchUnit: controllers solo dependen de puertos de entrada (no de repositorios ni adaptadores)

### T0.5 Integración continua
- [ ] T0.5.1 GitHub Actions: `mvn verify` (tests + JaCoCo) en cada push/PR
- [ ] T0.5.2 Job de frontend (lint + test + build) — se activa en Fase 5
- [ ] T0.5.3 Badge de CI y cobertura en README

**Criterio de salida Fase 0:** `./mvnw verify` en verde en CI, ArchUnit en verde, Postgres levanta con compose.

---

## Fase 1 – Dominio

### T1.1 Modelos de dominio (records inmutables)
- [ ] T1.1.1 `PokemonSummary` (id, name, spriteUrl, category, weight, abilities) — US01
- [ ] T1.1.2 `PokemonDetail` (id, name, imageUrl, height, weight, types, stats, description, evolutionChain) — US02
- [ ] T1.1.3 `Stat` (name, baseValue), `Ability` (name, hidden), `PokemonType` (name, slot)
- [ ] T1.1.4 `EvolutionStage` (speciesName, pokeApiId, stage, minLevel/trigger) + `EvolutionChain`
- [ ] T1.1.5 `LocalPokemon` (id, pokeApiId, datos base + campos propios: `localName`, `region`, `tags`, `version`, timestamps) — US03/US04
- [ ] T1.1.6 `User` (id, username, email, passwordHash, role) y enum `Role` (USER, ADMIN)
- [ ] T1.1.7 `PageRequest` / `PageResult<T>` propios del dominio (sin depender de Spring Data)

### T1.2 Reglas y validaciones de dominio (TDD)
- [ ] T1.2.1 Validación de paginación: `page ≥ 0`, `1 ≤ size ≤ 50`
- [ ] T1.2.2 Validación de campos propios: `localName` 1–100 sin solo espacios; `region` ≤ 100; `tags` máx. 10, sin duplicados, cada uno ≤ 30
- [ ] T1.2.3 Normalización: trim de textos, tags en minúsculas
- [ ] T1.2.4 `pokeApiId` inmutable tras la creación
- [ ] T1.2.5 Peso y altura > 0 cuando se informan
- [ ] T1.2.6 Aplanado del árbol evolutivo a lista ordenada, soportando ramas (caso Eevee) — test con fixture

### T1.3 Excepciones de dominio
- [ ] T1.3.1 `PokemonNotFoundException`, `UserNotFoundException`
- [ ] T1.3.2 `InvalidPokemonDataException` (con lista de violaciones campo/mensaje)
- [ ] T1.3.3 `InvalidPaginationException`, `DuplicateResourceException`
- [ ] T1.3.4 `ExternalServiceException` (PokeAPI caída / timeout)
- [ ] T1.3.5 `InvalidCredentialsException`, `ConcurrentModificationException`

**Criterio de salida Fase 1:** 100 % de reglas de dominio cubiertas por tests unitarios puros (sin Spring).

---

## Fase 2 – Casos de uso y adaptador PokeAPI (US01, US02)

### T2.1 Puertos
- [ ] T2.1.1 Puertos de entrada: `ListPokemonUseCase`, `GetPokemonDetailUseCase`
- [ ] T2.1.2 Puerto de salida: `PokemonCatalogPort` (`findPage`, `findDetail`, `findSpecies`, `findEvolutionChain`)

### T2.2 Caso de uso US01 – Enumeración (TDD con Mockito)
- [ ] T2.2.1 Test: devuelve página con sprite, categoría, peso y habilidades
- [ ] T2.2.2 Test: valida paginación inválida → `InvalidPaginationException`
- [ ] T2.2.3 Test: página fuera de rango → página vacía con metadatos correctos
- [ ] T2.2.4 Implementación `ListPokemonService`

### T2.3 Caso de uso US02 – Detalle (TDD)
- [ ] T2.3.1 Test: combina detalle + species + cadena evolutiva
- [ ] T2.3.2 Test: búsqueda por id o por nombre (normalizado a minúsculas)
- [ ] T2.3.3 Test: Pokémon inexistente → `PokemonNotFoundException`
- [ ] T2.3.4 Test: selección de idioma de la descripción (es → en → vacío)
- [ ] T2.3.5 Implementación `GetPokemonDetailService`

### T2.4 Adaptador PokeAPI
- [ ] T2.4.1 Guardar fixtures JSON reales: `/pokemon?offset=0&limit=20`, `/pokemon/1`, `/pokemon/133`, `/pokemon-species/1`, `/evolution-chain/1`, `/evolution-chain/67`
- [ ] T2.4.2 DTOs de PokeAPI (solo los campos usados, `@JsonIgnoreProperties(ignoreUnknown = true)`)
- [ ] T2.4.3 Cliente `RestClient` con base URL configurable y timeouts (connect 2 s, read 5 s)
- [ ] T2.4.4 Mapper PokeAPI → dominio (MapStruct + lógica para sprite oficial, genus en, flavor text limpio de `\n` / `\f`)
- [ ] T2.4.5 Carga paralela del detalle de cada ítem de la página (virtual threads / `CompletableFuture`)
- [ ] T2.4.6 Traducción de errores: 404 → `PokemonNotFoundException`; 5xx / timeout → `ExternalServiceException`
- [ ] T2.4.7 Tests con WireMock: éxito, 404, 500, timeout, respuesta con campos faltantes

### T2.5 Caché
- [ ] T2.5.1 `CacheConfig` con Caffeine: cachés `pokemonPage`, `pokemonDetail`, `species`, `evolutionChain`
- [ ] T2.5.2 TTL y tamaño máximo configurables por propiedades
- [ ] T2.5.3 `@Cacheable` en el adaptador (nunca en dominio)
- [ ] T2.5.4 Test: segunda llamada no golpea WireMock (verificar número de requests)

### T2.6 Controller de catálogo
- [ ] T2.6.1 DTOs de respuesta web (`PokemonSummaryResponse`, `PokemonDetailResponse`, `PageResponse<T>`)
- [ ] T2.6.2 `GET /api/v1/pokeapi/pokemon?page&size` — test `@WebMvcTest` (200, 400)
- [ ] T2.6.3 `GET /api/v1/pokeapi/pokemon/{idOrName}` — test (200, 404, 502)
- [ ] T2.6.4 Anotaciones OpenAPI (descripciones, ejemplos, códigos de respuesta)

**Criterio de salida Fase 2:** US01 y US02 funcionando en Swagger contra la PokeAPI real; tests en verde.

---

## Fase 3 – Persistencia, sincronización y CRUD local (US03, US04)

### T3.1 Esquema y entidades
- [ ] T3.1.1 `V2__create_pokemon.sql`: tabla `pokemon` (unique `pokeapi_id`, `version`, timestamps) + `pokemon_ability`, `pokemon_type`, `pokemon_tag`
- [ ] T3.1.2 Índices: `pokeapi_id`, `lower(name)`
- [ ] T3.1.3 Entidades JPA (`PokemonEntity` + colecciones) con `@Version`
- [ ] T3.1.4 Spring Data repository con búsqueda paginada por nombre
- [ ] T3.1.5 Mapper entidad ↔ dominio
- [ ] T3.1.6 `PokemonPersistenceAdapter` implementando `PokemonRepositoryPort`
- [ ] T3.1.7 Tests `@DataJpaTest` + Testcontainers: guardar, buscar, unicidad, bloqueo optimista, N+1 controlado (`@EntityGraph`)

### T3.2 US03 – Sincronización (TDD)
- [ ] T3.2.1 Puerto `SyncPokemonUseCase`
- [ ] T3.2.2 Test: sincroniza un Pokémon nuevo → se guarda, devuelve `created = true`
- [ ] T3.2.3 Test: Pokémon ya existente → actualiza datos base **sin pisar campos propios**, `created = false`
- [ ] T3.2.4 Test: Pokémon inexistente en PokeAPI → `PokemonNotFoundException`
- [ ] T3.2.5 Implementación `SyncPokemonService` (transaccional en el adaptador / config)
- [ ] T3.2.6 Sync masivo por rango (`from`, `to`, máx. 151 por llamada) con resumen `{synced, updated, failed[]}`
- [ ] T3.2.7 Endpoints: `POST /api/v1/pokemon/sync/{idOrName}` (201/200) y `POST /api/v1/pokemon/sync?from&to` (ADMIN)

### T3.3 CRUD local + US04 (TDD)
- [ ] T3.3.1 Puertos: `GetLocalPokemonUseCase`, `ListLocalPokemonUseCase`, `CreateLocalPokemonUseCase`, `UpdateLocalPokemonUseCase`, `DeleteLocalPokemonUseCase`
- [ ] T3.3.2 Test + impl. listar paginado con filtro por nombre / región / tag
- [ ] T3.3.3 Test + impl. obtener por id (404 si no existe)
- [ ] T3.3.4 Test + impl. crear manualmente (409 si `pokeApiId` duplicado)
- [ ] T3.3.5 Test + impl. **PUT** reemplazo completo de campos editables (404, 400, 409 versión)
- [ ] T3.3.6 Test + impl. **PATCH** parcial (distinguir campo ausente vs `null` explícito)
- [ ] T3.3.7 Test + impl. DELETE (204, 404)
- [ ] T3.3.8 DTOs de request con Bean Validation (`@NotBlank`, `@Size`, `@Positive`, `@Pattern`)
- [ ] T3.3.9 Rechazar propiedades desconocidas y body vacío
- [ ] T3.3.10 Controller `/api/v1/pokemon` con `Location` en 201 y ETag/versión en respuesta
- [ ] T3.3.11 Tests `@WebMvcTest` cubriendo todos los códigos (200, 201, 204, 400, 404, 409)

**Criterio de salida Fase 3:** sync idempotente y CRUD completo con validaciones; tests de persistencia con Postgres real.

---

## Fase 4 – Usuarios, seguridad y manejo de errores

### T4.1 Usuarios
- [ ] T4.1.1 `V3__create_users.sql` (unique username/email)
- [ ] T4.1.2 Entidad, repositorio y adaptador `UserRepositoryPort`
- [ ] T4.1.3 Puertos `PasswordHasherPort` (adaptador BCrypt) y `TokenProviderPort` (adaptador JJWT)
- [ ] T4.1.4 `AuthUseCase` – registro (TDD): éxito, username/email duplicado → 409, password débil → 400
- [ ] T4.1.5 `AuthUseCase` – login (TDD): éxito → token; credenciales inválidas → 401 sin revelar cuál campo falló
- [ ] T4.1.6 Endpoints `POST /auth/register`, `POST /auth/login`, `GET /auth/me`

### T4.2 Spring Security
- [ ] T4.2.1 `JwtAuthenticationFilter` (Bearer, validación de firma y expiración)
- [ ] T4.2.2 `SecurityConfig` stateless: rutas públicas (auth, GET catálogo, GET local, Swagger, health) y protegidas (resto)
- [ ] T4.2.3 Roles: DELETE y sync masivo solo ADMIN (`@PreAuthorize`)
- [ ] T4.2.4 `AuthenticationEntryPoint` (401) y `AccessDeniedHandler` (403) en formato ProblemDetail
- [ ] T4.2.5 CORS para el origen del frontend (configurable)
- [ ] T4.2.6 Secreto y expiración por variables de entorno; fallo de arranque si falta el secreto en perfil docker
- [ ] T4.2.7 Tests: sin token → 401, token expirado → 401, USER en DELETE → 403, ADMIN → 204

### T4.3 Manejo global de errores
- [ ] T4.3.1 `GlobalExceptionHandler` con `ProblemDetail` (RFC 7807) para todas las excepciones de dominio
- [ ] T4.3.2 Errores de validación con `errors[]` (campo, mensaje, valor rechazado)
- [ ] T4.3.3 JSON malformado, tipo incorrecto en path/query, método no permitido (405), media type (415)
- [ ] T4.3.4 Fallback 500 sin stack trace + log con correlation id (filtro MDC + header `X-Correlation-Id`)
- [ ] T4.3.5 Test por cada mapeo excepción → código HTTP

### T4.4 Datos semilla
- [ ] T4.4.1 `V4__seed_users.sql`: `admin / Admin123!` (ADMIN), `demo / Demo1234!` (USER) con hash BCrypt
- [ ] T4.4.2 `V5__seed_pokemon.sql`: Pokémon #1–#20 precargados, algunos con `localName`, `region` y `tags`
- [ ] T4.4.3 Test de integración `@SpringBootTest` end-to-end: login → sync → update → get → delete

**Criterio de salida Fase 4:** backend completo, Swagger con Authorize JWT, cobertura ≥ umbral.

---

## Fase 5 – Frontend React

### T5.1 Setup
- [ ] T5.1.1 Vite + React 18 + TypeScript, ESLint + Prettier, alias de rutas
- [ ] T5.1.2 Tailwind CSS, React Router, TanStack Query, React Hook Form + Zod, axios
- [ ] T5.1.3 Vitest + React Testing Library + MSW (handlers para todos los endpoints)
- [ ] T5.1.4 Variables `VITE_API_URL`; proxy en dev

### T5.2 Infraestructura de la app
- [ ] T5.2.1 Cliente HTTP con interceptor JWT y manejo de 401 (logout + redirect)
- [ ] T5.2.2 Tipos TS de las respuestas de la API (y de `ProblemDetail`)
- [ ] T5.2.3 `AuthContext` (token en memoria + `sessionStorage`), `ProtectedRoute`, `AdminOnly`
- [ ] T5.2.4 Layout responsivo (navbar, menú móvil), componentes `Pagination`, `Spinner`, `ErrorState`, `EmptyState`, `Toast`

### T5.3 Pantallas
- [ ] T5.3.1 Login y Registro (validación cliente + errores del servidor por campo)
- [ ] T5.3.2 Pokédex (US01): grid de tarjetas, paginación, skeletons, estados error/vacío
- [ ] T5.3.3 Detalle (US02): imagen, barras de stats, tipos, descripción, cadena evolutiva navegable, botón "Sincronizar" (US03)
- [ ] T5.3.4 Mis Pokémon: lista local con búsqueda, filtros, paginación, acciones editar/borrar (borrar solo ADMIN con confirmación)
- [ ] T5.3.5 Editar (US04): formulario de campos propios, manejo de 400/404/409
- [ ] T5.3.6 Página 404 del frontend

### T5.4 Calidad
- [ ] T5.4.1 Tests de componentes clave (tarjeta, paginación, formulario de edición, rutas protegidas)
- [ ] T5.4.2 Accesibilidad básica: labels, `alt`, foco visible, contraste
- [ ] T5.4.3 Verificar cero warnings en consola (keys, StrictMode, efectos duplicados)
- [ ] T5.4.4 Revisión responsive en 360 px, 768 px y 1280 px

**Criterio de salida Fase 5:** todas las historias operables desde la UI, sin warnings, tests en verde.

---

## Fase 6 – GenAI, contenedores y documentación

### T6.1 Ejercicio GenAI (Task API)
- [ ] T6.1.1 Redactar el prompt final en `docs/GENAI.md`
- [ ] T6.1.2 Generar el scaffold de `task-api/` y guardar la salida original sin editar (`docs/genai/raw-output/`)
- [ ] T6.1.3 Revisión crítica: compilar, ejecutar tests, buscar IDOR, validación de `dueDate`, transiciones de estado, PATCH null vs ausente, N+1, APIs obsoletas
- [ ] T6.1.4 Aplicar correcciones con commits separados y documentar antes/después
- [ ] T6.1.5 Añadir tests faltantes (usuario B no accede a tareas de A, etc.)
- [ ] T6.1.6 Sección "Cómo usé IA en el proyecto principal" y dónde no confié en ella

### T6.2 Docker
- [ ] T6.2.1 `backend/Dockerfile` multi-stage (Maven → JRE alpine), usuario no root, `HEALTHCHECK`
- [ ] T6.2.2 `frontend/Dockerfile` multi-stage (Node → Nginx) con `try_files` y proxy `/api`
- [ ] T6.2.3 `docker-compose.yml` final: `db`, `api` (depends_on healthy), `web`; `.env.example`
- [ ] T6.2.4 Probar `docker compose down -v && docker compose up --build` desde cero

### T6.3 Documentación
- [ ] T6.3.1 README: descripción, capturas/GIF, arquitectura (diagrama), historias → endpoint + pantalla
- [ ] T6.3.2 README: ejecución con Docker y en local, credenciales demo, URLs, variables de entorno
- [ ] T6.3.3 README: tests y cobertura, decisiones técnicas y trade-offs, mejoras futuras
- [ ] T6.3.4 Colección Postman / archivo `.http` con todas las peticiones de ejemplo
- [ ] T6.3.5 ADRs cortos en `docs/adr/` (arquitectura, JWT, caché, réplica local)

---

## Fase 7 – Cierre y presentación

- [ ] T7.1 Revisión final de código: nombres, duplicación, métodos largos, warnings del compilador
- [ ] T7.2 Revisión de cobertura y casos borde faltantes
- [ ] T7.3 Verificar checklist de requisitos contra el enunciado
- [ ] T7.4 Guion de presentación (arquitectura → demo → tests → GenAI → trade-offs) en `docs/PRESENTATION.md`
- [ ] T7.5 Lista de preguntas probables del panel con respuestas preparadas
- [ ] T7.6 Ensayo completo de la demo desde cero

---

## Trazabilidad requisitos → tareas

| Requisito del enunciado | Tareas |
| --- | --- |
| US01 Enumeración paginada + caché | T1.1.1, T1.2.1, T2.2, T2.4, T2.5, T2.6.2, T5.3.2 |
| US02 Vista detallada | T1.1.2–T1.1.4, T1.2.6, T2.3, T2.4, T2.6.3, T5.3.3 |
| US03 Sincronización a BD relacional | T3.1, T3.2, T5.3.3 |
| US04 Modificación local con 404/400 | T1.2.2–T1.2.5, T3.3.5–T3.3.11, T4.3, T5.3.5 |
| CRUD completo | T3.3, T5.3.4 |
| Usuarios, autenticación, rutas protegidas | T4.1, T4.2, T5.2.3, T5.3.1 |
| Capa de datos dedicada | T3.1 |
| Lógica de negocio independiente | T0.4, T1, T2.1–T2.3, T3.2–T3.3 |
| Pruebas unitarias de todo el core | Todas las subtareas TDD + T0.2.4 |
| Manejo de errores | T1.3, T4.3 |
| Frontend responsivo con CRUD | T5 |
| Seed data / credenciales demo | T4.4 |
| Dockerfile | T6.2 |
| README | T0.1.4, T6.3 |
| Sección GenAI | T6.1 |
| Repositorio público | T0.1.1 |

---

## Dudas abiertas (a confirmar antes de empezar)

1. **Repositorio**: ¿lo creas tú en tu cuenta de GitHub y me das acceso, o trabajo en local y tú lo subes? (Necesito acceso de escritura para hacer push.)
2. **Plazo de entrega**: ¿cuántos días tienes? Define si incluyo los "plus" (CI, ADRs, PATCH, sync masivo, multi-módulo).
3. **Build tool**: ¿Maven o Gradle? (Asumo Maven.)
4. **Frontend**: ¿React está bien o la empresa usa Vue/Angular?
5. **Idioma**: ¿código, commits y README en inglés (asumido) y presentación en español?
6. **Campos propios (US03)**: ¿te sirven `localName`, `region` y `tags`, o prefieres otros?
7. **Alcance de edición (US04)**: ¿solo campos propios editables, o también nombre/peso/altura replicados?
8. **Lectura pública vs protegida**: asumo GET públicos y escrituras con JWT. ¿Correcto?
9. **Herramienta GenAI a citar**: ¿la sección menciona Claude Code / Claude, u otra herramienta que uses tú?
10. **Flujo Git**: ¿ramas + PR por tarea o commits directos en `main`?
