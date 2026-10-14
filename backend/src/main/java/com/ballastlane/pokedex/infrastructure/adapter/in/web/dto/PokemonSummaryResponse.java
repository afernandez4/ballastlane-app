package com.ballastlane.pokedex.infrastructure.adapter.in.web.dto;

import com.ballastlane.pokedex.domain.model.Ability;
import com.ballastlane.pokedex.domain.model.PokemonSummary;
import java.util.List;

public record PokemonSummaryResponse(
    int id,
    String name,
    String spriteUrl,
    String category,
    int weight,
    List<AbilityResponse> abilities) {

  public static PokemonSummaryResponse from(PokemonSummary summary) {
    return new PokemonSummaryResponse(
        summary.id(),
        summary.name(),
        summary.spriteUrl(),
        summary.category(),
        summary.weight(),
        summary.abilities().stream().map(AbilityResponse::from).toList());
  }

  public record AbilityResponse(String name, boolean hidden) {

    static AbilityResponse from(Ability ability) {
      return new AbilityResponse(ability.name(), ability.hidden());
    }
  }
}
