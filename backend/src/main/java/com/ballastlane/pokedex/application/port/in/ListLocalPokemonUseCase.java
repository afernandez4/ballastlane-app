package com.ballastlane.pokedex.application.port.in;

import com.ballastlane.pokedex.domain.model.LocalPokemon;
import com.ballastlane.pokedex.domain.model.LocalPokemonFilter;
import com.ballastlane.pokedex.domain.model.PageResult;

/** US04: paginated list of local Pokémon with optional name, region and tag filters. */
public interface ListLocalPokemonUseCase {

  PageResult<LocalPokemon> list(LocalPokemonFilter filter, int page, int size);
}
