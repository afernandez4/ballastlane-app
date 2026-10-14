package com.ballastlane.pokedex.infrastructure.adapter.in.web;

import com.ballastlane.pokedex.application.port.in.CurrentUserUseCase;
import com.ballastlane.pokedex.application.port.in.LoginUseCase;
import com.ballastlane.pokedex.application.port.in.RegisterUserUseCase;
import com.ballastlane.pokedex.application.port.in.RegisterUserUseCase.RegisterCommand;
import com.ballastlane.pokedex.domain.model.IssuedToken;
import com.ballastlane.pokedex.infrastructure.adapter.in.web.dto.LoginRequest;
import com.ballastlane.pokedex.infrastructure.adapter.in.web.dto.RegisterRequest;
import com.ballastlane.pokedex.infrastructure.adapter.in.web.dto.TokenResponse;
import com.ballastlane.pokedex.infrastructure.adapter.in.web.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Sign-up, login and the current user")
class AuthController {

  private final RegisterUserUseCase register;
  private final LoginUseCase login;
  private final CurrentUserUseCase current;

  AuthController(RegisterUserUseCase register, LoginUseCase login, CurrentUserUseCase current) {
    this.register = register;
    this.login = login;
    this.current = current;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create an account (role USER)")
  @ApiResponse(responseCode = "201", description = "Account created")
  @ApiResponse(responseCode = "400", description = "Username, email or password rule broken")
  @ApiResponse(responseCode = "409", description = "Username or email already taken")
  UserResponse register(@RequestBody RegisterRequest body) {
    return UserResponse.from(
        register.register(new RegisterCommand(body.username(), body.email(), body.password())));
  }

  @PostMapping("/login")
  @Operation(summary = "Get a bearer token")
  @ApiResponse(responseCode = "200", description = "Token issued")
  @ApiResponse(responseCode = "401", description = "Wrong username or password")
  TokenResponse login(@RequestBody LoginRequest body) {
    IssuedToken token = login.login(body.username(), body.password());
    return new TokenResponse(token.value(), "Bearer", token.expiresAt());
  }

  @GetMapping("/me")
  @Operation(
      summary = "Current user",
      security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth"))
  @ApiResponse(responseCode = "401", description = "Missing or invalid token")
  UserResponse me(Principal principal) {
    return UserResponse.from(current.current(principal.getName()));
  }
}
