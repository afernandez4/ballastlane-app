package com.ballastlane.pokedex.infrastructure.adapter.in.web.dto;

import com.ballastlane.pokedex.domain.model.Ability;
import com.ballastlane.pokedex.domain.model.LocalPokemon;
import com.ballastlane.pokedex.domain.model.PokemonType;
import java.time.Instant;
import java.util.List;

public record LocalPokemonResponse(
    Long id,
    int pokeApiId,
    String name,
    String spriteUrl,
    String category,
    Integer height,
    Integer weight,
    List<TypeInfo> types,
    List<AbilityInfo> abilities,
    String localName,
    String region,
    List<String> tags,
    long version,
    Instant createdAt,
    Instant updatedAt) {

  public static LocalPokemonResponse from(LocalPokemon pokemon) {
    return new LocalPokemonResponse(
        pokemon.id(),
        pokemon.pokeApiId(),
        pokemon.name(),
        pokemon.spriteUrl(),
        pokemon.category(),
        pokemon.height(),
        pokemon.weight(),
        pokemon.types().stream().map(TypeInfo::from).toList(),
        pokemon.abilities().stream().map(AbilityInfo::from).toList(),
        pokemon.localName(),
        pokemon.region(),
        pokemon.tags(),
        pokemon.version(),
        pokemon.createdAt(),
        pokemon.updatedAt());
  }

  public record TypeInfo(String name, int slot) {

    static TypeInfo from(PokemonType type) {
      return new TypeInfo(type.name(), type.slot());
    }
  }

  public record AbilityInfo(String name, boolean hidden) {

    static AbilityInfo from(Ability ability) {
      return new AbilityInfo(ability.name(), ability.hidden());
    }
  }
}
