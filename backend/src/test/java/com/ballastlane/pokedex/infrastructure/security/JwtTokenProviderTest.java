package com.ballastlane.pokedex.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ballastlane.pokedex.domain.model.AuthenticatedUser;
import com.ballastlane.pokedex.domain.model.IssuedToken;
import com.ballastlane.pokedex.domain.model.Role;
import com.ballastlane.pokedex.domain.model.User;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

  private static final String SECRET = "test-secret-only-for-automated-tests-minimum-32-bytes";
  private static final Instant NOW = Instant.parse("2026-02-01T12:00:00Z");
  private static final User ADMIN = new User(1L, "admin", "admin@example.com", "h", Role.ADMIN);

  private static JwtTokenProvider providerAt(Instant instant) {
    return new JwtTokenProvider(
        SECRET, Duration.ofMinutes(60), Clock.fixed(instant, ZoneOffset.UTC));
  }

  @Test
  void issuedTokenVerifiesToTheSameUserAndRole() {
    JwtTokenProvider provider = providerAt(NOW);

    IssuedToken token = provider.issue(ADMIN);

    assertThat(token.expiresAt()).isEqualTo(NOW.plus(Duration.ofMinutes(60)));
    assertThat(provider.verify(token.value())).contains(new AuthenticatedUser("admin", Role.ADMIN));
  }

  @Test
  void expiredTokenIsRejected() {
    String token = providerAt(NOW).issue(ADMIN).value();

    assertThat(providerAt(NOW.plus(Duration.ofMinutes(61))).verify(token)).isEmpty();
  }

  @Test
  void tokenSignedWithAnotherSecretIsRejected() {
    String forged =
        new JwtTokenProvider(
                "another-secret-that-is-also-long-enough-32b",
                Duration.ofMinutes(60),
                Clock.fixed(NOW, ZoneOffset.UTC))
            .issue(ADMIN)
            .value();

    assertThat(providerAt(NOW).verify(forged)).isEmpty();
  }

  @Test
  void malformedTokenIsRejected() {
    assertThat(providerAt(NOW).verify("not-a-jwt")).isEmpty();
  }

  @Test
  void secretShorterThan32BytesFailsFast() {
    assertThatThrownBy(
            () ->
                new JwtTokenProvider(
                    "short", Duration.ofMinutes(60), Clock.fixed(NOW, ZoneOffset.UTC)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("32");
  }
}
