package com.ballastlane.pokedex.application.port.in;

import com.ballastlane.pokedex.domain.model.PageResult;
import com.ballastlane.pokedex.domain.model.PokemonSummary;

/** US01: paginated catalogue of Pokémon from PokeAPI. */
public interface ListPokemonUseCase {

  PageResult<PokemonSummary> list(int page, int size);
}
