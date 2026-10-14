package com.ballastlane.pokedex.infrastructure.adapter.out.persistence;

import com.ballastlane.pokedex.application.port.out.PokemonRepositoryPort;
import com.ballastlane.pokedex.domain.exception.ConcurrentModificationException;
import com.ballastlane.pokedex.domain.exception.DuplicateResourceException;
import com.ballastlane.pokedex.domain.exception.PokemonNotFoundException;
import com.ballastlane.pokedex.domain.model.LocalPokemon;
import com.ballastlane.pokedex.domain.model.LocalPokemonFilter;
import com.ballastlane.pokedex.domain.model.PageRequest;
import com.ballastlane.pokedex.domain.model.PageResult;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Stores local Pokémon in Postgres through Spring Data JPA. Each call runs in one transaction so
 * element collections can be read while mapping (open-in-view is disabled).
 */
@Component
@Transactional
public class PokemonPersistenceAdapter implements PokemonRepositoryPort {

  private final PokemonJpaRepository jpa;

  public PokemonPersistenceAdapter(PokemonJpaRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<LocalPokemon> findById(long id) {
    return jpa.findById(id).map(PokemonEntityMapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<LocalPokemon> findByPokeApiId(int pokeApiId) {
    return jpa.findByPokeApiId(pokeApiId).map(PokemonEntityMapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public PageResult<LocalPokemon> search(LocalPokemonFilter filter, PageRequest page) {
    org.springframework.data.domain.PageRequest springPage =
        org.springframework.data.domain.PageRequest.of(page.page(), page.size(), Sort.by("id"));
    Page<PokemonEntity> result =
        jpa.search("%" + filter.name() + "%", filter.region(), filter.tag(), springPage);
    return new PageResult<>(
        result.map(PokemonEntityMapper::toDomain).getContent(),
        page.page(),
        page.size(),
        result.getTotalElements());
  }

  @Override
  public LocalPokemon save(LocalPokemon pokemon) {
    PokemonEntity entity =
        pokemon.id() == null
            ? new PokemonEntity()
            : jpa.findById(pokemon.id())
                .orElseThrow(() -> new PokemonNotFoundException(String.valueOf(pokemon.id())));
    PokemonEntityMapper.apply(entity, pokemon);
    try {
      return PokemonEntityMapper.toDomain(jpa.saveAndFlush(entity));
    } catch (DataIntegrityViolationException e) {
      throw new DuplicateResourceException("Pokémon", "pokeApiId", pokemon.pokeApiId());
    } catch (OptimisticLockingFailureException e) {
      throw new ConcurrentModificationException(pokemon.id());
    }
  }

  @Override
  public void deleteById(long id) {
    jpa.findById(id).ifPresent(jpa::delete);
  }
}
