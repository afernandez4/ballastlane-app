package com.ballastlane.pokedex.infrastructure.config;

import com.ballastlane.pokedex.application.port.out.PasswordHasherPort;
import com.ballastlane.pokedex.application.port.out.TokenProviderPort;
import com.ballastlane.pokedex.application.port.out.UserRepositoryPort;
import com.ballastlane.pokedex.application.usecase.AuthService;
import com.ballastlane.pokedex.infrastructure.security.JwtTokenProvider;
import java.time.Clock;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableConfigurationProperties(PokedexSecurityProperties.class)
class AuthConfiguration {

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  TokenProviderPort tokenProvider(PokedexSecurityProperties properties, Clock clock) {
    return new JwtTokenProvider(
        properties.jwtSecret(), Duration.ofMinutes(properties.jwtExpirationMinutes()), clock);
  }

  /**
   * One bean implements all three auth use cases. Separate bean methods per interface would make
   * the injection ambiguous: Spring matches the runtime type, which is AuthService for each of
   * them.
   */
  @Bean
  AuthService authService(
      UserRepositoryPort users, PasswordHasherPort hasher, TokenProviderPort tokens) {
    return new AuthService(users, hasher, tokens);
  }
}
