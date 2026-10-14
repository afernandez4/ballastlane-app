package com.ballastlane.pokedex.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ballastlane.pokedex.application.port.in.SyncPokemonUseCase.SyncOutcome;
import com.ballastlane.pokedex.application.port.out.PokemonCatalogPort;
import com.ballastlane.pokedex.application.port.out.PokemonRepositoryPort;
import com.ballastlane.pokedex.domain.exception.InvalidPaginationException;
import com.ballastlane.pokedex.domain.exception.PokemonNotFoundException;
import com.ballastlane.pokedex.domain.model.Ability;
import com.ballastlane.pokedex.domain.model.EvolutionChain;
import com.ballastlane.pokedex.domain.model.LocalPokemon;
import com.ballastlane.pokedex.domain.model.PokemonDetail;
import com.ballastlane.pokedex.domain.model.PokemonType;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SyncPokemonServiceTest {

  private static final Instant NOW = Instant.parse("2026-02-01T12:00:00Z");
  private static final Instant EARLIER = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private PokemonCatalogPort catalog;
  @Mock private PokemonRepositoryPort repository;

  private SyncPokemonService service() {
    return new SyncPokemonService(catalog, repository, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  private static PokemonDetail pikachu(int height) {
    return new PokemonDetail(
        25,
        "pikachu",
        "https://img/25.png",
        height,
        60,
        List.of(new PokemonType("electric", 1)),
        List.of(),
        "Electric mouse.",
        new EvolutionChain(List.of()),
        "Mouse Pokémon",
        List.of(new Ability("static", false)));
  }

  @Test
  void createsLocalPokemonWhenNotStoredYet() {
    when(catalog.findDetail(eq("pikachu"), anyList())).thenReturn(Optional.of(pikachu(4)));
    when(repository.findByPokeApiId(25)).thenReturn(Optional.empty());
    when(repository.save(any(LocalPokemon.class))).thenAnswer(i -> i.getArgument(0));

    SyncOutcome outcome = service().sync("pikachu");

    ArgumentCaptor<LocalPokemon> saved = ArgumentCaptor.forClass(LocalPokemon.class);
    verify(repository).save(saved.capture());
    assertThat(outcome.created()).isTrue();
    assertThat(saved.getValue().pokeApiId()).isEqualTo(25);
    assertThat(saved.getValue().name()).isEqualTo("pikachu");
    assertThat(saved.getValue().category()).isEqualTo("Mouse Pokémon");
    assertThat(saved.getValue().abilities()).containsExactly(new Ability("static", false));
    assertThat(saved.getValue().localName()).isNull();
    assertThat(saved.getValue().createdAt()).isEqualTo(NOW);
    assertThat(saved.getValue().updatedAt()).isEqualTo(NOW);
  }

  @Test
  void updatesBaseDataAndKeepsOwnFieldsWhenAlreadyStored() {
    LocalPokemon stored =
        new LocalPokemon(
            9L,
            25,
            "pikachu",
            "old.png",
            "Old category",
            3,
            50,
            List.of(),
            List.of(),
            "Rayo",
            "Johto",
            List.of("fast"),
            2L,
            EARLIER,
            EARLIER);
    when(catalog.findDetail(anyString(), anyList())).thenReturn(Optional.of(pikachu(4)));
    when(repository.findByPokeApiId(25)).thenReturn(Optional.of(stored));
    when(repository.save(any(LocalPokemon.class))).thenAnswer(i -> i.getArgument(0));

    SyncOutcome outcome = service().sync("25");

    assertThat(outcome.created()).isFalse();
    LocalPokemon result = outcome.pokemon();
    assertThat(result.height()).isEqualTo(4);
    assertThat(result.category()).isEqualTo("Mouse Pokémon");
    assertThat(result.localName()).isEqualTo("Rayo");
    assertThat(result.region()).isEqualTo("Johto");
    assertThat(result.tags()).containsExactly("fast");
    assertThat(result.createdAt()).isEqualTo(EARLIER);
    assertThat(result.updatedAt()).isEqualTo(NOW);
    assertThat(result.version()).isEqualTo(2L);
  }

  @Test
  void unknownPokemonThrowsNotFoundAndStoresNothing() {
    when(catalog.findDetail(anyString(), anyList())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service().sync("missingno"))
        .isInstanceOf(PokemonNotFoundException.class);
    verifyNoInteractions(repository);
  }

  @Test
  void zeroHeightFromPokeApiIsStoredAsMissing() {
    when(catalog.findDetail(anyString(), anyList())).thenReturn(Optional.of(pikachu(0)));
    when(repository.findByPokeApiId(25)).thenReturn(Optional.empty());
    when(repository.save(any(LocalPokemon.class))).thenAnswer(i -> i.getArgument(0));

    assertThat(service().sync("25").pokemon().height()).isNull();
  }

  @Test
  void syncRangeCountsCreatedUpdatedAndCollectsFailures() {
    LocalPokemon stored =
        new LocalPokemon(
            2L, 2, "ivysaur", null, null, null, null, List.of(), List.of(), null, null, List.of(),
            0L, EARLIER, EARLIER);
    when(catalog.findDetail(eq("1"), anyList())).thenReturn(Optional.of(detail(1)));
    when(catalog.findDetail(eq("2"), anyList())).thenReturn(Optional.of(detail(2)));
    when(catalog.findDetail(eq("3"), anyList())).thenReturn(Optional.empty());
    when(repository.findByPokeApiId(1)).thenReturn(Optional.empty());
    when(repository.findByPokeApiId(2)).thenReturn(Optional.of(stored));
    when(repository.save(any(LocalPokemon.class))).thenAnswer(i -> i.getArgument(0));

    var summary = service().syncRange(1, 3);

    assertThat(summary.synced()).isEqualTo(1);
    assertThat(summary.updated()).isEqualTo(1);
    assertThat(summary.failed()).hasSize(1);
    assertThat(summary.failed().get(0).id()).isEqualTo(3);
  }

  @Test
  void syncRangeRejectsMoreThan151Pokemon() {
    assertThatThrownBy(() -> service().syncRange(1, 152))
        .isInstanceOf(InvalidPaginationException.class);
    verifyNoInteractions(catalog, repository);
  }

  @Test
  void syncRangeRejectsReversedBounds() {
    assertThatThrownBy(() -> service().syncRange(10, 5))
        .isInstanceOf(InvalidPaginationException.class);
  }

  private static PokemonDetail detail(int id) {
    return new PokemonDetail(
        id,
        "p" + id,
        null,
        1,
        1,
        List.of(),
        List.of(),
        "",
        new EvolutionChain(List.of()),
        "",
        List.of());
  }
}
