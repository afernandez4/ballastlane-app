package com.ballastlane.pokedex.application.usecase;

import com.ballastlane.pokedex.application.port.in.SyncPokemonUseCase;
import com.ballastlane.pokedex.application.port.out.PokemonCatalogPort;
import com.ballastlane.pokedex.application.port.out.PokemonRepositoryPort;
import com.ballastlane.pokedex.domain.exception.DomainException;
import com.ballastlane.pokedex.domain.exception.InvalidPaginationException;
import com.ballastlane.pokedex.domain.exception.PokemonNotFoundException;
import com.ballastlane.pokedex.domain.model.LocalPokemon;
import com.ballastlane.pokedex.domain.model.PokemonBaseData;
import com.ballastlane.pokedex.domain.model.PokemonDetail;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** US03: sync is an upsert on {@code pokeApiId}; own fields are never overwritten. */
public class SyncPokemonService implements SyncPokemonUseCase {

  static final int MAX_RANGE = 151;
  private static final List<String> PREFERRED_LANGUAGES = List.of("es", "en");

  private final PokemonCatalogPort catalog;
  private final PokemonRepositoryPort repository;
  private final Clock clock;

  public SyncPokemonService(
      PokemonCatalogPort catalog, PokemonRepositoryPort repository, Clock clock) {
    this.catalog = catalog;
    this.repository = repository;
    this.clock = clock;
  }

  @Override
  public SyncOutcome sync(String idOrName) {
    String key = idOrName == null ? "" : idOrName.trim().toLowerCase(Locale.ROOT);
    PokemonDetail detail =
        catalog
            .findDetail(key, PREFERRED_LANGUAGES)
            .orElseThrow(() -> new PokemonNotFoundException(String.valueOf(idOrName)));

    Instant now = clock.instant();
    PokemonBaseData base = toBaseData(detail);
    Optional<LocalPokemon> existing = repository.findByPokeApiId(detail.id());

    if (existing.isPresent()) {
      LocalPokemon saved = repository.save(existing.get().withBaseData(base, now));
      return new SyncOutcome(saved, false);
    }
    LocalPokemon saved = repository.save(LocalPokemon.fromBaseData(detail.id(), base, now));
    return new SyncOutcome(saved, true);
  }

  @Override
  public BulkSyncResult syncRange(int from, int to) {
    if (from < 1 || to < from || to - from + 1 > MAX_RANGE) {
      throw new InvalidPaginationException(
          "range must satisfy 1 <= from <= to and contain at most "
              + MAX_RANGE
              + " ids, was "
              + from
              + ".."
              + to);
    }
    int synced = 0;
    int updated = 0;
    List<FailedSync> failed = new ArrayList<>();
    for (int id = from; id <= to; id++) {
      try {
        SyncOutcome outcome = sync(String.valueOf(id));
        if (outcome.created()) {
          synced++;
        } else {
          updated++;
        }
      } catch (DomainException e) {
        failed.add(new FailedSync(id, e.getMessage()));
      }
    }
    return new BulkSyncResult(synced, updated, failed);
  }

  private static PokemonBaseData toBaseData(PokemonDetail detail) {
    return new PokemonBaseData(
        detail.name(),
        detail.imageUrl(),
        detail.category(),
        detail.height() > 0 ? detail.height() : null,
        detail.weight() > 0 ? detail.weight() : null,
        detail.types(),
        detail.abilities());
  }
}
