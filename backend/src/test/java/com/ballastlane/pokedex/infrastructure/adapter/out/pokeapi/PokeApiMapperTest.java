package com.ballastlane.pokedex.infrastructure.adapter.out.pokeapi;

import static org.assertj.core.api.Assertions.assertThat;

import com.ballastlane.pokedex.domain.model.EvolutionChain;
import com.ballastlane.pokedex.infrastructure.adapter.out.pokeapi.PokeApiDtos.FlavorText;
import com.ballastlane.pokedex.infrastructure.adapter.out.pokeapi.PokeApiDtos.NamedResource;
import com.ballastlane.pokedex.infrastructure.adapter.out.pokeapi.PokeApiDtos.SpeciesResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

class PokeApiMapperTest {

  private static final List<String> LANGUAGES = List.of("es", "en");

  @Test
  void descriptionUsesFirstPreferredLanguageAvailable() {
    SpeciesResponse species = species(flavor("en", "English text"), flavor("es", "Spanish text"));

    assertThat(PokeApiMapper.description(species, LANGUAGES)).isEqualTo("Spanish text");
  }

  @Test
  void descriptionFallsBackToEnglishWhenSpanishIsMissing() {
    SpeciesResponse species = species(flavor("en", "English text"));

    assertThat(PokeApiMapper.description(species, LANGUAGES)).isEqualTo("English text");
  }

  @Test
  void descriptionIsEmptyWhenNoPreferredLanguageMatches() {
    SpeciesResponse species = species(flavor("fr", "Texte"));

    assertThat(PokeApiMapper.description(species, LANGUAGES)).isEmpty();
  }

  @Test
  void descriptionRemovesLineBreaksAndFormFeeds() {
    SpeciesResponse species = species(flavor("en", "Line one\nline two\fthree"));

    assertThat(PokeApiMapper.description(species, LANGUAGES)).isEqualTo("Line one line two three");
  }

  @Test
  void flattensEeveeBranchesFromRealFixture() throws Exception {
    PokeApiDtos.EvolutionChainResponse response =
        new ObjectMapper()
            .readValue(
                TestFixtures.read("evolution-chain-67.json"),
                PokeApiDtos.EvolutionChainResponse.class);

    EvolutionChain chain = PokeApiMapper.toEvolutionChain(response);

    assertThat(chain.stages()).hasSize(9);
    assertThat(chain.stages().get(0).speciesName()).isEqualTo("eevee");
    assertThat(chain.stages().get(0).stage()).isZero();
    assertThat(chain.stages().subList(1, 9)).allSatisfy(s -> assertThat(s.stage()).isEqualTo(1));
  }

  private static SpeciesResponse species(FlavorText... entries) {
    return new SpeciesResponse(1, "bulbasaur", List.of(), List.of(entries), null);
  }

  private static FlavorText flavor(String language, String text) {
    return new FlavorText(text, new NamedResource(language, null));
  }
}
