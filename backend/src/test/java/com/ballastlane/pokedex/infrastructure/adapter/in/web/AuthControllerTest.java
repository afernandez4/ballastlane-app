package com.ballastlane.pokedex.infrastructure.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ballastlane.pokedex.application.port.in.CurrentUserUseCase;
import com.ballastlane.pokedex.application.port.in.LoginUseCase;
import com.ballastlane.pokedex.application.port.in.RegisterUserUseCase;
import com.ballastlane.pokedex.application.port.in.RegisterUserUseCase.RegisterCommand;
import com.ballastlane.pokedex.application.port.out.TokenProviderPort;
import com.ballastlane.pokedex.domain.exception.DuplicateResourceException;
import com.ballastlane.pokedex.domain.exception.InvalidCredentialsException;
import com.ballastlane.pokedex.domain.exception.InvalidUserDataException;
import com.ballastlane.pokedex.domain.model.IssuedToken;
import com.ballastlane.pokedex.domain.model.Role;
import com.ballastlane.pokedex.domain.model.User;
import com.ballastlane.pokedex.infrastructure.security.SecurityConfig;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, ApiExceptionHandler.class})
class AuthControllerTest {

  private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

  @Autowired private MockMvc mvc;

  @MockitoBean private TokenProviderPort tokens;

  @MockitoBean private RegisterUserUseCase register;
  @MockitoBean private LoginUseCase login;
  @MockitoBean private CurrentUserUseCase current;

  @Test
  @WithAnonymousUser
  void registerReturns201WithoutPasswordHash() throws Exception {
    when(register.register(any(RegisterCommand.class)))
        .thenReturn(new User(1L, "ash", "ash@example.com", "secret-hash", Role.USER));

    mvc.perform(
            post("/api/v1/auth/register")
                .contentType(JSON)
                .content(
                    "{\"username\":\"ash\",\"email\":\"ash@example.com\",\"password\":\"Pikachu123\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.username").value("ash"))
        .andExpect(jsonPath("$.role").value("USER"))
        .andExpect(jsonPath("$.passwordHash").doesNotExist());
  }

  @Test
  @WithAnonymousUser
  void registerDuplicateReturns409() throws Exception {
    when(register.register(any(RegisterCommand.class)))
        .thenThrow(new DuplicateResourceException("User", "username", "ash"));

    mvc.perform(
            post("/api/v1/auth/register")
                .contentType(JSON)
                .content(
                    "{\"username\":\"ash\",\"email\":\"ash@example.com\",\"password\":\"Pikachu123\"}"))
        .andExpect(status().isConflict());
  }

  @Test
  @WithAnonymousUser
  void registerWeakPasswordReturns400WithErrors() throws Exception {
    when(register.register(any(RegisterCommand.class)))
        .thenThrow(
            new InvalidUserDataException(
                java.util.List.of(
                    new InvalidUserDataException.FieldViolation(
                        "password", "must contain at least one letter and one digit"))));

    mvc.perform(
            post("/api/v1/auth/register")
                .contentType(JSON)
                .content(
                    "{\"username\":\"ash\",\"email\":\"ash@example.com\",\"password\":\"weak\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].field").value("password"));
  }

  @Test
  @WithAnonymousUser
  void loginReturnsBearerToken() throws Exception {
    when(login.login(anyString(), anyString()))
        .thenReturn(new IssuedToken("jwt-value", Instant.parse("2026-02-01T13:00:00Z")));

    mvc.perform(
            post("/api/v1/auth/login")
                .contentType(JSON)
                .content("{\"username\":\"ash\",\"password\":\"Pikachu123\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("jwt-value"))
        .andExpect(jsonPath("$.tokenType").value("Bearer"))
        .andExpect(jsonPath("$.expiresAt").value("2026-02-01T13:00:00Z"));
  }

  @Test
  @WithAnonymousUser
  void loginWithBadCredentialsReturns401() throws Exception {
    when(login.login(anyString(), anyString())).thenThrow(new InvalidCredentialsException());

    mvc.perform(
            post("/api/v1/auth/login")
                .contentType(JSON)
                .content("{\"username\":\"ash\",\"password\":\"wrong\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithAnonymousUser
  void meRequiresAuthentication() throws Exception {
    mvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(username = "ash")
  void meReturnsTheAuthenticatedUser() throws Exception {
    when(current.current("ash"))
        .thenReturn(new User(1L, "ash", "ash@example.com", "hash", Role.USER));

    mvc.perform(get("/api/v1/auth/me"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("ash"));
    verify(current).current("ash");
  }
}
