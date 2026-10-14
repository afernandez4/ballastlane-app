package com.ballastlane.pokedex.infrastructure.adapter.out.pokeapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Transport objects for PokeAPI. Only the fields the application uses are declared; unknown fields
 * are ignored so that PokeAPI can add new ones without breaking us.
 */
public final class PokeApiDtos {

  private PokeApiDtos() {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record NamedResource(String name, String url) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record PokemonListResponse(int count, List<NamedResource> results) {

    public PokemonListResponse {
      results = results == null ? List.of() : List.copyOf(results);
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record PokemonResponse(
      int id,
      String name,
      Integer height,
      Integer weight,
      Sprites sprites,
      List<TypeSlot> types,
      List<StatSlot> stats,
      List<AbilitySlot> abilities,
      NamedResource species) {

    public PokemonResponse {
      types = types == null ? List.of() : types;
      stats = stats == null ? List.of() : stats;
      abilities = abilities == null ? List.of() : abilities;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Sprites(@JsonProperty("front_default") String frontDefault, Other other) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Other(@JsonProperty("official-artwork") OfficialArtwork officialArtwork) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OfficialArtwork(@JsonProperty("front_default") String frontDefault) {}
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record TypeSlot(int slot, NamedResource type) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record StatSlot(@JsonProperty("base_stat") int baseStat, NamedResource stat) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record AbilitySlot(NamedResource ability, @JsonProperty("is_hidden") boolean hidden) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record SpeciesResponse(
      int id,
      String name,
      List<Genus> genera,
      @JsonProperty("flavor_text_entries") List<FlavorText> flavorTextEntries,
      @JsonProperty("evolution_chain") NamedUrl evolutionChain) {

    public SpeciesResponse {
      genera = genera == null ? List.of() : genera;
      flavorTextEntries = flavorTextEntries == null ? List.of() : flavorTextEntries;
    }
  }

  /** A bare {@code {"url": ...}} reference, used by species.evolution_chain. */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record NamedUrl(String url) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Genus(String genus, NamedResource language) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record FlavorText(
      @JsonProperty("flavor_text") String flavorText, NamedResource language) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record EvolutionChainResponse(int id, ChainLink chain) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record ChainLink(
      NamedResource species,
      @JsonProperty("evolution_details") List<EvolutionDetail> evolutionDetails,
      @JsonProperty("evolves_to") List<ChainLink> evolvesTo) {

    public ChainLink {
      evolutionDetails = evolutionDetails == null ? List.of() : evolutionDetails;
      evolvesTo = evolvesTo == null ? List.of() : evolvesTo;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record EvolutionDetail(
      @JsonProperty("min_level") Integer minLevel, NamedResource trigger) {}
}
