package com.ballastlane.pokedex.domain.model;

/** An elemental type of a Pokémon, with its slot (1 = primary). */
public record PokemonType(String name, int slot) {}
