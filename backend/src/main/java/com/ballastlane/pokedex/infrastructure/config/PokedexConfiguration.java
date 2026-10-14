package com.ballastlane.pokedex.infrastructure.config;

import com.ballastlane.pokedex.application.port.in.GetPokemonDetailUseCase;
import com.ballastlane.pokedex.application.port.in.ListPokemonUseCase;
import com.ballastlane.pokedex.application.port.out.PokemonCatalogPort;
import com.ballastlane.pokedex.application.usecase.GetPokemonDetailService;
import com.ballastlane.pokedex.application.usecase.ListPokemonService;
import com.ballastlane.pokedex.infrastructure.adapter.out.pokeapi.PokeApiCatalogAdapter;
import com.ballastlane.pokedex.infrastructure.adapter.out.pokeapi.PokeApiClient;
import com.ballastlane.pokedex.infrastructure.adapter.out.pokeapi.PokeApiProperties;
import java.util.concurrent.Executors;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/** Wires the framework-free use cases and the PokeAPI adapter together. */
@Configuration
@EnableConfigurationProperties(PokeApiProperties.class)
class PokedexConfiguration {

  @Bean
  RestClient pokeApiRestClient(PokeApiProperties properties) {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout((int) properties.connectTimeout().toMillis());
    factory.setReadTimeout((int) properties.readTimeout().toMillis());
    return RestClient.builder().baseUrl(properties.baseUrl()).requestFactory(factory).build();
  }

  @Bean
  PokeApiClient pokeApiClient(RestClient pokeApiRestClient) {
    return new PokeApiClient(pokeApiRestClient);
  }

  @Bean
  PokemonCatalogPort pokemonCatalogPort(PokeApiClient pokeApiClient) {
    return new PokeApiCatalogAdapter(pokeApiClient, Executors.newVirtualThreadPerTaskExecutor());
  }

  @Bean
  ListPokemonUseCase listPokemonUseCase(PokemonCatalogPort catalog) {
    return new ListPokemonService(catalog);
  }

  @Bean
  GetPokemonDetailUseCase getPokemonDetailUseCase(PokemonCatalogPort catalog) {
    return new GetPokemonDetailService(catalog);
  }
}
