package com.ballastlane.pokedex.infrastructure.config;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.ballastlane.pokedex.application.port.out.PokemonCatalogPort;
import com.ballastlane.pokedex.domain.model.PageRequest;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/** T2.5.4: a second identical call must not reach PokeAPI. Uses a real Spring context. */
class CachingConfigurationTest {

  private static final List<String> LANGUAGES = List.of("es", "en");

  @RegisterExtension
  static WireMockExtension wm =
      WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

  private ApplicationContextRunner runner() {
    return new ApplicationContextRunner()
        .withUserConfiguration(CacheConfig.class, PokedexConfiguration.class)
        .withPropertyValues(
            "pokedex.pokeapi.base-url=" + wm.baseUrl() + "/api/v2",
            "pokedex.pokeapi.connect-timeout=2s",
            "pokedex.pokeapi.read-timeout=5s",
            "pokedex.cache.ttl=10m",
            "pokedex.cache.max-size=100");
  }

  @Test
  void secondDetailRequestIsServedFromCache() {
    wm.stubFor(get(urlPathEqualTo("/api/v2/pokemon/1")).willReturn(okJson(read("pokemon-1.json"))));
    wm.stubFor(
        get(urlPathEqualTo("/api/v2/pokemon-species/bulbasaur"))
            .willReturn(okJson(read("pokemon-species-1.json"))));
    wm.stubFor(
        get(urlPathEqualTo("/api/v2/evolution-chain/1"))
            .willReturn(okJson(read("evolution-chain-1.json"))));

    runner()
        .run(
            context -> {
              PokemonCatalogPort catalog = context.getBean(PokemonCatalogPort.class);
              catalog.findDetail("1", LANGUAGES);
              catalog.findDetail("1", LANGUAGES);
            });

    wm.verify(1, getRequestedFor(urlPathEqualTo("/api/v2/pokemon/1")));
    wm.verify(1, getRequestedFor(urlPathEqualTo("/api/v2/pokemon-species/bulbasaur")));
    wm.verify(1, getRequestedFor(urlPathEqualTo("/api/v2/evolution-chain/1")));
  }

  @Test
  void secondPageRequestIsServedFromCache() {
    wm.stubFor(
        get(urlPathEqualTo("/api/v2/pokemon"))
            .withQueryParam("offset", equalTo("0"))
            .withQueryParam("limit", equalTo("20"))
            .willReturn(okJson(read("pokemon-list-offset0-limit20.json"))));
    wm.stubFor(
        get(urlPathMatching("/api/v2/pokemon/\\d+")).willReturn(okJson(read("pokemon-1.json"))));
    wm.stubFor(
        get(urlPathMatching("/api/v2/pokemon-species/\\d+"))
            .willReturn(okJson(read("pokemon-species-1.json"))));

    runner()
        .run(
            context -> {
              PokemonCatalogPort catalog = context.getBean(PokemonCatalogPort.class);
              catalog.findPage(new PageRequest(0, 20));
              catalog.findPage(new PageRequest(0, 20));
            });

    wm.verify(1, getRequestedFor(urlPathEqualTo("/api/v2/pokemon")));
  }

  private static String read(String fileName) {
    try (InputStream in =
        CachingConfigurationTest.class.getResourceAsStream("/fixtures/pokeapi/" + fileName)) {
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException("Missing fixture " + fileName, e);
    }
  }
}
