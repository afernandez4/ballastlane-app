package com.ballastlane.pokedex.domain.exception;

/** Base class for business rule violations. Translated to HTTP responses by the web adapter. */
public abstract class DomainException extends RuntimeException {

  protected DomainException(String message) {
    super(message);
  }

  protected DomainException(String message, Throwable cause) {
    super(message, cause);
  }
}
