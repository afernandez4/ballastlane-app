package com.ballastlane.pokedex.domain.exception;

import java.util.List;

/** Registration data broke one or more rules (username, email or password). */
public class InvalidUserDataException extends DomainException {

  /** One rule violation: the offending field and why it was rejected. */
  public record FieldViolation(String field, String message) {}

  private final List<FieldViolation> violations;

  public InvalidUserDataException(List<FieldViolation> violations) {
    super("Invalid user data: " + violations.size() + " violation(s)");
    this.violations = List.copyOf(violations);
  }

  public List<FieldViolation> getViolations() {
    return violations;
  }
}
