package com.ballastlane.pokedex.application.port.in;

public interface DeleteLocalPokemonUseCase {

  /**
   * @throws com.ballastlane.pokedex.domain.exception.PokemonNotFoundException if unknown
   */
  void delete(long id);
}
