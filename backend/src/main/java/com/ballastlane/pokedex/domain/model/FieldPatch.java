package com.ballastlane.pokedex.domain.model;

/**
 * A partial update for one field. {@code present=false} means "not sent, keep the current value";
 * {@code present=true} with a null value means "explicitly cleared".
 */
public record FieldPatch<T>(boolean present, T value) {

  public static <T> FieldPatch<T> absent() {
    return new FieldPatch<>(false, null);
  }

  public static <T> FieldPatch<T> of(T value) {
    return new FieldPatch<>(true, value);
  }
}
