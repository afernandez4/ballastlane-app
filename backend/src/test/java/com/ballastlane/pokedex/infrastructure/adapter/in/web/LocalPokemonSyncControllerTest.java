package com.ballastlane.pokedex.infrastructure.adapter.in.web;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ballastlane.pokedex.application.port.in.SyncPokemonUseCase;
import com.ballastlane.pokedex.application.port.in.SyncPokemonUseCase.BulkSyncResult;
import com.ballastlane.pokedex.application.port.in.SyncPokemonUseCase.FailedSync;
import com.ballastlane.pokedex.application.port.in.SyncPokemonUseCase.SyncOutcome;
import com.ballastlane.pokedex.application.port.out.TokenProviderPort;
import com.ballastlane.pokedex.domain.exception.PokemonNotFoundException;
import com.ballastlane.pokedex.domain.model.LocalPokemon;
import com.ballastlane.pokedex.infrastructure.security.SecurityConfig;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LocalPokemonController.class)
@Import({SecurityConfig.class, ApiExceptionHandler.class})
class LocalPokemonSyncControllerTest {

  private static final Instant NOW = Instant.parse("2026-02-01T12:00:00Z");

  @Autowired private MockMvc mvc;

  @MockitoBean private TokenProviderPort tokens;

  @MockitoBean private SyncPokemonUseCase sync;

  private static LocalPokemon stored(long id) {
    return new LocalPokemon(
        id,
        25,
        "pikachu",
        null,
        "Mouse Pokémon",
        4,
        60,
        List.of(),
        List.of(),
        null,
        null,
        List.of(),
        0L,
        NOW,
        NOW);
  }

  @Test
  @WithMockUser
  void syncCreatedReturns201WithLocation() throws Exception {
    when(sync.sync(anyString())).thenReturn(new SyncOutcome(stored(1L), true));

    mvc.perform(post("/api/v1/pokemon/sync/pikachu"))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/v1/pokemon/1"))
        .andExpect(jsonPath("$.pokeApiId").value(25))
        .andExpect(jsonPath("$.name").value("pikachu"));
  }

  @Test
  @WithMockUser
  void syncOfExistingPokemonReturns200() throws Exception {
    when(sync.sync(anyString())).thenReturn(new SyncOutcome(stored(1L), false));

    mvc.perform(post("/api/v1/pokemon/sync/25")).andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void syncUnknownPokemonReturns404() throws Exception {
    when(sync.sync(anyString())).thenThrow(new PokemonNotFoundException("missingno"));

    mvc.perform(post("/api/v1/pokemon/sync/missingno")).andExpect(status().isNotFound());
  }

  @Test
  void syncRequiresAuthentication() throws Exception {
    mvc.perform(post("/api/v1/pokemon/sync/pikachu")).andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "USER")
  void bulkSyncIsForbiddenForUsers() throws Exception {
    mvc.perform(post("/api/v1/pokemon/sync?from=1&to=3")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void bulkSyncReturnsSummaryWithFailures() throws Exception {
    when(sync.syncRange(1, 3))
        .thenReturn(new BulkSyncResult(2, 1, List.of(new FailedSync(3, "not found"))));

    mvc.perform(post("/api/v1/pokemon/sync?from=1&to=3"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.synced").value(2))
        .andExpect(jsonPath("$.updated").value(1))
        .andExpect(jsonPath("$.failed[0].id").value(3))
        .andExpect(jsonPath("$.failed[0].reason").value("not found"));
  }
}
