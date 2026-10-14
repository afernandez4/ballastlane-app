package com.ballastlane.pokedex.application.port.in;

import com.ballastlane.pokedex.domain.model.LocalPokemon;
import java.util.List;

/** US03: copies Pokémon base data from PokeAPI into the local database. Idempotent. */
public interface SyncPokemonUseCase {

  /** Syncs one Pokémon by id or name. {@code created} is false when the row already existed. */
  SyncOutcome sync(String idOrName);

  /** Syncs the ids from {@code from} to {@code to} inclusive, at most 151 per call. */
  BulkSyncResult syncRange(int from, int to);

  record SyncOutcome(LocalPokemon pokemon, boolean created) {}

  record BulkSyncResult(int synced, int updated, List<FailedSync> failed) {

    public BulkSyncResult {
      failed = List.copyOf(failed);
    }
  }

  record FailedSync(int id, String reason) {}
}
