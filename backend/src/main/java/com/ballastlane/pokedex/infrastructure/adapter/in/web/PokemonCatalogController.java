package com.ballastlane.pokedex.infrastructure.adapter.in.web;

import com.ballastlane.pokedex.application.port.in.GetPokemonDetailUseCase;
import com.ballastlane.pokedex.application.port.in.ListPokemonUseCase;
import com.ballastlane.pokedex.infrastructure.adapter.in.web.dto.PageResponse;
import com.ballastlane.pokedex.infrastructure.adapter.in.web.dto.PokemonDetailResponse;
import com.ballastlane.pokedex.infrastructure.adapter.in.web.dto.PokemonSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Read-only catalogue backed by PokeAPI (US01, US02). Depends on input ports only. */
@RestController
@RequestMapping("/api/v1/pokeapi")
@Tag(name = "Pokédex catalogue", description = "Pokémon data read from PokeAPI")
class PokemonCatalogController {

  private final ListPokemonUseCase listPokemon;
  private final GetPokemonDetailUseCase getDetail;

  PokemonCatalogController(ListPokemonUseCase listPokemon, GetPokemonDetailUseCase getDetail) {
    this.listPokemon = listPokemon;
    this.getDetail = getDetail;
  }

  @GetMapping("/pokemon")
  @Operation(
      summary = "List Pokémon, paginated",
      description = "Zero-based page, size between 1 and 50.")
  @ApiResponse(responseCode = "200", description = "Page of Pokémon summaries")
  @ApiResponse(responseCode = "400", description = "Invalid page or size")
  @ApiResponse(responseCode = "502", description = "PokeAPI unavailable")
  PageResponse<PokemonSummaryResponse> list(
      @Parameter(description = "Zero-based page index", example = "0")
          @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "Items per page, 1 to 50", example = "20")
          @RequestParam(defaultValue = "20")
          int size) {
    return PageResponse.from(listPokemon.list(page, size), PokemonSummaryResponse::from);
  }

  @GetMapping("/pokemon/{idOrName}")
  @Operation(
      summary = "Get Pokémon detail by id or name",
      description = "Includes species description and the flattened evolution chain.")
  @ApiResponse(responseCode = "200", description = "Pokémon detail")
  @ApiResponse(responseCode = "404", description = "Pokémon does not exist")
  @ApiResponse(responseCode = "502", description = "PokeAPI unavailable")
  PokemonDetailResponse detail(
      @Parameter(description = "PokeAPI id or name", example = "pikachu")
          @PathVariable
          @Schema(example = "pikachu")
          String idOrName) {
    return PokemonDetailResponse.from(getDetail.get(idOrName));
  }
}
