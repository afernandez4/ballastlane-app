package com.ballastlane.pokedex.domain.model;

import java.util.List;

/** Full view of a Pokémon, used by the detail endpoint (US02). */
public record PokemonDetail(
    int id,
    String name,
    String imageUrl,
    int height,
    int weight,
    List<PokemonType> types,
    List<Stat> stats,
    String description,
    EvolutionChain evolutionChain,
    String category,
    List<Ability> abilities) {

  public PokemonDetail {
    types = types == null ? List.of() : List.copyOf(types);
    stats = stats == null ? List.of() : List.copyOf(stats);
    abilities = abilities == null ? List.of() : List.copyOf(abilities);
  }

  /** Detail without category and abilities (used by the detail endpoint only). */
  public PokemonDetail(
      int id,
      String name,
      String imageUrl,
      int height,
      int weight,
      List<PokemonType> types,
      List<Stat> stats,
      String description,
      EvolutionChain evolutionChain) {
    this(
        id,
        name,
        imageUrl,
        height,
        weight,
        types,
        stats,
        description,
        evolutionChain,
        "",
        List.of());
  }
}
