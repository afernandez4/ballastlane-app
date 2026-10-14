package com.ballastlane.pokedex.domain.model;

import java.util.List;

/** Evolution chain flattened into an ordered list of stages (branches included). */
public record EvolutionChain(List<EvolutionStage> stages) {

  public EvolutionChain {
    stages = stages == null ? List.of() : List.copyOf(stages);
  }
}
