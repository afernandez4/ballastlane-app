package com.ballastlane.pokedex.domain.exception;

/** PokeAPI is unavailable, timed out or returned an unexpected error. */
public class ExternalServiceException extends DomainException {

  public ExternalServiceException(String message, Throwable cause) {
    super(message, cause);
  }

  public ExternalServiceException(String message) {
    super(message);
  }
}
