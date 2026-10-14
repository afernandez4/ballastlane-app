package com.ballastlane.pokedex.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ballastlane.pokedex.domain.exception.InvalidUserDataException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RegistrationTest {

  @Test
  void normalizesUsernameAndEmail() {
    Registration registration =
        new Registration("  Ash_Ketchum ", " Ash@Example.COM ", "Pikachu123");

    assertThat(registration.username()).isEqualTo("ash_ketchum");
    assertThat(registration.email()).isEqualTo("ash@example.com");
  }

  @Test
  void acceptsValidRegistration() {
    assertThat(new Registration("demo", "demo@example.com", "Demo1234!").username())
        .isEqualTo("demo");
  }

  @ParameterizedTest
  @ValueSource(strings = {"ab", "has space", "bad-dash"})
  void rejectsInvalidUsername(String username) {
    assertViolation(() -> new Registration(username, "a@b.co", "Passw0rd"), "username");
  }

  @ParameterizedTest
  @ValueSource(strings = {"no-at-sign", "a@b", "@example.com", " "})
  void rejectsInvalidEmail(String email) {
    assertViolation(() -> new Registration("demo", email, "Passw0rd"), "email");
  }

  @ParameterizedTest
  @ValueSource(strings = {"short1", "onlyletters", "12345678"})
  void rejectsWeakPassword(String password) {
    assertViolation(() -> new Registration("demo", "a@b.co", password), "password");
  }

  private static void assertViolation(Runnable action, String field) {
    assertThatThrownBy(action::run)
        .isInstanceOfSatisfying(
            InvalidUserDataException.class,
            ex ->
                assertThat(ex.getViolations())
                    .extracting(InvalidUserDataException.FieldViolation::field)
                    .contains(field));
  }
}
