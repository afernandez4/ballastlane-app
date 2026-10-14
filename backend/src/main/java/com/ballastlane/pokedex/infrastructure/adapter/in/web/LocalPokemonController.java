package com.ballastlane.pokedex.infrastructure.adapter.in.web;

import com.ballastlane.pokedex.application.port.in.SyncPokemonUseCase;
import com.ballastlane.pokedex.application.port.in.SyncPokemonUseCase.SyncOutcome;
import com.ballastlane.pokedex.infrastructure.adapter.in.web.dto.BulkSyncResponse;
import com.ballastlane.pokedex.infrastructure.adapter.in.web.dto.LocalPokemonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Local Pokémon store. Sync endpoints first; CRUD endpoints are added in US04. */
@RestController
@RequestMapping("/api/v1/pokemon")
@Tag(name = "Local Pokémon", description = "Pokémon stored in the local database")
class LocalPokemonController {

  private final SyncPokemonUseCase sync;

  LocalPokemonController(SyncPokemonUseCase sync) {
    this.sync = sync;
  }

  @PostMapping("/sync/{idOrName}")
  @Operation(
      summary = "Sync one Pokémon from PokeAPI",
      description =
          "Creates the local row (201) or refreshes its base data (200). Own fields are kept.")
  @ApiResponse(responseCode = "201", description = "Created")
  @ApiResponse(responseCode = "200", description = "Already existed, base data refreshed")
  @ApiResponse(responseCode = "404", description = "Pokémon does not exist in PokeAPI")
  ResponseEntity<LocalPokemonResponse> syncOne(@PathVariable String idOrName) {
    SyncOutcome outcome = sync.sync(idOrName);
    LocalPokemonResponse body = LocalPokemonResponse.from(outcome.pokemon());
    if (outcome.created()) {
      return ResponseEntity.created(URI.create("/api/v1/pokemon/" + outcome.pokemon().id()))
          .body(body);
    }
    return ResponseEntity.ok(body);
  }

  @PostMapping("/sync")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
      summary = "Sync a range of Pokémon ids (ADMIN)",
      description = "Syncs ids from 'from' to 'to' inclusive, at most 151 per call.")
  @ApiResponse(responseCode = "200", description = "Summary with failed ids")
  @ApiResponse(responseCode = "400", description = "Invalid range")
  @ApiResponse(responseCode = "403", description = "Not an administrator")
  BulkSyncResponse syncRange(@RequestParam int from, @RequestParam int to) {
    return BulkSyncResponse.from(sync.syncRange(from, to));
  }
}
