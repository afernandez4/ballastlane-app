package com.ballastlane.pokedex.infrastructure.adapter.out.pokeapi;

import com.ballastlane.pokedex.domain.exception.ExternalServiceException;
import com.ballastlane.pokedex.infrastructure.adapter.out.pokeapi.PokeApiDtos.EvolutionChainResponse;
import com.ballastlane.pokedex.infrastructure.adapter.out.pokeapi.PokeApiDtos.PokemonListResponse;
import com.ballastlane.pokedex.infrastructure.adapter.out.pokeapi.PokeApiDtos.PokemonResponse;
import com.ballastlane.pokedex.infrastructure.adapter.out.pokeapi.PokeApiDtos.SpeciesResponse;
import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Thin HTTP client over PokeAPI. A 404 becomes {@link Optional#empty()}; any other failure (5xx,
 * timeout, connection refused) becomes an {@link ExternalServiceException}.
 */
public class PokeApiClient {

  private final RestClient restClient;

  public PokeApiClient(RestClient restClient) {
    this.restClient = restClient;
  }

  public PokemonListResponse listPokemon(int offset, int limit) {
    return get(PokemonListResponse.class, "/pokemon?offset={offset}&limit={limit}", offset, limit)
        .orElseThrow(() -> new ExternalServiceException("PokeAPI returned no list body"));
  }

  public Optional<PokemonResponse> getPokemon(String idOrName) {
    return get(PokemonResponse.class, "/pokemon/{key}", idOrName);
  }

  @Cacheable("species")
  public Optional<SpeciesResponse> getSpecies(String idOrName) {
    return get(SpeciesResponse.class, "/pokemon-species/{key}", idOrName);
  }

  @Cacheable("evolutionChain")
  public Optional<EvolutionChainResponse> getEvolutionChain(int id) {
    return get(EvolutionChainResponse.class, "/evolution-chain/{id}", id);
  }

  private <T> Optional<T> get(Class<T> type, String path, Object... vars) {
    try {
      return Optional.ofNullable(restClient.get().uri(path, vars).retrieve().body(type));
    } catch (HttpClientErrorException.NotFound notFound) {
      return Optional.empty();
    } catch (RestClientException e) {
      throw new ExternalServiceException("PokeAPI request failed: " + path, e);
    }
  }
}
