package com.ballastlane.pokedex.domain.exception;

import java.util.List;

/** Local Pokémon data broke one or more validation rules. Carries every violation found. */
public class InvalidPokemonDataException extends DomainException {

  /** One rule violation: the offending field and why it was rejected. */
  public record FieldViolation(String field, String message) {}

  private final List<FieldViolation> violations;

  public InvalidPokemonDataException(List<FieldViolation> violations) {
    super("Invalid Pokémon data: " + violations.size() + " violation(s)");
    this.violations = List.copyOf(violations);
  }

  public List<FieldViolation> getViolations() {
    return violations;
  }
}
