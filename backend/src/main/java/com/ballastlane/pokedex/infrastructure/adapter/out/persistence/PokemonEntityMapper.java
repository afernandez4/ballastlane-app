package com.ballastlane.pokedex.infrastructure.adapter.out.persistence;

import com.ballastlane.pokedex.domain.model.Ability;
import com.ballastlane.pokedex.domain.model.LocalPokemon;
import com.ballastlane.pokedex.domain.model.PokemonType;
import java.util.List;

/** Converts between the JPA entity and the domain model. Not a Spring bean; pure functions. */
final class PokemonEntityMapper {

  private PokemonEntityMapper() {}

  static LocalPokemon toDomain(PokemonEntity entity) {
    return new LocalPokemon(
        entity.getId(),
        entity.getPokeApiId(),
        entity.getName(),
        entity.getSpriteUrl(),
        entity.getCategory(),
        entity.getHeight(),
        entity.getWeight(),
        entity.getTypes().stream().map(t -> new PokemonType(t.getName(), t.getSlot())).toList(),
        entity.getAbilities().stream().map(a -> new Ability(a.getName(), a.isHidden())).toList(),
        entity.getLocalName(),
        entity.getRegion(),
        List.copyOf(entity.getTags()),
        entity.getVersion(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  /**
   * Copies the domain state into the entity. Collections are edited in place so Hibernate tracks
   * the changes. The version is managed by JPA and never copied.
   */
  static void apply(PokemonEntity entity, LocalPokemon pokemon) {
    entity.setPokeApiId(pokemon.pokeApiId());
    entity.setName(pokemon.name());
    entity.setSpriteUrl(pokemon.spriteUrl());
    entity.setCategory(pokemon.category());
    entity.setHeight(pokemon.height());
    entity.setWeight(pokemon.weight());
    entity.setLocalName(pokemon.localName());
    entity.setRegion(pokemon.region());
    entity.setCreatedAt(pokemon.createdAt());
    entity.setUpdatedAt(pokemon.updatedAt());

    entity.getTypes().clear();
    pokemon.types().forEach(t -> entity.getTypes().add(new TypeEmbeddable(t.name(), t.slot())));

    entity.getAbilities().clear();
    pokemon
        .abilities()
        .forEach(a -> entity.getAbilities().add(new AbilityEmbeddable(a.name(), a.hidden())));

    entity.getTags().clear();
    entity.getTags().addAll(pokemon.tags());
  }
}
