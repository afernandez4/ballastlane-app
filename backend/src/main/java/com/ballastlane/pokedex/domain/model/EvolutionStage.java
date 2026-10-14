package com.ballastlane.pokedex.domain.model;

/**
 * One species in an evolution chain.
 *
 * @param speciesName species name, e.g. "charmeleon"
 * @param pokeApiId PokeAPI species id
 * @param stage depth in the chain (0 = base form)
 * @param minLevel level required to evolve into this stage, or {@code null}
 * @param trigger evolution trigger such as "level-up" or "use-item", or {@code null}
 */
public record EvolutionStage(
    String speciesName, int pokeApiId, int stage, Integer minLevel, String trigger) {}
