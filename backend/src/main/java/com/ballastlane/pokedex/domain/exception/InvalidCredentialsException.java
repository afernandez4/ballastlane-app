package com.ballastlane.pokedex.domain.exception;

/** Login failed. The message never reveals whether the username or the password was wrong. */
public class InvalidCredentialsException extends DomainException {

  public InvalidCredentialsException() {
    super("Invalid username or password");
  }
}
