package com.ballastlane.pokedex.application.port.out;

import com.ballastlane.pokedex.domain.model.PageRequest;
import com.ballastlane.pokedex.domain.model.PageResult;
import com.ballastlane.pokedex.domain.model.PokemonDetail;
import com.ballastlane.pokedex.domain.model.PokemonSummary;
import java.util.List;
import java.util.Optional;

/** Outbound port to the external Pokémon catalogue (PokeAPI). */
public interface PokemonCatalogPort {

  PageResult<PokemonSummary> findPage(PageRequest request);

  /**
   * Full detail (species, evolution chain, description). The adapter picks the description using
   * {@code preferredLanguages} in order and leaves it empty when none matches.
   *
   * @return empty when the Pokémon does not exist
   */
  Optional<PokemonDetail> findDetail(String normalizedIdOrName, List<String> preferredLanguages);
}
