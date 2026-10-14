package com.ballastlane.pokedex.application.usecase;

import com.ballastlane.pokedex.application.port.in.GetPokemonDetailUseCase;
import com.ballastlane.pokedex.application.port.out.PokemonCatalogPort;
import com.ballastlane.pokedex.domain.exception.PokemonNotFoundException;
import com.ballastlane.pokedex.domain.model.PokemonDetail;
import java.util.List;
import java.util.Locale;

/** US02: looks a Pokémon up by id or name and asks for the description in Spanish, then English. */
public class GetPokemonDetailService implements GetPokemonDetailUseCase {

  private static final List<String> PREFERRED_LANGUAGES = List.of("en");

  private final PokemonCatalogPort catalog;

  public GetPokemonDetailService(PokemonCatalogPort catalog) {
    this.catalog = catalog;
  }

  @Override
  public PokemonDetail get(String idOrName) {
    String key = normalize(idOrName);
    if (key.isEmpty()) {
      throw new PokemonNotFoundException(String.valueOf(idOrName));
    }
    return catalog
        .findDetail(key, PREFERRED_LANGUAGES)
        .orElseThrow(() -> new PokemonNotFoundException(idOrName));
  }

  private static String normalize(String idOrName) {
    return idOrName == null ? "" : idOrName.trim().toLowerCase(Locale.ROOT);
  }
}
