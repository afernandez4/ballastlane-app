package com.ballastlane.pokedex.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ballastlane.pokedex.application.port.in.RegisterUserUseCase.RegisterCommand;
import com.ballastlane.pokedex.application.port.out.PasswordHasherPort;
import com.ballastlane.pokedex.application.port.out.TokenProviderPort;
import com.ballastlane.pokedex.application.port.out.UserRepositoryPort;
import com.ballastlane.pokedex.domain.exception.DuplicateResourceException;
import com.ballastlane.pokedex.domain.exception.InvalidCredentialsException;
import com.ballastlane.pokedex.domain.exception.InvalidUserDataException;
import com.ballastlane.pokedex.domain.exception.UserNotFoundException;
import com.ballastlane.pokedex.domain.model.IssuedToken;
import com.ballastlane.pokedex.domain.model.Role;
import com.ballastlane.pokedex.domain.model.User;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserRepositoryPort users;
  @Mock private PasswordHasherPort hasher;
  @Mock private TokenProviderPort tokens;

  @InjectMocks private AuthService service;

  private static final User ASH =
      new User(1L, "ash", "ash@example.com", "hashed-secret", Role.USER);

  @Test
  void registerStoresUserWithHashedPasswordAndUserRole() {
    when(users.findByUsername("ash")).thenReturn(Optional.empty());
    when(users.findByEmail("ash@example.com")).thenReturn(Optional.empty());
    when(hasher.hash("Pikachu123")).thenReturn("hashed-secret");
    when(users.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

    User saved = service.register(new RegisterCommand("Ash", "ash@example.com", "Pikachu123"));

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(users).save(captor.capture());
    assertThat(captor.getValue().username()).isEqualTo("ash");
    assertThat(captor.getValue().passwordHash()).isEqualTo("hashed-secret");
    assertThat(captor.getValue().role()).isEqualTo(Role.USER);
    assertThat(saved.passwordHash()).isNotEqualTo("Pikachu123");
  }

  @Test
  void registerRejectsDuplicateUsernameWithConflict() {
    when(users.findByUsername("ash")).thenReturn(Optional.of(ASH));

    assertThatThrownBy(
            () -> service.register(new RegisterCommand("ash", "other@example.com", "Pikachu123")))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessageContaining("username");
    verify(users, never()).save(any());
  }

  @Test
  void registerRejectsDuplicateEmailWithConflict() {
    when(users.findByUsername("misty")).thenReturn(Optional.empty());
    when(users.findByEmail("ash@example.com")).thenReturn(Optional.of(ASH));

    assertThatThrownBy(
            () -> service.register(new RegisterCommand("misty", "ash@example.com", "Pikachu123")))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessageContaining("email");
  }

  @Test
  void registerRejectsWeakPasswordBeforeTouchingStorage() {
    assertThatThrownBy(() -> service.register(new RegisterCommand("ash", "a@b.co", "weak")))
        .isInstanceOf(InvalidUserDataException.class);
    verify(users, never()).save(any());
  }

  @Test
  void loginReturnsTokenForCorrectPassword() {
    IssuedToken token = new IssuedToken("jwt-value", Instant.parse("2026-02-01T13:00:00Z"));
    when(users.findByUsername("ash")).thenReturn(Optional.of(ASH));
    when(hasher.matches("Pikachu123", "hashed-secret")).thenReturn(true);
    when(tokens.issue(ASH)).thenReturn(token);

    assertThat(service.login("ASH", "Pikachu123")).isEqualTo(token);
  }

  @Test
  void loginWithWrongPasswordIsRejected() {
    when(users.findByUsername("ash")).thenReturn(Optional.of(ASH));
    when(hasher.matches(anyString(), anyString())).thenReturn(false);

    assertThatThrownBy(() -> service.login("ash", "wrong"))
        .isInstanceOf(InvalidCredentialsException.class);
    verify(tokens, never()).issue(any());
  }

  @Test
  void loginWithUnknownUserUsesTheSameErrorAsWrongPassword() {
    when(users.findByUsername("ghost")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.login("ghost", "whatever"))
        .isInstanceOf(InvalidCredentialsException.class)
        .hasMessage("Invalid username or password");
  }

  @Test
  void currentUserReturnsStoredUser() {
    when(users.findByUsername("ash")).thenReturn(Optional.of(ASH));

    assertThat(service.current("ash")).isEqualTo(ASH);
  }

  @Test
  void currentUserThrowsWhenMissing() {
    when(users.findByUsername("ghost")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.current("ghost")).isInstanceOf(UserNotFoundException.class);
  }
}
