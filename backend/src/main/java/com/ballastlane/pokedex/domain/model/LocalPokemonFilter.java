package com.ballastlane.pokedex.domain.model;

import java.util.Locale;

/**
 * Search criteria for local Pokémon. A blank value means "no filter on this field". Values are
 * trimmed and lowercased so that matching is case-insensitive.
 */
public record LocalPokemonFilter(String name, String region, String tag) {

  public LocalPokemonFilter {
    name = normalize(name);
    region = normalize(region);
    tag = normalize(tag);
  }

  private static String normalize(String value) {
    return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
  }
}
