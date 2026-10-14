package com.ballastlane.pokedex.domain.service;

import com.ballastlane.pokedex.domain.model.EvolutionChain;
import com.ballastlane.pokedex.domain.model.EvolutionStage;
import java.util.ArrayList;
import java.util.List;

/**
 * Turns the evolution tree returned by PokeAPI into an ordered list of stages. Branches (for
 * example Eevee) are kept: stages are ordered by depth, then by their position in the tree.
 */
public final class EvolutionChainFlattener {

  private EvolutionChainFlattener() {}

  /** Tree node as received from the source. {@code minLevel} and {@code trigger} may be null. */
  public record EvolutionNode(
      String speciesName,
      int pokeApiId,
      Integer minLevel,
      String trigger,
      List<EvolutionNode> evolvesTo) {

    public EvolutionNode {
      evolvesTo = evolvesTo == null ? List.of() : List.copyOf(evolvesTo);
    }
  }

  public static EvolutionChain flatten(EvolutionNode root) {
    List<EvolutionStage> stages = new ArrayList<>();
    List<EvolutionNode> level = List.of(root);
    int depth = 0;

    while (!level.isEmpty()) {
      List<EvolutionNode> next = new ArrayList<>();
      for (EvolutionNode node : level) {
        stages.add(
            new EvolutionStage(
                node.speciesName(), node.pokeApiId(), depth, node.minLevel(), node.trigger()));
        next.addAll(node.evolvesTo());
      }
      level = next;
      depth++;
    }
    return new EvolutionChain(stages);
  }
}
