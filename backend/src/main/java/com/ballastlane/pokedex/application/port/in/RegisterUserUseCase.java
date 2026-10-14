package com.ballastlane.pokedex.application.port.in;

import com.ballastlane.pokedex.domain.model.User;

public interface RegisterUserUseCase {

  /**
   * @throws com.ballastlane.pokedex.domain.exception.InvalidUserDataException if the data breaks a
   *     rule
   * @throws com.ballastlane.pokedex.domain.exception.DuplicateResourceException if the username or
   *     email is taken
   */
  User register(RegisterCommand command);

  record RegisterCommand(String username, String email, String password) {}
}
