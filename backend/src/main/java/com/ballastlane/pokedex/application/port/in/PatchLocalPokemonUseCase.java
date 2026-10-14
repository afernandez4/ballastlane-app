package com.ballastlane.pokedex.application.port.in;

import com.ballastlane.pokedex.domain.model.LocalPokemon;
import com.ballastlane.pokedex.domain.model.LocalPokemonPatch;

/** PATCH: updates only the fields the client sent. */
public interface PatchLocalPokemonUseCase {

  LocalPokemon patch(long id, LocalPokemonPatch patch);
}
