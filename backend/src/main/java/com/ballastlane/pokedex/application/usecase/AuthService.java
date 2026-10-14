package com.ballastlane.pokedex.application.usecase;

import com.ballastlane.pokedex.application.port.in.CurrentUserUseCase;
import com.ballastlane.pokedex.application.port.in.LoginUseCase;
import com.ballastlane.pokedex.application.port.in.RegisterUserUseCase;
import com.ballastlane.pokedex.application.port.out.PasswordHasherPort;
import com.ballastlane.pokedex.application.port.out.TokenProviderPort;
import com.ballastlane.pokedex.application.port.out.UserRepositoryPort;
import com.ballastlane.pokedex.domain.exception.DuplicateResourceException;
import com.ballastlane.pokedex.domain.exception.InvalidCredentialsException;
import com.ballastlane.pokedex.domain.exception.UserNotFoundException;
import com.ballastlane.pokedex.domain.model.IssuedToken;
import com.ballastlane.pokedex.domain.model.Registration;
import com.ballastlane.pokedex.domain.model.Role;
import com.ballastlane.pokedex.domain.model.User;
import java.util.Locale;

/** Sign-up, login and current-user lookup. */
public class AuthService implements RegisterUserUseCase, LoginUseCase, CurrentUserUseCase {

  private final UserRepositoryPort users;
  private final PasswordHasherPort hasher;
  private final TokenProviderPort tokens;

  public AuthService(
      UserRepositoryPort users, PasswordHasherPort hasher, TokenProviderPort tokens) {
    this.users = users;
    this.hasher = hasher;
    this.tokens = tokens;
  }

  @Override
  public User register(RegisterCommand command) {
    Registration data = new Registration(command.username(), command.email(), command.password());
    if (users.findByUsername(data.username()).isPresent()) {
      throw new DuplicateResourceException("User", "username", data.username());
    }
    if (users.findByEmail(data.email()).isPresent()) {
      throw new DuplicateResourceException("User", "email", data.email());
    }
    User user =
        new User(null, data.username(), data.email(), hasher.hash(data.password()), Role.USER);
    return users.save(user);
  }

  @Override
  public IssuedToken login(String username, String password) {
    User user =
        users.findByUsername(normalize(username)).orElseThrow(InvalidCredentialsException::new);
    if (!hasher.matches(password, user.passwordHash())) {
      throw new InvalidCredentialsException();
    }
    return tokens.issue(user);
  }

  @Override
  public User current(String username) {
    return users
        .findByUsername(normalize(username))
        .orElseThrow(() -> new UserNotFoundException(username));
  }

  private static String normalize(String username) {
    return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
  }
}
