package com.ballastlane.pokedex.infrastructure.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Cache settings bound from {@code pokedex.cache.*}. Applies to every Caffeine cache. */
@ConfigurationProperties(prefix = "pokedex.cache")
public record PokedexCacheProperties(Duration ttl, long maxSize) {}
