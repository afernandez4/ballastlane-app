package com.ballastlane.pokedex.domain.exception;

/** The resource was modified by someone else since the version the client read. */
public class ConcurrentModificationException extends DomainException {

  public ConcurrentModificationException(Long id) {
    super("Pokémon " + id + " was modified concurrently; reload and retry");
  }
}
