package com.ballastlane.pokedex.infrastructure.adapter.out.pokeapi;

import com.ballastlane.pokedex.domain.model.Ability;
import com.ballastlane.pokedex.domain.model.EvolutionChain;
import com.ballastlane.pokedex.domain.model.PokemonDetail;
import com.ballastlane.pokedex.domain.model.PokemonSummary;
import com.ballastlane.pokedex.domain.model.PokemonType;
import com.ballastlane.pokedex.domain.model.Stat;
import com.ballastlane.pokedex.domain.service.EvolutionChainFlattener;
import com.ballastlane.pokedex.infrastructure.adapter.out.pokeapi.PokeApiDtos.AbilitySlot;
import com.ballastlane.pokedex.infrastructure.adapter.out.pokeapi.PokeApiDtos.ChainLink;
import com.ballastlane.pokedex.infrastructure.adapter.out.pokeapi.PokeApiDtos.EvolutionChainResponse;
import com.ballastlane.pokedex.infrastructure.adapter.out.pokeapi.PokeApiDtos.FlavorText;
import com.ballastlane.pokedex.infrastructure.adapter.out.pokeapi.PokeApiDtos.Genus;
import com.ballastlane.pokedex.infrastructure.adapter.out.pokeapi.PokeApiDtos.PokemonResponse;
import com.ballastlane.pokedex.infrastructure.adapter.out.pokeapi.PokeApiDtos.SpeciesResponse;
import com.ballastlane.pokedex.infrastructure.adapter.out.pokeapi.PokeApiDtos.StatSlot;
import com.ballastlane.pokedex.infrastructure.adapter.out.pokeapi.PokeApiDtos.TypeSlot;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Converts PokeAPI transport objects into domain objects. Pure functions, no I/O. */
final class PokeApiMapper {

  private static final String ENGLISH = "en";

  private PokeApiMapper() {}

  static PokemonSummary toSummary(int id, PokemonResponse pokemon, SpeciesResponse species) {
    return new PokemonSummary(
        id,
        pokemon.name(),
        pokemon.sprites() == null ? null : pokemon.sprites().frontDefault(),
        category(species),
        orZero(pokemon.weight()),
        abilities(pokemon));
  }

  static PokemonDetail toDetail(
      PokemonResponse pokemon,
      SpeciesResponse species,
      EvolutionChainResponse chain,
      List<String> preferredLanguages) {
    return new PokemonDetail(
        pokemon.id(),
        pokemon.name(),
        imageUrl(pokemon),
        orZero(pokemon.height()),
        orZero(pokemon.weight()),
        types(pokemon.types()),
        stats(pokemon.stats()),
        description(species, preferredLanguages),
        chain == null ? new EvolutionChain(List.of()) : toEvolutionChain(chain));
  }

  /** Genus in English, e.g. "Seed Pokémon". Empty when the species has none. */
  static String category(SpeciesResponse species) {
    return species.genera().stream()
        .filter(g -> isLanguage(g.language(), ENGLISH))
        .map(Genus::genus)
        .findFirst()
        .orElse("");
  }

  /**
   * First flavour text whose language matches the earliest preferred language. Line breaks and form
   * feeds are replaced by spaces. Empty when no preferred language matches.
   */
  static String description(SpeciesResponse species, List<String> preferredLanguages) {
    for (String language : preferredLanguages) {
      Optional<FlavorText> match =
          species.flavorTextEntries().stream()
              .filter(f -> isLanguage(f.language(), language))
              .filter(f -> f.flavorText() != null)
              .findFirst();
      if (match.isPresent()) {
        return clean(match.get().flavorText());
      }
    }
    return "";
  }

  static EvolutionChain toEvolutionChain(EvolutionChainResponse response) {
    return new EvolutionChain(EvolutionChainFlattener.flatten(toNode(response.chain())).stages());
  }

  /** Numeric id from a PokeAPI resource URL such as {@code .../pokemon/25/}. */
  static int idFromUrl(String url) {
    String trimmed = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    return Integer.parseInt(trimmed.substring(trimmed.lastIndexOf('/') + 1));
  }

  private static EvolutionChainFlattener.EvolutionNode toNode(ChainLink link) {
    var detail = link.evolutionDetails().isEmpty() ? null : link.evolutionDetails().get(0);
    return new EvolutionChainFlattener.EvolutionNode(
        link.species().name(),
        idFromUrl(link.species().url()),
        detail == null ? null : detail.minLevel(),
        detail == null || detail.trigger() == null ? null : detail.trigger().name(),
        link.evolvesTo().stream().map(PokeApiMapper::toNode).toList());
  }

  private static String imageUrl(PokemonResponse pokemon) {
    if (pokemon.sprites() == null) {
      return null;
    }
    var sprites = pokemon.sprites();
    boolean hasOfficial =
        sprites.other() != null
            && sprites.other().officialArtwork() != null
            && sprites.other().officialArtwork().frontDefault() != null;
    return hasOfficial ? sprites.other().officialArtwork().frontDefault() : sprites.frontDefault();
  }

  private static List<PokemonType> types(List<TypeSlot> slots) {
    return slots.stream()
        .sorted(Comparator.comparingInt(TypeSlot::slot))
        .map(t -> new PokemonType(t.type().name(), t.slot()))
        .toList();
  }

  private static List<Stat> stats(List<StatSlot> slots) {
    return slots.stream().map(s -> new Stat(s.stat().name(), s.baseStat())).toList();
  }

  private static List<Ability> abilities(PokemonResponse pokemon) {
    return pokemon.abilities().stream().map(PokeApiMapper::toAbility).toList();
  }

  private static Ability toAbility(AbilitySlot slot) {
    return new Ability(slot.ability().name(), slot.hidden());
  }

  private static boolean isLanguage(PokeApiDtos.NamedResource language, String code) {
    return language != null && code.equals(language.name());
  }

  private static String clean(String text) {
    return text.replace('\n', ' ').replace('\f', ' ').replaceAll("\\s+", " ").trim();
  }

  private static int orZero(Integer value) {
    return Objects.requireNonNullElse(value, 0);
  }
}
