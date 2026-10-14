package com.ballastlane.pokedex.application.port.in;

import com.ballastlane.pokedex.domain.model.User;

public interface CurrentUserUseCase {

  /**
   * @throws com.ballastlane.pokedex.domain.exception.UserNotFoundException if unknown
   */
  User current(String username);
}
