package com.ballastlane.pokedex.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ballastlane.pokedex.application.port.out.PokemonCatalogPort;
import com.ballastlane.pokedex.application.port.out.PokemonRepositoryPort;
import com.ballastlane.pokedex.domain.exception.ConcurrentModificationException;
import com.ballastlane.pokedex.domain.exception.DuplicateResourceException;
import com.ballastlane.pokedex.domain.exception.InvalidPokemonDataException;
import com.ballastlane.pokedex.domain.exception.PokemonNotFoundException;
import com.ballastlane.pokedex.domain.model.Ability;
import com.ballastlane.pokedex.domain.model.EvolutionChain;
import com.ballastlane.pokedex.domain.model.FieldPatch;
import com.ballastlane.pokedex.domain.model.LocalPokemon;
import com.ballastlane.pokedex.domain.model.LocalPokemonFields;
import com.ballastlane.pokedex.domain.model.LocalPokemonFilter;
import com.ballastlane.pokedex.domain.model.LocalPokemonPatch;
import com.ballastlane.pokedex.domain.model.PageRequest;
import com.ballastlane.pokedex.domain.model.PageResult;
import com.ballastlane.pokedex.domain.model.PokemonDetail;
import com.ballastlane.pokedex.domain.model.PokemonType;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LocalPokemonServiceTest {

  private static final Instant NOW = Instant.parse("2026-02-01T12:00:00Z");
  private static final Instant EARLIER = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private PokemonRepositoryPort repository;
  @Mock private PokemonCatalogPort catalog;

  private LocalPokemonService service() {
    return new LocalPokemonService(repository, catalog, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  private static LocalPokemon stored(long id, String localName, String region, List<String> tags) {
    return new LocalPokemon(
        id,
        25,
        "pikachu",
        "sprite.png",
        "Mouse Pokémon",
        4,
        60,
        List.of(new PokemonType("electric", 1)),
        List.of(new Ability("static", false)),
        localName,
        region,
        tags,
        3L,
        EARLIER,
        EARLIER);
  }

  private static PokemonDetail pikachuDetail() {
    return new PokemonDetail(
        25,
        "pikachu",
        "https://img/25.png",
        4,
        60,
        List.of(new PokemonType("electric", 1)),
        List.of(),
        "",
        new EvolutionChain(List.of()),
        "Mouse Pokémon",
        List.of(new Ability("static", false)));
  }

  @Test
  void listDelegatesFilterAndPagination() {
    LocalPokemonFilter filter = new LocalPokemonFilter("pika", "", "");
    PageResult<LocalPokemon> page = new PageResult<>(List.of(), 0, 20, 0L);
    when(repository.search(filter, new PageRequest(0, 20))).thenReturn(page);

    assertThat(service().list(filter, 0, 20)).isEqualTo(page);
  }

  @Test
  void getReturnsStoredPokemon() {
    LocalPokemon pokemon = stored(1L, "Rayo", "Johto", List.of());
    when(repository.findById(1L)).thenReturn(Optional.of(pokemon));

    assertThat(service().get(1L)).isEqualTo(pokemon);
  }

  @Test
  void getUnknownIdThrowsNotFound() {
    when(repository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service().get(99L)).isInstanceOf(PokemonNotFoundException.class);
  }

  @Test
  void createStoresPokemonWithBaseDataAndOwnFields() {
    when(repository.findByPokeApiId(25)).thenReturn(Optional.empty());
    when(catalog.findDetail(eq("25"), anyList())).thenReturn(Optional.of(pikachuDetail()));
    when(repository.save(any(LocalPokemon.class))).thenAnswer(i -> i.getArgument(0));

    LocalPokemon created =
        service().create(25, new LocalPokemonFields("Rayo", "Kanto", List.of("electric")));

    assertThat(created.name()).isEqualTo("pikachu");
    assertThat(created.localName()).isEqualTo("Rayo");
    assertThat(created.tags()).containsExactly("electric");
    assertThat(created.createdAt()).isEqualTo(NOW);
  }

  @Test
  void createRejectsDuplicatePokeApiIdWith409Semantics() {
    when(repository.findByPokeApiId(25))
        .thenReturn(Optional.of(stored(1L, "Rayo", null, List.of())));

    assertThatThrownBy(() -> service().create(25, new LocalPokemonFields("Otro", null, List.of())))
        .isInstanceOf(DuplicateResourceException.class);
    verifyNoInteractions(catalog);
  }

  @Test
  void createFailsWhenPokeApiDoesNotKnowThePokemon() {
    when(repository.findByPokeApiId(9999)).thenReturn(Optional.empty());
    when(catalog.findDetail(anyString(), anyList())).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> service().create(9999, new LocalPokemonFields("Nada", null, List.of())))
        .isInstanceOf(PokemonNotFoundException.class);
    verify(repository, never()).save(any());
  }

  @Test
  void replaceUpdatesAllOwnFields() {
    when(repository.findById(1L))
        .thenReturn(Optional.of(stored(1L, "Viejo", "Kanto", List.of("a"))));
    when(repository.save(any(LocalPokemon.class))).thenAnswer(i -> i.getArgument(0));

    LocalPokemon result =
        service().replace(1L, new LocalPokemonFields("Nuevo", null, List.of("b")), null);

    assertThat(result.localName()).isEqualTo("Nuevo");
    assertThat(result.region()).isNull();
    assertThat(result.tags()).containsExactly("b");
    assertThat(result.pokeApiId()).isEqualTo(25);
  }

  @Test
  void replaceWithStaleExpectedVersionIsRejected() {
    when(repository.findById(1L)).thenReturn(Optional.of(stored(1L, "Rayo", null, List.of())));

    assertThatThrownBy(
            () -> service().replace(1L, new LocalPokemonFields("X", null, List.of()), 2L))
        .isInstanceOf(ConcurrentModificationException.class);
    verify(repository, never()).save(any());
  }

  @Test
  void replaceUnknownIdThrowsNotFound() {
    when(repository.findById(5L)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> service().replace(5L, new LocalPokemonFields("X", null, List.of()), null))
        .isInstanceOf(PokemonNotFoundException.class);
  }

  @Test
  void replaceWithBlankLocalNameIsRejectedByDomain() {
    assertThatThrownBy(() -> new LocalPokemonFields(" ", null, List.of()))
        .isInstanceOf(InvalidPokemonDataException.class);
  }

  @Test
  void patchChangesOnlyFieldsThatArePresent() {
    when(repository.findById(1L))
        .thenReturn(Optional.of(stored(1L, "Rayo", "Johto", List.of("fast"))));
    when(repository.save(any(LocalPokemon.class))).thenAnswer(i -> i.getArgument(0));

    LocalPokemon result =
        service()
            .patch(
                1L,
                new LocalPokemonPatch(
                    FieldPatch.absent(), FieldPatch.of("Kanto"), FieldPatch.absent()));

    assertThat(result.localName()).isEqualTo("Rayo");
    assertThat(result.region()).isEqualTo("Kanto");
    assertThat(result.tags()).containsExactly("fast");
  }

  @Test
  void patchWithExplicitNullClearsOptionalField() {
    when(repository.findById(1L))
        .thenReturn(Optional.of(stored(1L, "Rayo", "Johto", List.of("fast"))));
    when(repository.save(any(LocalPokemon.class))).thenAnswer(i -> i.getArgument(0));

    LocalPokemon result =
        service()
            .patch(
                1L,
                new LocalPokemonPatch(
                    FieldPatch.absent(), FieldPatch.of(null), FieldPatch.of(null)));

    assertThat(result.region()).isNull();
    assertThat(result.tags()).isEmpty();
    assertThat(result.localName()).isEqualTo("Rayo");
  }

  @Test
  void patchWithExplicitNullLocalNameIsRejected() {
    when(repository.findById(1L)).thenReturn(Optional.of(stored(1L, "Rayo", null, List.of())));

    assertThatThrownBy(
            () ->
                service()
                    .patch(
                        1L,
                        new LocalPokemonPatch(
                            FieldPatch.of(null), FieldPatch.absent(), FieldPatch.absent())))
        .isInstanceOf(InvalidPokemonDataException.class);
    verify(repository, never()).save(any());
  }

  @Test
  void deleteRemovesExistingPokemon() {
    when(repository.findById(1L)).thenReturn(Optional.of(stored(1L, "Rayo", null, List.of())));

    service().delete(1L);

    verify(repository).deleteById(1L);
  }

  @Test
  void deleteUnknownIdThrowsNotFound() {
    when(repository.findById(7L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service().delete(7L)).isInstanceOf(PokemonNotFoundException.class);
    verify(repository, never()).deleteById(7L);
  }
}
