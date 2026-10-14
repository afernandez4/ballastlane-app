package com.ballastlane.pokedex.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** JWT settings bound from {@code pokedex.security.*}. */
@ConfigurationProperties(prefix = "pokedex.security")
public record PokedexSecurityProperties(String jwtSecret, long jwtExpirationMinutes) {}
