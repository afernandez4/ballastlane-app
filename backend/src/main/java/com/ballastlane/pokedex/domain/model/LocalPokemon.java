package com.ballastlane.pokedex.domain.model;

import com.ballastlane.pokedex.domain.exception.InvalidPokemonDataException;
import com.ballastlane.pokedex.domain.exception.InvalidPokemonDataException.FieldViolation;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Pokémon stored locally. Base data is synchronised from PokeAPI; {@code localName}, {@code region}
 * and {@code tags} are owned by this application (US03/US04).
 */
public record LocalPokemon(
    Long id,
    int pokeApiId,
    String name,
    String spriteUrl,
    String category,
    Integer height,
    Integer weight,
    List<PokemonType> types,
    List<Ability> abilities,
    String localName,
    String region,
    List<String> tags,
    long version,
    Instant createdAt,
    Instant updatedAt) {

  public LocalPokemon {
    List<FieldViolation> violations = new ArrayList<>();
    if (height != null && height <= 0) {
      violations.add(new FieldViolation("height", "must be greater than 0"));
    }
    if (weight != null && weight <= 0) {
      violations.add(new FieldViolation("weight", "must be greater than 0"));
    }
    if (!violations.isEmpty()) {
      throw new InvalidPokemonDataException(violations);
    }

    types = types == null ? List.of() : List.copyOf(types);
    abilities = abilities == null ? List.of() : List.copyOf(abilities);
    tags = tags == null ? List.of() : List.copyOf(tags);
  }

  /**
   * Returns a copy with the user-editable fields replaced. {@code pokeApiId} and the base data are
   * kept as they are.
   */
  public LocalPokemon withOwnFields(LocalPokemonFields fields) {
    return new LocalPokemon(
        id,
        pokeApiId,
        name,
        spriteUrl,
        category,
        height,
        weight,
        types,
        abilities,
        fields.localName(),
        fields.region(),
        fields.tags(),
        version,
        createdAt,
        updatedAt);
  }

  /** New local row from data synchronised from PokeAPI. Own fields start empty. */
  public static LocalPokemon fromBaseData(int pokeApiId, PokemonBaseData base, Instant now) {
    return new LocalPokemon(
        null,
        pokeApiId,
        base.name(),
        base.spriteUrl(),
        base.category(),
        base.height(),
        base.weight(),
        base.types(),
        base.abilities(),
        null,
        null,
        List.of(),
        0L,
        now,
        now);
  }

  /** Replaces the synchronised data and keeps own fields, id, version and creation time. */
  public LocalPokemon withBaseData(PokemonBaseData base, Instant now) {
    return new LocalPokemon(
        id,
        pokeApiId,
        base.name(),
        base.spriteUrl(),
        base.category(),
        base.height(),
        base.weight(),
        base.types(),
        base.abilities(),
        localName,
        region,
        tags,
        version,
        createdAt,
        now);
  }
}
