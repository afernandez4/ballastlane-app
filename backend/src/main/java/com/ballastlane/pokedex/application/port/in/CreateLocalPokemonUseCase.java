package com.ballastlane.pokedex.application.port.in;

import com.ballastlane.pokedex.domain.model.LocalPokemon;
import com.ballastlane.pokedex.domain.model.LocalPokemonFields;

/** Creates a local row for a Pokémon that exists in PokeAPI, with the given own fields. */
public interface CreateLocalPokemonUseCase {

  /**
   * @throws com.ballastlane.pokedex.domain.exception.DuplicateResourceException if the pokeApiId is
   *     already stored
   * @throws com.ballastlane.pokedex.domain.exception.PokemonNotFoundException if PokeAPI does not
   *     know the id
   */
  LocalPokemon create(int pokeApiId, LocalPokemonFields fields);
}
