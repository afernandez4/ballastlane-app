package com.ballastlane.pokedex.infrastructure.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ballastlane.pokedex.application.port.in.CurrentUserUseCase;
import com.ballastlane.pokedex.application.port.in.GetPokemonDetailUseCase;
import com.ballastlane.pokedex.application.port.in.ListPokemonUseCase;
import com.ballastlane.pokedex.application.port.in.LoginUseCase;
import com.ballastlane.pokedex.application.port.in.RegisterUserUseCase;
import com.ballastlane.pokedex.application.port.out.TokenProviderPort;
import com.ballastlane.pokedex.infrastructure.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({PokemonCatalogController.class, AuthController.class})
@Import({SecurityConfig.class, ApiExceptionHandler.class, CorrelationIdFilter.class})
class ErrorHandlingTest {

  @Autowired private MockMvc mvc;

  @MockitoBean private ListPokemonUseCase list;
  @MockitoBean private GetPokemonDetailUseCase detail;
  @MockitoBean private RegisterUserUseCase register;
  @MockitoBean private LoginUseCase login;
  @MockitoBean private CurrentUserUseCase current;
  @MockitoBean private TokenProviderPort tokens;

  @Test
  @WithAnonymousUser
  void wrongParameterTypeReturns400ProblemDetail() throws Exception {
    mvc.perform(get("/api/v1/pokeapi/pokemon").param("page", "abc"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.detail").value("parameter 'page' has an invalid value"));
  }

  @Test
  @WithMockUser
  void unsupportedMethodReturns405() throws Exception {
    mvc.perform(delete("/api/v1/pokeapi/pokemon")).andExpect(status().isMethodNotAllowed());
  }

  @Test
  @WithAnonymousUser
  void unsupportedMediaTypeReturns415() throws Exception {
    mvc.perform(post("/api/v1/auth/login").contentType(MediaType.TEXT_PLAIN).content("hello"))
        .andExpect(status().isUnsupportedMediaType());
  }

  @Test
  @WithMockUser
  void unknownPathReturns404() throws Exception {
    mvc.perform(get("/api/v1/does-not-exist")).andExpect(status().isNotFound());
  }

  @Test
  @WithAnonymousUser
  void unexpectedErrorReturns500WithoutStackTraceAndWithCorrelationId() throws Exception {
    when(login.login(any(), any())).thenThrow(new IllegalStateException("db password is hunter2"));

    mvc.perform(
            post("/api/v1/auth/login")
                .header("X-Correlation-Id", "req-42")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"a\",\"password\":\"b\"}"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.correlationId").value("req-42"))
        .andExpect(
            content()
                .string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("hunter2"))))
        .andExpect(
            content()
                .string(
                    org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("IllegalStateException"))));
  }

  @Test
  @WithAnonymousUser
  void correlationIdIsEchoedAndGeneratedWhenMissing() throws Exception {
    mvc.perform(get("/api/v1/pokeapi/pokemon").header("X-Correlation-Id", "abc-123"))
        .andExpect(header().string("X-Correlation-Id", "abc-123"));
    mvc.perform(get("/api/v1/pokeapi/pokemon")).andExpect(header().exists("X-Correlation-Id"));
  }
}
