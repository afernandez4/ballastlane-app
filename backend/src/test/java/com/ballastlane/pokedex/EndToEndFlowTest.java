package com.ballastlane.pokedex;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * T4.4.3: login → sync → update → get → delete against the whole application. PokeAPI is replaced
 * by WireMock serving real fixtures; the database is a real Postgres container.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@SuppressWarnings({"rawtypes", "unchecked"}) // JSON bodies are read as plain maps
class EndToEndFlowTest {

  /** BCrypt of "Admin123!", created with the project's own encoder. */
  private static final String ADMIN_HASH =
      "$2a$10$drW.d8p24HzTofuFzltxNe4pd7JO229x1GdBnpJ/kAQ4z6M.RB6B6";

  private static WireMockServer pokeApi;

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private TestRestTemplate rest;
  @Autowired private JdbcTemplate jdbc;

  @BeforeAll
  static void startPokeApi() {
    pokeApi = new WireMockServer(options().dynamicPort());
    pokeApi.start();
    pokeApi.stubFor(
        get(urlPathEqualTo("/api/v2/pokemon/1")).willReturn(okJson(fixture("pokemon-1.json"))));
    pokeApi.stubFor(
        get(urlPathEqualTo("/api/v2/pokemon-species/bulbasaur"))
            .willReturn(okJson(fixture("pokemon-species-1.json"))));
    pokeApi.stubFor(
        get(urlPathEqualTo("/api/v2/evolution-chain/1"))
            .willReturn(okJson(fixture("evolution-chain-1.json"))));
  }

  @AfterAll
  static void stopPokeApi() {
    pokeApi.stop();
  }

  @DynamicPropertySource
  static void pokeApiUrl(DynamicPropertyRegistry registry) {
    registry.add(
        "pokedex.pokeapi.base-url", () -> "http://localhost:" + pokeApi.port() + "/api/v2");
  }

  @Test
  void registerLoginSyncUpdateGetAndDeleteFlow() {
    // 1. Register and log in as a normal user.
    ResponseEntity<Map> registered =
        rest.postForEntity(
            "/api/v1/auth/register",
            json(Map.of("username", "ash", "email", "ash@example.com", "password", "Pikachu123")),
            Map.class);
    assertThat(registered.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    String userToken = login("ash", "Pikachu123");
    assertThat(
            rest.exchange("/api/v1/auth/me", HttpMethod.GET, auth(userToken, null), Map.class)
                .getStatusCode())
        .isEqualTo(HttpStatus.OK);

    // 2. Sync Pokémon #1 from PokeAPI. Created → 201 with Location.
    ResponseEntity<Map> synced =
        rest.exchange("/api/v1/pokemon/sync/1", HttpMethod.POST, auth(userToken, null), Map.class);
    assertThat(synced.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(synced.getHeaders().getLocation().getPath())
        .isEqualTo("/api/v1/pokemon/" + synced.getBody().get("id"));
    long id = ((Number) synced.getBody().get("id")).longValue();
    String etag = synced.getHeaders().getFirst(HttpHeaders.ETAG);

    // 3. Edit own fields with If-Match. Stale version → 409.
    ResponseEntity<Map> staleUpdate =
        rest.exchange(
            "/api/v1/pokemon/" + id,
            HttpMethod.PUT,
            auth(
                userToken,
                json(
                    Map.of(
                        "localName",
                        "Little Seed",
                        "region",
                        "Kanto",
                        "tags",
                        java.util.List.of("starter")),
                    "\"99\"")),
            Map.class);
    assertThat(staleUpdate.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

    ResponseEntity<Map> updated =
        rest.exchange(
            "/api/v1/pokemon/" + id,
            HttpMethod.PUT,
            auth(
                userToken,
                json(
                    Map.of(
                        "localName",
                        "Little Seed",
                        "region",
                        "Kanto",
                        "tags",
                        java.util.List.of("starter")),
                    etag)),
            Map.class);
    assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);

    // 4. Public read sees the edit, and the base data is untouched.
    ResponseEntity<Map> read = rest.getForEntity("/api/v1/pokemon/" + id, Map.class);
    assertThat(read.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(read.getBody())
        .containsEntry("localName", "Little Seed")
        .containsEntry("name", "bulbasaur")
        .containsEntry("pokeApiId", 1);

    // 5. Only an ADMIN may delete: a normal user gets 403, the admin gets 204.
    ResponseEntity<Void> forbidden =
        rest.exchange(
            "/api/v1/pokemon/" + id, HttpMethod.DELETE, auth(userToken, null), Void.class);
    assertThat(forbidden.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    jdbc.update(
        "insert into users (username, email, password_hash, role) values (?, ?, ?, ?)",
        "root",
        "root@example.com",
        ADMIN_HASH,
        "ADMIN");
    String adminToken = login("root", "Admin123!");
    ResponseEntity<Void> deleted =
        rest.exchange(
            "/api/v1/pokemon/" + id, HttpMethod.DELETE, auth(adminToken, null), Void.class);
    assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    assertThat(rest.getForEntity("/api/v1/pokemon/" + id, Map.class).getStatusCode())
        .isEqualTo(HttpStatus.NOT_FOUND);
  }

  private String login(String username, String password) {
    ResponseEntity<Map> response =
        rest.postForEntity(
            "/api/v1/auth/login",
            json(Map.of("username", username, "password", password)),
            Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    return (String) response.getBody().get("accessToken");
  }

  private static HttpEntity<Object> json(Map<String, ?> body) {
    return json(body, null);
  }

  private static HttpEntity<Object> json(Object body, String ifMatch) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (ifMatch != null) {
      headers.set(HttpHeaders.IF_MATCH, ifMatch);
    }
    return new HttpEntity<>(body, headers);
  }

  private static HttpEntity<Object> auth(String token, HttpEntity<Object> request) {
    HttpHeaders headers = new HttpHeaders();
    if (request != null) {
      headers.putAll(request.getHeaders());
    }
    headers.setBearerAuth(token);
    return new HttpEntity<>(request == null ? null : request.getBody(), headers);
  }

  private static String fixture(String name) {
    try (InputStream in = EndToEndFlowTest.class.getResourceAsStream("/fixtures/pokeapi/" + name)) {
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException("Missing fixture " + name, e);
    }
  }
}
