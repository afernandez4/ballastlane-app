package com.ballastlane.pokedex.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ballastlane.pokedex.domain.exception.InvalidPokemonDataException;
import java.util.List;
import org.junit.jupiter.api.Test;

class LocalPokemonFieldsTest {

  @Test
  void trimsTextAndLowercasesTags() {
    LocalPokemonFields fields =
        new LocalPokemonFields("  Chispito  ", "  Kanto ", List.of("  Fire ", "STARTER"));

    assertThat(fields.localName()).isEqualTo("Chispito");
    assertThat(fields.region()).isEqualTo("Kanto");
    assertThat(fields.tags()).containsExactly("fire", "starter");
  }

  @Test
  void allowsMissingRegionAndTags() {
    LocalPokemonFields fields = new LocalPokemonFields("Chispito", null, null);

    assertThat(fields.region()).isNull();
    assertThat(fields.tags()).isEmpty();
  }

  @Test
  void rejectsBlankLocalName() {
    assertViolations(() -> new LocalPokemonFields("   ", null, null), "localName");
  }

  @Test
  void rejectsLocalNameLongerThan100() {
    assertViolations(() -> new LocalPokemonFields("a".repeat(101), null, null), "localName");
  }

  @Test
  void acceptsLocalNameWithExactly100Characters() {
    assertThat(new LocalPokemonFields("a".repeat(100), null, null).localName()).hasSize(100);
  }

  @Test
  void rejectsRegionLongerThan100() {
    assertViolations(() -> new LocalPokemonFields("Chispito", "r".repeat(101), null), "region");
  }

  @Test
  void rejectsMoreThanTenTags() {
    List<String> tags = java.util.stream.IntStream.range(0, 11).mapToObj(i -> "t" + i).toList();

    assertViolations(() -> new LocalPokemonFields("Chispito", null, tags), "tags");
  }

  @Test
  void rejectsDuplicateTagsAfterNormalization() {
    assertViolations(
        () -> new LocalPokemonFields("Chispito", null, List.of("Fire", " fire ")), "tags");
  }

  @Test
  void rejectsTagLongerThan30() {
    assertViolations(
        () -> new LocalPokemonFields("Chispito", null, List.of("x".repeat(31))), "tags");
  }

  @Test
  void rejectsBlankTag() {
    assertViolations(() -> new LocalPokemonFields("Chispito", null, List.of("  ")), "tags");
  }

  @Test
  void reportsEveryViolationAtOnce() {
    assertThatThrownBy(() -> new LocalPokemonFields(" ", "r".repeat(101), List.of("a", "a")))
        .isInstanceOfSatisfying(
            InvalidPokemonDataException.class, ex -> assertThat(ex.getViolations()).hasSize(3));
  }

  private static void assertViolations(Runnable action, String field) {
    assertThatThrownBy(action::run)
        .isInstanceOfSatisfying(
            InvalidPokemonDataException.class,
            ex ->
                assertThat(ex.getViolations())
                    .extracting(InvalidPokemonDataException.FieldViolation::field)
                    .contains(field));
  }
}
