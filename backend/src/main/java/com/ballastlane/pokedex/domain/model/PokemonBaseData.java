package com.ballastlane.pokedex.domain.model;

import java.util.List;

/**
 * Data copied from PokeAPI into a {@link LocalPokemon}. Own fields are not part of it, so a sync
 * can never overwrite them.
 */
public record PokemonBaseData(
    String name,
    String spriteUrl,
    String category,
    Integer height,
    Integer weight,
    List<PokemonType> types,
    List<Ability> abilities) {}
