package com.ballastlane.pokedex.infrastructure.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.ballastlane.pokedex.application.port.out.PokemonRepositoryPort;
import com.ballastlane.pokedex.domain.model.LocalPokemon;
import com.ballastlane.pokedex.domain.model.LocalPokemonFilter;
import com.ballastlane.pokedex.domain.model.PageRequest;
import com.ballastlane.pokedex.domain.model.PageResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Loads the demo seed exactly as the local and docker profiles do, and reads it back. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Testcontainers
@Import(PokemonPersistenceAdapter.class)
@TestPropertySource(properties = "spring.flyway.locations=classpath:db/migration,classpath:db/seed")
class SeedDataTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private PokemonRepositoryPort repository;

  @Test
  void demoSeedLoadsTwentyPokemonWithTypesAbilitiesAndOwnFields() {
    PageResult<LocalPokemon> all =
        repository.search(new LocalPokemonFilter("", "", ""), new PageRequest(0, 50));

    assertThat(all.totalElements()).isEqualTo(20);
    LocalPokemon bulbasaur = repository.findByPokeApiId(1).orElseThrow();
    assertThat(bulbasaur.types()).extracting(t -> t.name()).containsExactly("grass", "poison");
    assertThat(bulbasaur.abilities()).isNotEmpty();
    assertThat(bulbasaur.localName()).isEqualTo("Semillita");
    assertThat(bulbasaur.tags()).containsExactly("starter", "grass");
  }
}
