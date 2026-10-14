package com.ballastlane.pokedex.domain.model;

/** An ability a Pokémon can have. {@code hidden} marks hidden abilities. */
public record Ability(String name, boolean hidden) {}
