package com.ballastlane.pokedex.infrastructure.adapter.in.web.dto;

import com.ballastlane.pokedex.domain.model.EvolutionStage;
import com.ballastlane.pokedex.domain.model.PokemonDetail;
import com.ballastlane.pokedex.domain.model.PokemonType;
import com.ballastlane.pokedex.domain.model.Stat;
import java.util.List;

public record PokemonDetailResponse(
    int id,
    String name,
    String imageUrl,
    int height,
    int weight,
    List<TypeResponse> types,
    List<StatResponse> stats,
    String description,
    List<EvolutionStageResponse> evolutionChain) {

  public static PokemonDetailResponse from(PokemonDetail detail) {
    return new PokemonDetailResponse(
        detail.id(),
        detail.name(),
        detail.imageUrl(),
        detail.height(),
        detail.weight(),
        detail.types().stream().map(TypeResponse::from).toList(),
        detail.stats().stream().map(StatResponse::from).toList(),
        detail.description(),
        detail.evolutionChain().stages().stream().map(EvolutionStageResponse::from).toList());
  }

  public record TypeResponse(String name, int slot) {

    static TypeResponse from(PokemonType type) {
      return new TypeResponse(type.name(), type.slot());
    }
  }

  public record StatResponse(String name, int baseValue) {

    static StatResponse from(Stat stat) {
      return new StatResponse(stat.name(), stat.baseValue());
    }
  }

  public record EvolutionStageResponse(
      String speciesName, int pokeApiId, int stage, Integer minLevel, String trigger) {

    static EvolutionStageResponse from(EvolutionStage stage) {
      return new EvolutionStageResponse(
          stage.speciesName(), stage.pokeApiId(), stage.stage(), stage.minLevel(), stage.trigger());
    }
  }
}
