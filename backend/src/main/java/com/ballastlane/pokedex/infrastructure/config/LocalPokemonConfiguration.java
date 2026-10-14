package com.ballastlane.pokedex.infrastructure.config;

import com.ballastlane.pokedex.application.port.in.SyncPokemonUseCase;
import com.ballastlane.pokedex.application.port.out.PokemonCatalogPort;
import com.ballastlane.pokedex.application.port.out.PokemonRepositoryPort;
import com.ballastlane.pokedex.application.usecase.LocalPokemonService;
import com.ballastlane.pokedex.application.usecase.SyncPokemonService;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wiring for the local store use cases (US03, US04). */
@Configuration
class LocalPokemonConfiguration {

  @Bean
  Clock clock() {
    return Clock.systemUTC();
  }

  @Bean
  LocalPokemonService localPokemonService(
      PokemonRepositoryPort repository, PokemonCatalogPort catalog, Clock clock) {
    return new LocalPokemonService(repository, catalog, clock);
  }

  @Bean
  SyncPokemonUseCase syncPokemonUseCase(
      PokemonCatalogPort catalog, PokemonRepositoryPort repository, Clock clock) {
    return new SyncPokemonService(catalog, repository, clock);
  }
}
