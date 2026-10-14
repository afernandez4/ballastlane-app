package com.ballastlane.pokedex.infrastructure.adapter.in.web.dto;

import com.ballastlane.pokedex.domain.model.Role;
import com.ballastlane.pokedex.domain.model.User;

/** Never exposes the password hash. */
public record UserResponse(Long id, String username, String email, Role role) {

  public static UserResponse from(User user) {
    return new UserResponse(user.id(), user.username(), user.email(), user.role());
  }
}
