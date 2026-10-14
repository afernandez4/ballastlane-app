package com.ballastlane.pokedex.application.port.out;

import com.ballastlane.pokedex.domain.model.LocalPokemon;
import com.ballastlane.pokedex.domain.model.LocalPokemonFilter;
import com.ballastlane.pokedex.domain.model.PageRequest;
import com.ballastlane.pokedex.domain.model.PageResult;
import java.util.Optional;

/** Outbound port for locally stored Pokémon. */
public interface PokemonRepositoryPort {

  Optional<LocalPokemon> findById(long id);

  Optional<LocalPokemon> findByPokeApiId(int pokeApiId);

  PageResult<LocalPokemon> search(LocalPokemonFilter filter, PageRequest page);

  /**
   * Inserts when {@code id} is null, otherwise replaces the stored fields of that row.
   *
   * @throws com.ballastlane.pokedex.domain.exception.DuplicateResourceException if pokeApiId
   *     already exists
   */
  LocalPokemon save(LocalPokemon pokemon);

  void deleteById(long id);
}
