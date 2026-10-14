package com.ballastlane.pokedex.application.port.in;

import com.ballastlane.pokedex.domain.model.LocalPokemon;

public interface GetLocalPokemonUseCase {

  /**
   * @throws com.ballastlane.pokedex.domain.exception.PokemonNotFoundException if unknown
   */
  LocalPokemon get(long id);
}
