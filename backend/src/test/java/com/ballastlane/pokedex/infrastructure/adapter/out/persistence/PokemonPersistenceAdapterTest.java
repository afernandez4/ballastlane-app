package com.ballastlane.pokedex.infrastructure.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ballastlane.pokedex.application.port.out.PokemonRepositoryPort;
import com.ballastlane.pokedex.domain.exception.DuplicateResourceException;
import com.ballastlane.pokedex.domain.model.Ability;
import com.ballastlane.pokedex.domain.model.LocalPokemon;
import com.ballastlane.pokedex.domain.model.LocalPokemonFields;
import com.ballastlane.pokedex.domain.model.LocalPokemonFilter;
import com.ballastlane.pokedex.domain.model.PageRequest;
import com.ballastlane.pokedex.domain.model.PageResult;
import com.ballastlane.pokedex.domain.model.PokemonType;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Runs the real Flyway migrations and Hibernate validation against Postgres 16. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Testcontainers
@Import(PokemonPersistenceAdapter.class)
class PokemonPersistenceAdapterTest {

  private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private PokemonRepositoryPort repository;

  @Test
  void savesAndReloadsPokemonWithItsCollections() {
    LocalPokemon saved =
        repository.save(pokemon(1, "bulbasaur", "Kanto", List.of("starter", "grass")));

    LocalPokemon loaded = repository.findById(saved.id()).orElseThrow();

    assertThat(loaded.pokeApiId()).isEqualTo(1);
    assertThat(loaded.localName()).isEqualTo("Mi bulbasaur");
    assertThat(loaded.types())
        .containsExactly(new PokemonType("grass", 1), new PokemonType("poison", 2));
    assertThat(loaded.abilities()).containsExactly(new Ability("overgrow", false));
    assertThat(loaded.tags()).containsExactly("starter", "grass");
    assertThat(loaded.version()).isZero();
  }

  @Test
  void findsByPokeApiId() {
    repository.save(pokemon(4, "charmander", "Kanto", List.of()));

    assertThat(repository.findByPokeApiId(4)).isPresent();
    assertThat(repository.findByPokeApiId(999)).isEmpty();
  }

  @Test
  void rejectsSecondPokemonWithSamePokeApiId() {
    repository.save(pokemon(7, "squirtle", "Kanto", List.of()));

    assertThatThrownBy(() -> repository.save(pokemon(7, "squirtle-copy", "Kanto", List.of())))
        .isInstanceOf(DuplicateResourceException.class);
  }

  @Test
  void updatesOwnFieldsAndIncrementsVersion() {
    LocalPokemon saved = repository.save(pokemon(25, "pikachu", "Kanto", List.of()));
    LocalPokemon edited =
        saved.withOwnFields(new LocalPokemonFields("Rayo", "Johto", List.of("fast")));

    LocalPokemon updated = repository.save(edited);

    assertThat(updated.localName()).isEqualTo("Rayo");
    assertThat(updated.region()).isEqualTo("Johto");
    assertThat(updated.tags()).containsExactly("fast");
    assertThat(updated.version()).isEqualTo(1L);
  }

  @Test
  void searchFiltersByNameInsideNameOrLocalName() {
    repository.save(pokemon(1, "bulbasaur", "Kanto", List.of()));
    repository.save(pokemon(2, "ivysaur", "Kanto", List.of()));
    repository.save(pokemon(25, "pikachu", "Kanto", List.of()));

    PageResult<LocalPokemon> result =
        repository.search(new LocalPokemonFilter("SAUR", "", ""), new PageRequest(0, 10));

    assertThat(result.totalElements()).isEqualTo(2);
    assertThat(result.content())
        .extracting(LocalPokemon::name)
        .containsExactlyInAnyOrder("bulbasaur", "ivysaur");
  }

  @Test
  void searchFiltersByRegionIgnoringCase() {
    repository.save(pokemon(1, "bulbasaur", "Kanto", List.of()));
    repository.save(pokemon(152, "chikorita", "Johto", List.of()));

    PageResult<LocalPokemon> result =
        repository.search(new LocalPokemonFilter("", "johto", ""), new PageRequest(0, 10));

    assertThat(result.content()).extracting(LocalPokemon::name).containsExactly("chikorita");
  }

  @Test
  void searchFiltersByTagAndCombinesFilters() {
    repository.save(pokemon(1, "bulbasaur", "Kanto", List.of("starter")));
    repository.save(pokemon(4, "charmander", "Kanto", List.of("starter")));
    repository.save(pokemon(7, "squirtle", "Kanto", List.of("water")));

    PageResult<LocalPokemon> byTag =
        repository.search(new LocalPokemonFilter("", "", "starter"), new PageRequest(0, 10));
    PageResult<LocalPokemon> combined =
        repository.search(
            new LocalPokemonFilter("char", "kanto", "starter"), new PageRequest(0, 10));

    assertThat(byTag.totalElements()).isEqualTo(2);
    assertThat(combined.content()).extracting(LocalPokemon::name).containsExactly("charmander");
  }

  @Test
  void searchPaginatesResults() {
    for (int id = 1; id <= 5; id++) {
      repository.save(pokemon(id, "pokemon" + id, "Kanto", List.of()));
    }

    PageResult<LocalPokemon> secondPage =
        repository.search(new LocalPokemonFilter("", "", ""), new PageRequest(1, 2));

    assertThat(secondPage.content()).hasSize(2);
    assertThat(secondPage.totalElements()).isEqualTo(5);
    assertThat(secondPage.totalPages()).isEqualTo(3);
  }

  @Test
  void deleteRemovesPokemon() {
    LocalPokemon saved = repository.save(pokemon(143, "snorlax", "Kanto", List.of()));

    repository.deleteById(saved.id());

    assertThat(repository.findById(saved.id())).isEmpty();
  }

  private static LocalPokemon pokemon(
      int pokeApiId, String name, String region, List<String> tags) {
    return new LocalPokemon(
        null,
        pokeApiId,
        name,
        "https://img/" + pokeApiId + ".png",
        "Seed Pokémon",
        7,
        69,
        List.of(new PokemonType("grass", 1), new PokemonType("poison", 2)),
        List.of(new Ability("overgrow", false)),
        "Mi " + name,
        region,
        tags,
        0L,
        NOW,
        NOW);
  }
}
