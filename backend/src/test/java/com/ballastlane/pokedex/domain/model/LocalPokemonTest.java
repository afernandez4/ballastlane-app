package com.ballastlane.pokedex.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ballastlane.pokedex.domain.exception.InvalidPokemonDataException;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class LocalPokemonTest {

  private static LocalPokemon pokemon(Integer height, Integer weight) {
    return new LocalPokemon(
        1L,
        25,
        "pikachu",
        "sprite.png",
        "Mouse Pokémon",
        height,
        weight,
        List.of(new PokemonType("electric", 1)),
        List.of(new Ability("static", false)),
        "Chispito",
        "Kanto",
        List.of("starter"),
        0L,
        Instant.parse("2026-01-01T00:00:00Z"),
        Instant.parse("2026-01-01T00:00:00Z"));
  }

  @Test
  void acceptsPositiveHeightAndWeight() {
    assertThat(pokemon(4, 60).weight()).isEqualTo(60);
  }

  @Test
  void acceptsMissingHeightAndWeight() {
    assertThat(pokemon(null, null).height()).isNull();
  }

  @Test
  void rejectsZeroOrNegativeHeight() {
    assertThatThrownBy(() -> pokemon(0, 60))
        .isInstanceOfSatisfying(
            InvalidPokemonDataException.class,
            ex ->
                assertThat(ex.getViolations())
                    .extracting(InvalidPokemonDataException.FieldViolation::field)
                    .containsExactly("height"));
  }

  @Test
  void rejectsZeroOrNegativeWeight() {
    assertThatThrownBy(() -> pokemon(4, -1)).isInstanceOf(InvalidPokemonDataException.class);
  }

  @Test
  void updateChangesOnlyOwnFieldsAndKeepsPokeApiId() {
    LocalPokemon original = pokemon(4, 60);
    LocalPokemonFields fields = new LocalPokemonFields("Rayo", "Johto", List.of("fast"));

    LocalPokemon updated = original.withOwnFields(fields);

    assertThat(updated.pokeApiId()).isEqualTo(original.pokeApiId());
    assertThat(updated.name()).isEqualTo(original.name());
    assertThat(updated.weight()).isEqualTo(original.weight());
    assertThat(updated.localName()).isEqualTo("Rayo");
    assertThat(updated.region()).isEqualTo("Johto");
    assertThat(updated.tags()).containsExactly("fast");
  }
}
