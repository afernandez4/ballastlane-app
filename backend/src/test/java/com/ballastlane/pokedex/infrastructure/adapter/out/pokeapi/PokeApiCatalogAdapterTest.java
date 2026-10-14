package com.ballastlane.pokedex.infrastructure.adapter.out.pokeapi;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.status;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ballastlane.pokedex.domain.exception.ExternalServiceException;
import com.ballastlane.pokedex.domain.model.Ability;
import com.ballastlane.pokedex.domain.model.PageRequest;
import com.ballastlane.pokedex.domain.model.PageResult;
import com.ballastlane.pokedex.domain.model.PokemonDetail;
import com.ballastlane.pokedex.domain.model.PokemonSummary;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

class PokeApiCatalogAdapterTest {

  private static final List<String> LANGUAGES = List.of("es", "en");

  @RegisterExtension
  static WireMockExtension wm =
      WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

  private PokeApiCatalogAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = adapterWith(RestClient.builder().baseUrl(wm.baseUrl() + "/api/v2").build());
  }

  private static PokeApiCatalogAdapter adapterWith(RestClient restClient) {
    return new PokeApiCatalogAdapter(
        new PokeApiClient(restClient), Executors.newVirtualThreadPerTaskExecutor());
  }

  @Test
  void findPageReturnsSummariesWithSpriteCategoryWeightAndAbilities() {
    stubCatalogPage("0", "20");
    stubPokemonDetail();
    stubSpecies();

    PageResult<PokemonSummary> page = adapter.findPage(new PageRequest(0, 20));

    assertThat(page.content()).hasSize(20);
    assertThat(page.totalElements()).isEqualTo(1351L);
    PokemonSummary first = page.content().get(0);
    assertThat(first.id()).isEqualTo(1);
    assertThat(first.name()).isEqualTo("bulbasaur");
    assertThat(first.spriteUrl()).isNotBlank();
    assertThat(first.category()).isEqualTo("Seed Pokémon");
    assertThat(first.weight()).isEqualTo(69);
    assertThat(first.abilities()).extracting(Ability::name).contains("overgrow");
  }

  @Test
  void findPageConvertsPageAndSizeToOffsetAndLimit() {
    stubCatalogPage("20", "10");
    stubPokemonDetail();
    stubSpecies();

    adapter.findPage(new PageRequest(2, 10));

    wm.verify(
        getRequestedFor(urlPathEqualTo("/api/v2/pokemon"))
            .withQueryParam("offset", equalTo("20"))
            .withQueryParam("limit", equalTo("10")));
  }

  @Test
  void findDetailCombinesPokemonSpeciesAndEvolutionChain() {
    stubFixture("/api/v2/pokemon/1", "pokemon-1.json");
    stubFixture("/api/v2/pokemon-species/bulbasaur", "pokemon-species-1.json");
    stubFixture("/api/v2/evolution-chain/1", "evolution-chain-1.json");

    Optional<PokemonDetail> result = adapter.findDetail("1", LANGUAGES);

    assertThat(result).isPresent();
    PokemonDetail detail = result.get();
    assertThat(detail.name()).isEqualTo("bulbasaur");
    assertThat(detail.height()).isEqualTo(7);
    assertThat(detail.weight()).isEqualTo(69);
    assertThat(detail.imageUrl()).contains("official-artwork");
    assertThat(detail.types())
        .extracting(t -> t.name() + ":" + t.slot())
        .containsExactly("grass:1", "poison:2");
    assertThat(detail.stats())
        .anySatisfy(
            s -> {
              assertThat(s.name()).isEqualTo("hp");
              assertThat(s.baseValue()).isEqualTo(45);
            });
    assertThat(detail.description()).isNotBlank();
    assertThat(detail.description()).doesNotContain("\n", "\f");
    assertThat(detail.evolutionChain().stages())
        .extracting(s -> s.speciesName())
        .containsExactly("bulbasaur", "ivysaur", "venusaur");
  }

  @Test
  void findDetailReturnsEmptyWhenPokemonDoesNotExist() {
    wm.stubFor(get(urlPathEqualTo("/api/v2/pokemon/missingno")).willReturn(status(404)));

    assertThat(adapter.findDetail("missingno", LANGUAGES)).isEmpty();
  }

  @Test
  void serverErrorBecomesExternalServiceException() {
    wm.stubFor(get(urlPathEqualTo("/api/v2/pokemon/1")).willReturn(serverError()));

    assertThatThrownBy(() -> adapter.findDetail("1", LANGUAGES))
        .isInstanceOf(ExternalServiceException.class);
  }

  @Test
  void timeoutBecomesExternalServiceException() {
    wm.stubFor(
        get(urlPathEqualTo("/api/v2/pokemon/1")).willReturn(okJson("{}").withFixedDelay(1000)));
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setReadTimeout(200);
    PokeApiCatalogAdapter slow =
        adapterWith(
            RestClient.builder().baseUrl(wm.baseUrl() + "/api/v2").requestFactory(factory).build());

    assertThatThrownBy(() -> slow.findDetail("1", LANGUAGES))
        .isInstanceOf(ExternalServiceException.class);
  }

  @Test
  void missingOptionalFieldsAreMappedToEmptyValues() {
    wm.stubFor(
        get(urlPathEqualTo("/api/v2/pokemon/999"))
            .willReturn(
                okJson(
                    """
                                        {"id":999,"name":"fakemon",
                                         "species":{"name":"fakemon","url":"https://pokeapi.co/api/v2/pokemon-species/999/"}}
                                        """)));
    wm.stubFor(
        get(urlPathEqualTo("/api/v2/pokemon-species/fakemon"))
            .willReturn(
                okJson(
                    """
                                        {"id":999,"name":"fakemon",
                                         "evolution_chain":{"url":"https://pokeapi.co/api/v2/evolution-chain/999/"}}
                                        """)));
    wm.stubFor(
        get(urlPathEqualTo("/api/v2/evolution-chain/999"))
            .willReturn(
                okJson(
                    """
                                        {"id":999,"chain":{"species":{"name":"fakemon",
                                         "url":"https://pokeapi.co/api/v2/pokemon-species/999/"},
                                         "evolves_to":[]}}
                                        """)));

    PokemonDetail detail = adapter.findDetail("999", LANGUAGES).orElseThrow();

    assertThat(detail.types()).isEmpty();
    assertThat(detail.stats()).isEmpty();
    assertThat(detail.description()).isEmpty();
    assertThat(detail.imageUrl()).isNull();
    assertThat(detail.evolutionChain().stages()).hasSize(1);
  }

  private void stubCatalogPage(String offset, String limit) {
    wm.stubFor(
        get(urlPathEqualTo("/api/v2/pokemon"))
            .withQueryParam("offset", equalTo(offset))
            .withQueryParam("limit", equalTo(limit))
            .willReturn(okJson(TestFixtures.read("pokemon-list-offset0-limit20.json"))));
  }

  private void stubPokemonDetail() {
    wm.stubFor(
        get(urlPathMatching("/api/v2/pokemon/\\d+"))
            .willReturn(okJson(TestFixtures.read("pokemon-1.json"))));
  }

  private void stubSpecies() {
    wm.stubFor(
        get(urlPathMatching("/api/v2/pokemon-species/\\d+"))
            .willReturn(okJson(TestFixtures.read("pokemon-species-1.json"))));
  }

  private void stubFixture(String path, String fixture) {
    wm.stubFor(get(urlPathEqualTo(path)).willReturn(okJson(TestFixtures.read(fixture))));
  }
}
