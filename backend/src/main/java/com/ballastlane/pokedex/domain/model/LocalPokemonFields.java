package com.ballastlane.pokedex.domain.model;

import com.ballastlane.pokedex.domain.exception.InvalidPokemonDataException;
import com.ballastlane.pokedex.domain.exception.InvalidPokemonDataException.FieldViolation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * The fields owned by this application and editable by users (US04). Text is trimmed, tags are
 * trimmed and lowercased. All violations are reported together.
 */
public record LocalPokemonFields(String localName, String region, List<String> tags) {

  static final int MAX_LOCAL_NAME_LENGTH = 100;
  static final int MAX_REGION_LENGTH = 100;
  static final int MAX_TAGS = 10;
  static final int MAX_TAG_LENGTH = 30;

  public LocalPokemonFields {
    List<FieldViolation> violations = new ArrayList<>();

    localName = trimToNull(localName);
    if (localName == null) {
      violations.add(new FieldViolation("localName", "must not be blank"));
    } else if (localName.length() > MAX_LOCAL_NAME_LENGTH) {
      violations.add(
          new FieldViolation(
              "localName", "must be at most " + MAX_LOCAL_NAME_LENGTH + " characters"));
    }

    region = trimToNull(region);
    if (region != null && region.length() > MAX_REGION_LENGTH) {
      violations.add(
          new FieldViolation("region", "must be at most " + MAX_REGION_LENGTH + " characters"));
    }

    tags = normalizeTags(tags, violations);

    if (!violations.isEmpty()) {
      throw new InvalidPokemonDataException(violations);
    }
  }

  private static String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private static List<String> normalizeTags(List<String> raw, List<FieldViolation> violations) {
    if (raw == null) {
      return List.of();
    }
    if (raw.size() > MAX_TAGS) {
      violations.add(new FieldViolation("tags", "must contain at most " + MAX_TAGS + " tags"));
    }

    List<String> normalized = new ArrayList<>();
    Set<String> seen = new HashSet<>();
    boolean blank = false;
    boolean tooLong = false;
    boolean duplicated = false;

    for (String tag : raw) {
      String value = tag == null ? "" : tag.trim().toLowerCase(Locale.ROOT);
      if (value.isEmpty()) {
        blank = true;
        continue;
      }
      if (value.length() > MAX_TAG_LENGTH) {
        tooLong = true;
      }
      if (!seen.add(value)) {
        duplicated = true;
      }
      normalized.add(value);
    }

    if (blank) {
      violations.add(new FieldViolation("tags", "must not contain blank tags"));
    }
    if (tooLong) {
      violations.add(
          new FieldViolation("tags", "each tag must be at most " + MAX_TAG_LENGTH + " characters"));
    }
    if (duplicated) {
      violations.add(new FieldViolation("tags", "must not contain duplicate tags"));
    }
    return List.copyOf(normalized);
  }
}
