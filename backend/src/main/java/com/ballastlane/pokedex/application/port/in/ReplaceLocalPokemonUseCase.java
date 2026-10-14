package com.ballastlane.pokedex.application.port.in;

import com.ballastlane.pokedex.domain.model.LocalPokemon;
import com.ballastlane.pokedex.domain.model.LocalPokemonFields;

/** PUT: replaces every user-editable field. */
public interface ReplaceLocalPokemonUseCase {

  /**
   * @param expectedVersion version the client read (from If-Match), or null to skip the check
   * @throws com.ballastlane.pokedex.domain.exception.ConcurrentModificationException on version
   *     mismatch
   */
  LocalPokemon replace(long id, LocalPokemonFields fields, Long expectedVersion);
}
