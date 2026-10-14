package com.ballastlane.pokedex.infrastructure.adapter.out.pokeapi;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Connection settings for PokeAPI, bound from {@code pokedex.pokeapi.*}. */
@ConfigurationProperties(prefix = "pokedex.pokeapi")
public record PokeApiProperties(String baseUrl, Duration connectTimeout, Duration readTimeout) {}
