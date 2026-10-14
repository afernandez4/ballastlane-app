package com.ballastlane.pokedex.infrastructure.security;

import com.ballastlane.pokedex.application.port.out.TokenProviderPort;
import com.ballastlane.pokedex.domain.model.AuthenticatedUser;
import com.ballastlane.pokedex.domain.model.IssuedToken;
import com.ballastlane.pokedex.domain.model.Role;
import com.ballastlane.pokedex.domain.model.User;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;

/** HS256 JWTs. The secret must be at least 32 bytes; startup fails otherwise. */
public class JwtTokenProvider implements TokenProviderPort {

  private static final String ROLE_CLAIM = "role";
  private static final int MIN_SECRET_BYTES = 32;

  private final SecretKey key;
  private final Duration ttl;
  private final Clock clock;

  public JwtTokenProvider(String secret, Duration ttl, Clock clock) {
    byte[] bytes = secret == null ? new byte[0] : secret.getBytes(StandardCharsets.UTF_8);
    if (bytes.length < MIN_SECRET_BYTES) {
      throw new IllegalStateException(
          "JWT secret must be at least " + MIN_SECRET_BYTES + " bytes long");
    }
    this.key = Keys.hmacShaKeyFor(bytes);
    this.ttl = ttl;
    this.clock = clock;
  }

  @Override
  public IssuedToken issue(User user) {
    Instant now = clock.instant();
    Instant expiresAt = now.plus(ttl);
    String token =
        Jwts.builder()
            .subject(user.username())
            .claim(ROLE_CLAIM, user.role().name())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .signWith(key)
            .compact();
    return new IssuedToken(token, expiresAt);
  }

  @Override
  public Optional<AuthenticatedUser> verify(String token) {
    try {
      var claims =
          Jwts.parser()
              .verifyWith(key)
              .clock(() -> Date.from(clock.instant()))
              .build()
              .parseSignedClaims(token)
              .getPayload();
      Role role = Role.valueOf(claims.get(ROLE_CLAIM, String.class));
      return Optional.of(new AuthenticatedUser(claims.getSubject(), role));
    } catch (JwtException | IllegalArgumentException e) {
      return Optional.empty();
    }
  }
}
