# ballastlane-chalenge

Pokédex: API en Spring Boot con arquitectura hexagonal y aplicación web en React. Consulta Pokémon desde PokeAPI, guarda una copia local en Postgres y permite editar tus propios datos.

> Repositorio: <https://github.com/afernandez4/ballastlane-chalenge>

## Contenido

- [Historias de usuario](#historias-de-usuario)
- [Arquitectura](#arquitectura)
- [Ejecución con Docker](#ejecución-con-docker)
- [Ejecución en local](#ejecución-en-local)
- [URLs](#urls)
- [Credenciales demo](#credenciales-demo)
- [Variables de entorno](#variables-de-entorno)
- [Tests y cobertura](#tests-y-cobertura)
- [Decisiones técnicas y trade-offs](#decisiones-técnicas-y-trade-offs)
- [Estado del proyecto](#estado-del-proyecto)

## Historias de usuario

| Historia | Endpoint | Pantalla |
| --- | --- | --- |
| US01 Enumeración paginada (caché) | `GET /api/v1/pokeapi/pokemon?page&size` | Pokédex (`/pokedex`) |
| US02 Vista detallada | `GET /api/v1/pokeapi/pokemon/{idOrName}` | Detalle (`/pokedex/:idOrName`) |
| US03 Sincronización a BD relacional | `POST /api/v1/pokemon/sync/{idOrName}`; `POST /api/v1/pokemon/sync?from&to` (ADMIN) | Botón "Guardar en mi catálogo" en el detalle |
| US04 Modificación local con 404/400/409 | `PUT` y `PATCH /api/v1/pokemon/{id}` (`If-Match`) | Mis Pokémon (`/mis-pokemon`) y edición (`/mis-pokemon/:id/editar`) |
| CRUD local | `GET`, `POST`, `PUT`, `PATCH`, `DELETE /api/v1/pokemon` (DELETE solo ADMIN) | Mis Pokémon |
| Usuarios y JWT | `POST /api/v1/auth/register`, `POST /api/v1/auth/login`, `GET /api/v1/auth/me` | Login y registro |

Documentación interactiva de la API (Swagger UI): `http://localhost:8080/swagger-ui.html` cuando el backend está levantado.

## Arquitectura

```
backend/
  domain/                       modelos y reglas (sin Spring)
  application/
    port/in                     casos de uso (entrada)
    port/out                    puertos hacia PokeAPI, BD, hash y tokens
    usecase/                    servicios de aplicación
  infrastructure/
    adapter/in/web              controladores REST, DTOs, ProblemDetail
    adapter/out/pokeapi         cliente RestClient, DTOs y mapper de PokeAPI
    adapter/out/persistence     JPA, Flyway, mapper entidad <-> dominio
    security                    JWT, filtro, reglas de acceso, CORS
    config                      cableado de casos de uso, caché y propiedades
frontend/                       React 18 + Vite + TypeScript + Tailwind
task-api/                       reservado para el ejercicio GenAI (pendiente)
docs/                           notas de decisiones
```

Las reglas de dependencia se verifican con ArchUnit (`ArchitectureTest`): el dominio no depende de la aplicación ni de la infraestructura, el dominio y la aplicación no usan frameworks, y los controladores solo usan puertos de entrada.

## Ejecución con Docker

Requisitos: Docker con Compose.

```bash
cp .env.example .env
# edita JWT_SECRET en .env (al menos 32 bytes); sin él, compose no arranca
docker compose up --build
```

Servicios: `db` (Postgres 16), `api` (Spring Boot en perfil `docker`) y `web` (React servido por nginx, que reenvía `/api` al backend). Los puertos del host son configurables con `POSTGRES_PORT`, `API_PORT` y `WEB_PORT` en `.env`.

## Ejecución en local

Requisitos: Java 21, Maven (o `./mvnw`), Node 22, Docker (para la base de datos y los tests).

1. Base de datos:
   ```bash
   docker compose up -d db
   ```
2. Backend (perfil `local`, con los seeds de demo):
   ```bash
   cd backend
   SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
   ```
3. Frontend (Vite reenvía `/api` a `localhost:8080`):
   ```bash
   cd frontend
   npm ci
   npm run dev
   ```

## URLs

| Qué | Dónde |
| --- | --- |
| Aplicación (Docker) | <http://localhost:8081> |
| Aplicación (desarrollo) | <http://localhost:5173> |
| API | <http://localhost:8080/api/v1> |
| Swagger UI | <http://localhost:8080/swagger-ui.html> |
| Health | <http://localhost:8080/actuator/health> |

Peticiones de ejemplo para cualquier cliente HTTP: [`docs/api.http`](docs/api.http).

## Credenciales demo

Solo existen en los perfiles `local` y `docker` (datos de seed).

| Usuario | Contraseña | Rol |
| --- | --- | --- |
| `admin` | `Admin123!` | ADMIN (puede borrar y sincronizar en masa) |
| `demo` | `Demo1234!` | USER |

El seed también carga Pokémon #1 a #20 con datos de PokeAPI y algunos campos propios.

## Variables de entorno

| Variable | Por defecto | Uso |
| --- | --- | --- |
| `JWT_SECRET` | — (obligatoria en `docker`) | Firma de tokens; mínimo 32 bytes |
| `JWT_EXPIRATION_MINUTES` | `60` | Caducidad del token |
| `POKEAPI_BASE_URL` | `https://pokeapi.co/api/v2` | Origen de los datos |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Orígenes permitidos (lista separada por comas) |
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | `pokedex` | Base de datos |
| `DB_URL` | `jdbc:postgresql://db:5432/pokedex` | Solo perfil `docker` |
| `VITE_API_URL` | vacío | URL del backend para el build del frontend (vacío = mismo origen) |

## Tests y cobertura

```bash
cd backend && ./mvnw verify     # tests, Testcontainers, ArchUnit, Spotless y JaCoCo
cd frontend && npm test         # Vitest + Testing Library + MSW
```

- Backend: tests unitarios de dominio y casos de uso (Mockito), adaptador de PokeAPI con WireMock y fixtures reales, controladores con `@WebMvcTest`, persistencia contra Postgres 16 con Testcontainers, y un test de extremo a extremo (`EndToEndFlowTest`) con el flujo registro → login → sincronización → edición → borrado.
- JaCoCo exige 80 % de líneas en el conjunto y 85 % en `domain` + `application`.
- Frontend: cliente HTTP y manejo de 401, decodificación del token, rutas protegidas, paginación, tarjeta, formulario de edición (validación y errores 400/409) y login.
- Los tests con Testcontainers necesitan Docker en ejecución.

## Decisiones técnicas y trade-offs

- **Hexagonal sin frameworks en el núcleo.** Los casos de uso son clases Java planas; Spring solo cablea. Coste: más ficheros y mapeos. Beneficio: el dominio se prueba sin contexto.
- **Dos modelos de persistencia.** La entidad JPA es distinta del modelo de dominio; el mapper es código a mano porque la composición de datos (detalle + especie + cadena) no encaja en MapStruct sin mucha configuración.
- **Caché en infraestructura.** Caffeine en el adaptador y el cliente de PokeAPI, con TTL y tamaño configurables. Trade-off: cada instancia tiene su propia caché.
- **Sincronización como upsert.** Sincronizar nunca pisa campos propios (`localName`, `region`, `tags`); solo se refrescan los datos base.
- **Concurrencia optimista.** `@Version` en BD, `ETag` en las respuestas y `If-Match` en PUT. Un token desfasado devuelve 409.
- **Seguridad sin estado.** JWT HS256 firmado con secreto de 32 bytes o más; el arranque falla si falta en `docker`. El frontend guarda el token en memoria y en `sessionStorage`, y solo lo lee para mostrar el usuario y ocultar botones de admin: la autorización real está en el backend.
- **Errores RFC 7807.** Todas las respuestas de error son `ProblemDetail`; los 500 no exponen detalles y llevan `correlationId`.
- **Seeds fuera de los tests.** Los datos de demo están en `db/seed` y solo se cargan en `local` y `docker`, así los tests de persistencia trabajan sobre esquema limpio.
- **Frontend en React 18** (según el plan) con TanStack Query para caché de servidor; los formularios usan React Hook Form con Zod, con las mismas reglas que el backend.

## Estado del proyecto

- Hecho: backend (Fases 0 a 4), frontend (Fase 5), Docker y documentación básica.
- Pendiente: ejercicio GenAI de `task-api/` (T6.1). Requiere el proceso de trabajo real del autor y la sección "Cómo usé IA" debe reflejarlo; no está generada.
- Pendiente: presentación y preguntas del panel (Fase 7) y ADRs.
- Pendiente de verificar en CI: el job de frontend y el de backend con Docker disponible.
