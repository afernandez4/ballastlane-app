package com.ballastlane.pokedex.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.ballastlane.pokedex.domain.model.EvolutionChain;
import com.ballastlane.pokedex.domain.model.EvolutionStage;
import java.util.List;
import org.junit.jupiter.api.Test;

class EvolutionChainFlattenerTest {

  @Test
  void flattensLinearChainInStageOrder() {
    var charmander =
        new EvolutionChainFlattener.EvolutionNode(
            "charmander",
            4,
            null,
            null,
            List.of(
                new EvolutionChainFlattener.EvolutionNode(
                    "charmeleon",
                    5,
                    16,
                    "level-up",
                    List.of(
                        new EvolutionChainFlattener.EvolutionNode(
                            "charizard", 6, 36, "level-up", List.of())))));

    EvolutionChain chain = EvolutionChainFlattener.flatten(charmander);

    assertThat(chain.stages())
        .containsExactly(
            new EvolutionStage("charmander", 4, 0, null, null),
            new EvolutionStage("charmeleon", 5, 1, 16, "level-up"),
            new EvolutionStage("charizard", 6, 2, 36, "level-up"));
  }

  @Test
  void flattensBranchingChainsLikeEevee() {
    var eevee =
        new EvolutionChainFlattener.EvolutionNode(
            "eevee",
            133,
            null,
            null,
            List.of(
                new EvolutionChainFlattener.EvolutionNode(
                    "vaporeon", 134, null, "use-item", List.of()),
                new EvolutionChainFlattener.EvolutionNode(
                    "jolteon", 135, null, "use-item", List.of()),
                new EvolutionChainFlattener.EvolutionNode(
                    "flareon", 136, null, "use-item", List.of())));

    EvolutionChain chain = EvolutionChainFlattener.flatten(eevee);

    assertThat(chain.stages())
        .extracting(EvolutionStage::speciesName)
        .containsExactly("eevee", "vaporeon", "jolteon", "flareon");
    assertThat(chain.stages()).extracting(EvolutionStage::stage).containsExactly(0, 1, 1, 1);
  }

  @Test
  void singleSpeciesProducesOneStage() {
    var ditto = new EvolutionChainFlattener.EvolutionNode("ditto", 132, null, null, List.of());

    assertThat(EvolutionChainFlattener.flatten(ditto).stages()).hasSize(1);
  }
}
