package com.ballastlane.pokedex.application.port.in;

import com.ballastlane.pokedex.domain.model.PokemonDetail;

/** US02: detailed view of one Pokémon, looked up by id or by name. */
public interface GetPokemonDetailUseCase {

  PokemonDetail get(String idOrName);
}
