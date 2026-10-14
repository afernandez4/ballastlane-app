package com.ballastlane.pokedex.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * In-memory Caffeine caches for PokeAPI responses. Caching lives in infrastructure, never domain.
 */
@Configuration
@EnableCaching
@EnableConfigurationProperties(PokedexCacheProperties.class)
class CacheConfig {

  static final String POKEMON_PAGE = "pokemonPage";
  static final String POKEMON_DETAIL = "pokemonDetail";
  static final String SPECIES = "species";
  static final String EVOLUTION_CHAIN = "evolutionChain";

  @Bean
  CacheManager cacheManager(PokedexCacheProperties properties) {
    CaffeineCacheManager manager =
        new CaffeineCacheManager(POKEMON_PAGE, POKEMON_DETAIL, SPECIES, EVOLUTION_CHAIN);
    manager.setCaffeine(
        Caffeine.newBuilder().expireAfterWrite(properties.ttl()).maximumSize(properties.maxSize()));
    return manager;
  }
}
