package com.ballastlane.pokedex.application.port.in;

import com.ballastlane.pokedex.domain.model.IssuedToken;

public interface LoginUseCase {

  /**
   * @throws com.ballastlane.pokedex.domain.exception.InvalidCredentialsException on any mismatch
   */
  IssuedToken login(String username, String password);
}
