package com.ballastlane.pokedex.domain.model;

import java.util.List;

/** Compact view of a Pokémon used in the paginated catalogue (US01). */
public record PokemonSummary(
    int id, String name, String spriteUrl, String category, int weight, List<Ability> abilities) {

  public PokemonSummary {
    abilities = abilities == null ? List.of() : List.copyOf(abilities);
  }
}
