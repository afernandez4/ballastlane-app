package com.ballastlane.pokedex.domain.exception;

public class DuplicateResourceException extends DomainException {

  public DuplicateResourceException(String resource, String field, Object value) {
    super(resource + " already exists with " + field + " = " + value);
  }
}
