package com.ballastlane.pokedex.infrastructure.adapter.in.web;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ballastlane.pokedex.application.port.in.GetPokemonDetailUseCase;
import com.ballastlane.pokedex.application.port.in.ListPokemonUseCase;
import com.ballastlane.pokedex.application.port.out.TokenProviderPort;
import com.ballastlane.pokedex.domain.exception.ExternalServiceException;
import com.ballastlane.pokedex.domain.exception.InvalidPaginationException;
import com.ballastlane.pokedex.domain.exception.PokemonNotFoundException;
import com.ballastlane.pokedex.domain.model.Ability;
import com.ballastlane.pokedex.domain.model.EvolutionChain;
import com.ballastlane.pokedex.domain.model.EvolutionStage;
import com.ballastlane.pokedex.domain.model.PageResult;
import com.ballastlane.pokedex.domain.model.PokemonDetail;
import com.ballastlane.pokedex.domain.model.PokemonSummary;
import com.ballastlane.pokedex.domain.model.PokemonType;
import com.ballastlane.pokedex.domain.model.Stat;
import com.ballastlane.pokedex.infrastructure.security.SecurityConfig;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PokemonCatalogController.class)
@Import({SecurityConfig.class, ApiExceptionHandler.class})
class PokemonCatalogControllerTest {

  private static final String BASE = "/api/v1/pokeapi/pokemon";

  @Autowired private MockMvc mvc;

  @MockitoBean private TokenProviderPort tokens;

  @MockitoBean private ListPokemonUseCase listPokemon;

  @MockitoBean private GetPokemonDetailUseCase getDetail;

  @Test
  void listReturns200WithPageOfSummaries() throws Exception {
    PokemonSummary bulbasaur =
        new PokemonSummary(
            1,
            "bulbasaur",
            "https://img/1.png",
            "Seed Pokémon",
            69,
            List.of(new Ability("overgrow", false)));
    when(listPokemon.list(0, 20)).thenReturn(new PageResult<>(List.of(bulbasaur), 0, 20, 1351L));

    mvc.perform(get(BASE).param("page", "0").param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].name").value("bulbasaur"))
        .andExpect(jsonPath("$.content[0].category").value("Seed Pokémon"))
        .andExpect(jsonPath("$.content[0].weight").value(69))
        .andExpect(jsonPath("$.content[0].abilities[0].name").value("overgrow"))
        .andExpect(jsonPath("$.totalElements").value(1351))
        .andExpect(jsonPath("$.totalPages").value(68));
  }

  @Test
  void listUsesDefaultsWhenParametersAreMissing() throws Exception {
    when(listPokemon.list(0, 20)).thenReturn(new PageResult<>(List.of(), 0, 20, 0L));

    mvc.perform(get(BASE)).andExpect(status().isOk());
  }

  @Test
  void listReturns400ForInvalidPagination() throws Exception {
    when(listPokemon.list(anyInt(), anyInt()))
        .thenThrow(new InvalidPaginationException("size must be between 1 and 50, was 51"));

    mvc.perform(get(BASE).param("page", "0").param("size", "51"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.detail").value("size must be between 1 and 50, was 51"));
  }

  @Test
  void detailReturns200WithStatsTypesAndEvolutionChain() throws Exception {
    PokemonDetail pikachu =
        new PokemonDetail(
            25,
            "pikachu",
            "https://img/25.png",
            4,
            60,
            List.of(new PokemonType("electric", 1)),
            List.of(new Stat("hp", 35)),
            "Cuando se enfada, libera electricidad.",
            new EvolutionChain(
                List.of(
                    new EvolutionStage("pichu", 172, 0, null, null),
                    new EvolutionStage("pikachu", 25, 1, 16, "level-up"))));
    when(getDetail.get("pikachu")).thenReturn(pikachu);

    mvc.perform(get(BASE + "/pikachu"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("pikachu"))
        .andExpect(jsonPath("$.types[0].name").value("electric"))
        .andExpect(jsonPath("$.stats[0].name").value("hp"))
        .andExpect(jsonPath("$.stats[0].baseValue").value(35))
        .andExpect(jsonPath("$.evolutionChain[1].speciesName").value("pikachu"))
        .andExpect(jsonPath("$.evolutionChain[1].minLevel").value(16));
  }

  @Test
  void detailReturns404WhenPokemonDoesNotExist() throws Exception {
    when(getDetail.get(anyString())).thenThrow(new PokemonNotFoundException("missingno"));

    mvc.perform(get(BASE + "/missingno"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  void detailReturns502WhenPokeApiFails() throws Exception {
    when(getDetail.get(anyString()))
        .thenThrow(new ExternalServiceException("PokeAPI request failed", null));

    mvc.perform(get(BASE + "/pikachu"))
        .andExpect(status().isBadGateway())
        .andExpect(jsonPath("$.status").value(502));
  }

  @Test
  void preflightFromFrontendOriginIsAllowed() throws Exception {
    mvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options(BASE)
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "GET"))
        .andExpect(
            org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                .string("Access-Control-Allow-Origin", "http://localhost:5173"));
  }
}
