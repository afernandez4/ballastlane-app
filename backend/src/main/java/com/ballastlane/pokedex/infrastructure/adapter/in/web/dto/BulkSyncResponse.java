package com.ballastlane.pokedex.infrastructure.adapter.in.web.dto;

import com.ballastlane.pokedex.application.port.in.SyncPokemonUseCase.BulkSyncResult;
import com.ballastlane.pokedex.application.port.in.SyncPokemonUseCase.FailedSync;
import java.util.List;

public record BulkSyncResponse(int synced, int updated, List<Failure> failed) {

  public static BulkSyncResponse from(BulkSyncResult result) {
    return new BulkSyncResponse(
        result.synced(), result.updated(), result.failed().stream().map(Failure::from).toList());
  }

  public record Failure(int id, String reason) {

    static Failure from(FailedSync failed) {
      return new Failure(failed.id(), failed.reason());
    }
  }
}
