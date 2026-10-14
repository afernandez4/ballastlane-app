package com.ballastlane.pokedex.domain.model;

import com.ballastlane.pokedex.domain.exception.InvalidUserDataException;
import com.ballastlane.pokedex.domain.exception.InvalidUserDataException.FieldViolation;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Validated sign-up data. Username and email are lowercased; the password is kept as typed because
 * it is hashed by the application layer.
 */
public record Registration(String username, String email, String password) {

  static final Pattern USERNAME = Pattern.compile("[a-z0-9_]{3,50}");
  static final Pattern EMAIL = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}");
  static final int MIN_PASSWORD = 8;
  static final int MAX_PASSWORD = 72;

  public Registration {
    List<FieldViolation> violations = new ArrayList<>();

    username = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    if (!USERNAME.matcher(username).matches()) {
      violations.add(
          new FieldViolation("username", "must be 3-50 characters: letters, digits or underscore"));
    }

    email = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    if (email.length() > 254 || !EMAIL.matcher(email).matches()) {
      violations.add(new FieldViolation("email", "must be a valid email address"));
    }

    password = password == null ? "" : password;
    if (password.length() < MIN_PASSWORD || password.length() > MAX_PASSWORD) {
      violations.add(
          new FieldViolation(
              "password", "must be " + MIN_PASSWORD + "-" + MAX_PASSWORD + " characters"));
    } else if (!password.matches(".*[A-Za-z].*") || !password.matches(".*\\d.*")) {
      violations.add(
          new FieldViolation("password", "must contain at least one letter and one digit"));
    }

    if (!violations.isEmpty()) {
      throw new InvalidUserDataException(violations);
    }
  }
}
