package com.ballastlane.pokedex.infrastructure.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ballastlane.pokedex.application.port.in.CreateLocalPokemonUseCase;
import com.ballastlane.pokedex.application.port.in.DeleteLocalPokemonUseCase;
import com.ballastlane.pokedex.application.port.in.GetLocalPokemonUseCase;
import com.ballastlane.pokedex.application.port.in.ListLocalPokemonUseCase;
import com.ballastlane.pokedex.application.port.in.PatchLocalPokemonUseCase;
import com.ballastlane.pokedex.application.port.in.ReplaceLocalPokemonUseCase;
import com.ballastlane.pokedex.application.port.out.TokenProviderPort;
import com.ballastlane.pokedex.domain.exception.ConcurrentModificationException;
import com.ballastlane.pokedex.domain.exception.DuplicateResourceException;
import com.ballastlane.pokedex.domain.exception.PokemonNotFoundException;
import com.ballastlane.pokedex.domain.model.LocalPokemon;
import com.ballastlane.pokedex.domain.model.LocalPokemonFields;
import com.ballastlane.pokedex.domain.model.LocalPokemonPatch;
import com.ballastlane.pokedex.domain.model.PageResult;
import com.ballastlane.pokedex.domain.model.PokemonType;
import com.ballastlane.pokedex.infrastructure.security.SecurityConfig;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LocalPokemonCrudController.class)
@Import({SecurityConfig.class, ApiExceptionHandler.class})
class LocalPokemonCrudControllerTest {

  private static final Instant NOW = Instant.parse("2026-02-01T12:00:00Z");
  private static final String BASE = "/api/v1/pokemon";

  @Autowired private MockMvc mvc;

  @MockitoBean private TokenProviderPort tokens;

  @MockitoBean private ListLocalPokemonUseCase list;
  @MockitoBean private GetLocalPokemonUseCase get;
  @MockitoBean private CreateLocalPokemonUseCase create;
  @MockitoBean private ReplaceLocalPokemonUseCase replace;
  @MockitoBean private PatchLocalPokemonUseCase patch;
  @MockitoBean private DeleteLocalPokemonUseCase delete;

  private static LocalPokemon pikachu(long version) {
    return new LocalPokemon(
        1L,
        25,
        "pikachu",
        "sprite.png",
        "Mouse Pokémon",
        4,
        60,
        List.of(new PokemonType("electric", 1)),
        List.of(),
        "Rayo",
        "Kanto",
        List.of("electric"),
        version,
        NOW,
        NOW);
  }

  @Test
  @WithAnonymousUser
  void listIsPublicAndSupportsFilters() throws Exception {
    when(list.list(any(), anyInt(), anyInt()))
        .thenReturn(new PageResult<>(List.of(pikachu(0)), 0, 20, 1L));

    mvc.perform(get(BASE).param("name", "pika").param("region", "Kanto").param("tag", "electric"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].localName").value("Rayo"))
        .andExpect(jsonPath("$.totalElements").value(1));
  }

  @Test
  @WithAnonymousUser
  void getReturnsPokemonWithEtag() throws Exception {
    when(get.get(1L)).thenReturn(pikachu(3L));

    mvc.perform(get(BASE + "/1"))
        .andExpect(status().isOk())
        .andExpect(header().string("ETag", "\"3\""))
        .andExpect(jsonPath("$.pokeApiId").value(25));
  }

  @Test
  @WithAnonymousUser
  void getUnknownIdReturns404() throws Exception {
    when(get.get(9L)).thenThrow(new PokemonNotFoundException("9"));

    mvc.perform(get(BASE + "/9")).andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser
  void createReturns201WithLocationAndEtag() throws Exception {
    when(create.create(eq(25), any(LocalPokemonFields.class))).thenReturn(pikachu(0L));

    mvc.perform(
            post(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"pokeApiId\":25,\"localName\":\"Rayo\",\"region\":\"Kanto\",\"tags\":[\"electric\"]}"))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", BASE + "/1"))
        .andExpect(header().string("ETag", "\"0\""));
  }

  @Test
  @WithMockUser
  void createDuplicatePokeApiIdReturns409() throws Exception {
    when(create.create(anyInt(), any(LocalPokemonFields.class)))
        .thenThrow(new DuplicateResourceException("Pokémon", "pokeApiId", 25));

    mvc.perform(
            post(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"pokeApiId\":25,\"localName\":\"Rayo\"}"))
        .andExpect(status().isConflict());
  }

  @Test
  @WithMockUser
  void createWithBlankLocalNameReturns400() throws Exception {
    mvc.perform(
            post(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"pokeApiId\":25,\"localName\":\"  \"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].field").exists());
  }

  @Test
  @WithMockUser
  void createWithUnknownFieldReturns400() throws Exception {
    mvc.perform(
            post(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"pokeApiId\":25,\"localName\":\"Rayo\",\"hacked\":true}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void createWithEmptyBodyReturns400() throws Exception {
    mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void replaceWithMatchingIfMatchReturns200() throws Exception {
    when(replace.replace(eq(1L), any(LocalPokemonFields.class), eq(3L))).thenReturn(pikachu(4L));

    mvc.perform(
            put(BASE + "/1")
                .header("If-Match", "\"3\"")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"localName\":\"Rayo\",\"region\":null,\"tags\":[]}"))
        .andExpect(status().isOk())
        .andExpect(header().string("ETag", "\"4\""));
  }

  @Test
  @WithMockUser
  void replaceWithStaleVersionReturns409() throws Exception {
    when(replace.replace(anyLong(), any(LocalPokemonFields.class), anyLong()))
        .thenThrow(new ConcurrentModificationException(1L));

    mvc.perform(
            put(BASE + "/1")
                .header("If-Match", "\"1\"")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"localName\":\"Rayo\"}"))
        .andExpect(status().isConflict());
  }

  @Test
  @WithMockUser
  void replaceWithoutLocalNameReturns400() throws Exception {
    mvc.perform(
            put(BASE + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"region\":\"Kanto\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void patchOnlyTouchesSentFields() throws Exception {
    when(patch.patch(eq(1L), any(LocalPokemonPatch.class))).thenReturn(pikachu(1L));

    mvc.perform(
            patch(BASE + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"region\":\"Johto\"}"))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "USER")
  void deleteIsForbiddenForUsers() throws Exception {
    mvc.perform(delete(BASE + "/1")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void deleteReturns204ForAdmins() throws Exception {
    mvc.perform(delete(BASE + "/1")).andExpect(status().isNoContent());
  }

  @Test
  @WithAnonymousUser
  void writesRequireAuthentication() throws Exception {
    mvc.perform(
            post(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"pokeApiId\":25,\"localName\":\"Rayo\"}"))
        .andExpect(status().isUnauthorized());
  }
}
