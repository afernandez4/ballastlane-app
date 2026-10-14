package com.ballastlane.pokedex.infrastructure.adapter.in.web;

import com.ballastlane.pokedex.application.port.in.CreateLocalPokemonUseCase;
import com.ballastlane.pokedex.application.port.in.DeleteLocalPokemonUseCase;
import com.ballastlane.pokedex.application.port.in.GetLocalPokemonUseCase;
import com.ballastlane.pokedex.application.port.in.ListLocalPokemonUseCase;
import com.ballastlane.pokedex.application.port.in.PatchLocalPokemonUseCase;
import com.ballastlane.pokedex.application.port.in.ReplaceLocalPokemonUseCase;
import com.ballastlane.pokedex.domain.model.LocalPokemon;
import com.ballastlane.pokedex.domain.model.LocalPokemonFilter;
import com.ballastlane.pokedex.infrastructure.adapter.in.web.dto.LocalPokemonCreateRequest;
import com.ballastlane.pokedex.infrastructure.adapter.in.web.dto.LocalPokemonPatchRequest;
import com.ballastlane.pokedex.infrastructure.adapter.in.web.dto.LocalPokemonReplaceRequest;
import com.ballastlane.pokedex.infrastructure.adapter.in.web.dto.LocalPokemonResponse;
import com.ballastlane.pokedex.infrastructure.adapter.in.web.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Local CRUD (US04). Reads are public; writes need a token; DELETE needs ADMIN. */
@RestController
@RequestMapping("/api/v1/pokemon")
@Tag(name = "Local Pokémon", description = "Pokémon stored in the local database")
class LocalPokemonCrudController {

  private final ListLocalPokemonUseCase list;
  private final GetLocalPokemonUseCase get;
  private final CreateLocalPokemonUseCase create;
  private final ReplaceLocalPokemonUseCase replace;
  private final PatchLocalPokemonUseCase patch;
  private final DeleteLocalPokemonUseCase delete;

  LocalPokemonCrudController(
      ListLocalPokemonUseCase list,
      GetLocalPokemonUseCase get,
      CreateLocalPokemonUseCase create,
      ReplaceLocalPokemonUseCase replace,
      PatchLocalPokemonUseCase patch,
      DeleteLocalPokemonUseCase delete) {
    this.list = list;
    this.get = get;
    this.create = create;
    this.replace = replace;
    this.patch = patch;
    this.delete = delete;
  }

  @GetMapping
  @Operation(summary = "List local Pokémon with optional filters")
  PageResponse<LocalPokemonResponse> list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String region,
      @RequestParam(required = false) String tag) {
    return PageResponse.from(
        list.list(new LocalPokemonFilter(name, region, tag), page, size),
        LocalPokemonResponse::from);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a local Pokémon by id")
  @ApiResponse(responseCode = "404", description = "Not stored locally")
  ResponseEntity<LocalPokemonResponse> get(@PathVariable long id) {
    return withEtag(get.get(id));
  }

  @PostMapping
  @Operation(summary = "Create a local Pokémon for an existing PokeAPI id")
  @ApiResponse(responseCode = "201", description = "Created")
  @ApiResponse(responseCode = "409", description = "pokeApiId already stored")
  ResponseEntity<LocalPokemonResponse> create(@Valid @RequestBody LocalPokemonCreateRequest body) {
    LocalPokemon created = create.create(body.pokeApiId(), body.toFields());
    return ResponseEntity.created(URI.create("/api/v1/pokemon/" + created.id()))
        .eTag(etag(created))
        .body(LocalPokemonResponse.from(created));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Replace the editable fields of a local Pokémon")
  @ApiResponse(responseCode = "409", description = "If-Match version does not match")
  ResponseEntity<LocalPokemonResponse> replace(
      @PathVariable long id,
      @RequestHeader(value = "If-Match", required = false) String ifMatch,
      @Valid @RequestBody LocalPokemonReplaceRequest body) {
    return withEtag(replace.replace(id, body.toFields(), parseVersion(ifMatch)));
  }

  @PatchMapping("/{id}")
  @Operation(summary = "Partially update the editable fields of a local Pokémon")
  ResponseEntity<LocalPokemonResponse> patch(
      @PathVariable long id, @RequestBody LocalPokemonPatchRequest body) {
    return withEtag(patch.patch(id, body.toPatch()));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Delete a local Pokémon (ADMIN)")
  @ApiResponse(responseCode = "204", description = "Deleted")
  @ApiResponse(responseCode = "403", description = "Not an administrator")
  ResponseEntity<Void> delete(@PathVariable long id) {
    delete.delete(id);
    return ResponseEntity.noContent().build();
  }

  private static ResponseEntity<LocalPokemonResponse> withEtag(LocalPokemon pokemon) {
    return ResponseEntity.ok().eTag(etag(pokemon)).body(LocalPokemonResponse.from(pokemon));
  }

  private static String etag(LocalPokemon pokemon) {
    return "\"" + pokemon.version() + "\"";
  }

  /** Parses {@code If-Match: "3"} into 3. Absent header means no version check. */
  private static Long parseVersion(String ifMatch) {
    if (ifMatch == null || ifMatch.isBlank()) {
      return null;
    }
    try {
      return Long.parseLong(ifMatch.replace("\"", "").trim());
    } catch (NumberFormatException e) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "If-Match must be a version number");
    }
  }
}
