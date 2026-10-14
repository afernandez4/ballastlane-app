package com.ballastlane.pokedex.infrastructure.adapter.out.pokeapi;

import com.ballastlane.pokedex.application.port.out.PokemonCatalogPort;
import com.ballastlane.pokedex.domain.exception.DomainException;
import com.ballastlane.pokedex.domain.exception.ExternalServiceException;
import com.ballastlane.pokedex.domain.model.PageRequest;
import com.ballastlane.pokedex.domain.model.PageResult;
import com.ballastlane.pokedex.domain.model.PokemonDetail;
import com.ballastlane.pokedex.domain.model.PokemonSummary;
import com.ballastlane.pokedex.infrastructure.adapter.out.pokeapi.PokeApiDtos.EvolutionChainResponse;
import com.ballastlane.pokedex.infrastructure.adapter.out.pokeapi.PokeApiDtos.NamedResource;
import com.ballastlane.pokedex.infrastructure.adapter.out.pokeapi.PokeApiDtos.PokemonListResponse;
import com.ballastlane.pokedex.infrastructure.adapter.out.pokeapi.PokeApiDtos.PokemonResponse;
import com.ballastlane.pokedex.infrastructure.adapter.out.pokeapi.PokeApiDtos.SpeciesResponse;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import org.springframework.cache.annotation.Cacheable;

/**
 * Implements the catalogue port on top of PokeAPI. The detail of every item in a page is loaded in
 * parallel on the given executor (virtual threads in production).
 */
public class PokeApiCatalogAdapter implements PokemonCatalogPort {

  private final PokeApiClient client;
  private final Executor executor;

  public PokeApiCatalogAdapter(PokeApiClient client, Executor executor) {
    this.client = client;
    this.executor = executor;
  }

  @Override
  @Cacheable("pokemonPage")
  public PageResult<PokemonSummary> findPage(PageRequest request) {
    PokemonListResponse list = client.listPokemon(request.page() * request.size(), request.size());

    List<CompletableFuture<PokemonSummary>> pending =
        list.results().stream()
            .map(r -> CompletableFuture.supplyAsync(() -> loadSummary(r), executor))
            .toList();

    List<PokemonSummary> content = pending.stream().map(PokeApiCatalogAdapter::join).toList();
    return new PageResult<>(content, request.page(), request.size(), list.count());
  }

  @Override
  @Cacheable("pokemonDetail")
  public Optional<PokemonDetail> findDetail(
      String normalizedIdOrName, List<String> preferredLanguages) {
    Optional<PokemonResponse> found = client.getPokemon(normalizedIdOrName);
    if (found.isEmpty()) {
      return Optional.empty();
    }
    PokemonResponse pokemon = found.get();

    String speciesName = pokemon.species() == null ? pokemon.name() : pokemon.species().name();
    SpeciesResponse species =
        client
            .getSpecies(speciesName)
            .orElseThrow(
                () -> new ExternalServiceException("PokeAPI has no species for " + speciesName));

    EvolutionChainResponse chain = loadChain(species);
    return Optional.of(PokeApiMapper.toDetail(pokemon, species, chain, preferredLanguages));
  }

  private PokemonSummary loadSummary(NamedResource resource) {
    int id = PokeApiMapper.idFromUrl(resource.url());
    String key = String.valueOf(id);
    PokemonResponse pokemon =
        client
            .getPokemon(key)
            .orElseThrow(
                () ->
                    new ExternalServiceException(
                        "PokeAPI listed pokemon " + id + " but returned 404"));
    SpeciesResponse species =
        client.getSpecies(key).orElse(new SpeciesResponse(id, null, null, null, null));
    return PokeApiMapper.toSummary(id, pokemon, species);
  }

  private EvolutionChainResponse loadChain(SpeciesResponse species) {
    if (species.evolutionChain() == null) {
      return null;
    }
    int chainId = PokeApiMapper.idFromUrl(species.evolutionChain().url());
    return client.getEvolutionChain(chainId).orElse(null);
  }

  private static <T> T join(CompletableFuture<T> future) {
    try {
      return future.join();
    } catch (CompletionException e) {
      if (e.getCause() instanceof DomainException domain) {
        throw domain;
      }
      throw new ExternalServiceException("PokeAPI page load failed", e.getCause());
    }
  }
}
