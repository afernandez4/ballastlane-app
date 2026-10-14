package com.ballastlane.pokedex.infrastructure.adapter.out.persistence;

import com.ballastlane.pokedex.application.port.out.UserRepositoryPort;
import com.ballastlane.pokedex.domain.exception.DuplicateResourceException;
import com.ballastlane.pokedex.domain.model.User;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

/** Stores users through Spring Data JPA. Unique constraints are enforced by the database. */
@Component
public class UserPersistenceAdapter implements UserRepositoryPort {

  private final UserJpaRepository jpa;

  public UserPersistenceAdapter(UserJpaRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return jpa.findByUsername(username).map(UserPersistenceAdapter::toDomain);
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return jpa.findByEmail(email).map(UserPersistenceAdapter::toDomain);
  }

  @Override
  public User save(User user) {
    UserEntity entity =
        new UserEntity(user.username(), user.email(), user.passwordHash(), user.role());
    try {
      return toDomain(jpa.saveAndFlush(entity));
    } catch (DataIntegrityViolationException e) {
      throw new DuplicateResourceException("User", "username or email", user.username());
    }
  }

  private static User toDomain(UserEntity entity) {
    return new User(
        entity.getId(),
        entity.getUsername(),
        entity.getEmail(),
        entity.getPasswordHash(),
        entity.getRole());
  }
}
