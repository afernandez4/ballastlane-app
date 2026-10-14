package com.ballastlane.pokedex.domain.exception;

public class PokemonNotFoundException extends DomainException {

  public PokemonNotFoundException(String identifier) {
    super("Pokémon not found: " + identifier);
  }
}
