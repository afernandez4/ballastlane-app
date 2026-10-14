package com.ballastlane.pokedex.application.port.out;

import com.ballastlane.pokedex.domain.model.AuthenticatedUser;
import com.ballastlane.pokedex.domain.model.IssuedToken;
import com.ballastlane.pokedex.domain.model.User;
import java.util.Optional;

public interface TokenProviderPort {

  IssuedToken issue(User user);

  /** Empty when the token is malformed, tampered with, or expired. */
  Optional<AuthenticatedUser> verify(String token);
}
