package com.ballastlane.pokedex.domain.model;

/** A base stat of a Pokémon, e.g. {@code hp = 45}. */
public record Stat(String name, int baseValue) {}
