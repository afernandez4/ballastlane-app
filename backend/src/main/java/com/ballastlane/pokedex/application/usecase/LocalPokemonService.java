package com.ballastlane.pokedex.application.usecase;

import com.ballastlane.pokedex.application.port.in.CreateLocalPokemonUseCase;
import com.ballastlane.pokedex.application.port.in.DeleteLocalPokemonUseCase;
import com.ballastlane.pokedex.application.port.in.GetLocalPokemonUseCase;
import com.ballastlane.pokedex.application.port.in.ListLocalPokemonUseCase;
import com.ballastlane.pokedex.application.port.in.PatchLocalPokemonUseCase;
import com.ballastlane.pokedex.application.port.in.ReplaceLocalPokemonUseCase;
import com.ballastlane.pokedex.application.port.out.PokemonCatalogPort;
import com.ballastlane.pokedex.application.port.out.PokemonRepositoryPort;
import com.ballastlane.pokedex.domain.exception.ConcurrentModificationException;
import com.ballastlane.pokedex.domain.exception.DuplicateResourceException;
import com.ballastlane.pokedex.domain.exception.InvalidPokemonDataException;
import com.ballastlane.pokedex.domain.exception.InvalidPokemonDataException.FieldViolation;
import com.ballastlane.pokedex.domain.exception.PokemonNotFoundException;
import com.ballastlane.pokedex.domain.model.LocalPokemon;
import com.ballastlane.pokedex.domain.model.LocalPokemonFields;
import com.ballastlane.pokedex.domain.model.LocalPokemonFilter;
import com.ballastlane.pokedex.domain.model.LocalPokemonPatch;
import com.ballastlane.pokedex.domain.model.PageRequest;
import com.ballastlane.pokedex.domain.model.PageResult;
import com.ballastlane.pokedex.domain.model.PokemonBaseData;
import com.ballastlane.pokedex.domain.model.PokemonDetail;
import java.time.Clock;
import java.util.List;

/** US04 and local CRUD. All operations validate through the domain model. */
public class LocalPokemonService
    implements ListLocalPokemonUseCase,
        GetLocalPokemonUseCase,
        CreateLocalPokemonUseCase,
        ReplaceLocalPokemonUseCase,
        PatchLocalPokemonUseCase,
        DeleteLocalPokemonUseCase {

  private static final List<String> PREFERRED_LANGUAGES = List.of("en");

  private final PokemonRepositoryPort repository;
  private final PokemonCatalogPort catalog;
  private final Clock clock;

  public LocalPokemonService(
      PokemonRepositoryPort repository, PokemonCatalogPort catalog, Clock clock) {
    this.repository = repository;
    this.catalog = catalog;
    this.clock = clock;
  }

  @Override
  public PageResult<LocalPokemon> list(LocalPokemonFilter filter, int page, int size) {
    return repository.search(filter, new PageRequest(page, size));
  }

  @Override
  public LocalPokemon get(long id) {
    return find(id);
  }

  @Override
  public LocalPokemon create(int pokeApiId, LocalPokemonFields fields) {
    if (repository.findByPokeApiId(pokeApiId).isPresent()) {
      throw new DuplicateResourceException("Pokémon", "pokeApiId", pokeApiId);
    }
    PokemonDetail detail =
        catalog
            .findDetail(String.valueOf(pokeApiId), PREFERRED_LANGUAGES)
            .orElseThrow(() -> new PokemonNotFoundException(String.valueOf(pokeApiId)));
    PokemonBaseData base =
        new PokemonBaseData(
            detail.name(),
            detail.imageUrl(),
            detail.category(),
            detail.height() > 0 ? detail.height() : null,
            detail.weight() > 0 ? detail.weight() : null,
            detail.types(),
            detail.abilities());
    LocalPokemon pokemon =
        LocalPokemon.fromBaseData(pokeApiId, base, clock.instant()).withOwnFields(fields);
    return repository.save(pokemon);
  }

  @Override
  public LocalPokemon replace(long id, LocalPokemonFields fields, Long expectedVersion) {
    LocalPokemon current = find(id);
    if (expectedVersion != null && expectedVersion != current.version()) {
      throw new ConcurrentModificationException(id);
    }
    return repository.save(current.withOwnFields(fields));
  }

  @Override
  public LocalPokemon patch(long id, LocalPokemonPatch patch) {
    LocalPokemon current = find(id);
    if (patch.isEmpty()) {
      throw new InvalidPokemonDataException(
          List.of(new FieldViolation("body", "must contain at least one field")));
    }
    LocalPokemonFields merged =
        new LocalPokemonFields(
            patch.localName().present() ? patch.localName().value() : current.localName(),
            patch.region().present() ? patch.region().value() : current.region(),
            patch.tags().present() ? patch.tags().value() : current.tags());
    return repository.save(current.withOwnFields(merged));
  }

  @Override
  public void delete(long id) {
    find(id);
    repository.deleteById(id);
  }

  private LocalPokemon find(long id) {
    return repository
        .findById(id)
        .orElseThrow(() -> new PokemonNotFoundException(String.valueOf(id)));
  }
}
