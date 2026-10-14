package com.ballastlane.pokedex.infrastructure.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ballastlane.pokedex.application.port.out.UserRepositoryPort;
import com.ballastlane.pokedex.domain.exception.DuplicateResourceException;
import com.ballastlane.pokedex.domain.model.Role;
import com.ballastlane.pokedex.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Testcontainers
@Import(UserPersistenceAdapter.class)
class UserPersistenceAdapterTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private UserRepositoryPort users;

  @Test
  void savesAndFindsUserByUsernameAndEmail() {
    User saved = users.save(new User(null, "ash", "ash@example.com", "hash", Role.USER));

    assertThat(saved.id()).isNotNull();
    assertThat(users.findByUsername("ash")).contains(saved);
    assertThat(users.findByEmail("ash@example.com")).contains(saved);
    assertThat(users.findByUsername("missing")).isEmpty();
  }

  @Test
  void duplicateUsernameIsReportedAsConflict() {
    users.save(new User(null, "misty", "misty@example.com", "hash", Role.USER));

    assertThatThrownBy(
            () -> users.save(new User(null, "misty", "other@example.com", "hash", Role.USER)))
        .isInstanceOf(DuplicateResourceException.class);
  }

  @Test
  void seededRolesAreMappedBackToEnum() {
    User admin = users.save(new User(null, "root", "root@example.com", "hash", Role.ADMIN));

    assertThat(users.findByUsername("root").orElseThrow().role()).isEqualTo(Role.ADMIN);
    assertThat(admin.role()).isEqualTo(Role.ADMIN);
  }
}
