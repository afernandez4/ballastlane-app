package com.ballastlane.pokedex.application.port.out;

import com.ballastlane.pokedex.domain.model.User;
import java.util.Optional;

public interface UserRepositoryPort {

  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);

  User save(User user);
}
