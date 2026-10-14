package com.ballastlane.pokedex.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ballastlane.pokedex.application.port.out.PokemonCatalogPort;
import com.ballastlane.pokedex.domain.exception.PokemonNotFoundException;
import com.ballastlane.pokedex.domain.model.EvolutionChain;
import com.ballastlane.pokedex.domain.model.EvolutionStage;
import com.ballastlane.pokedex.domain.model.PokemonDetail;
import com.ballastlane.pokedex.domain.model.PokemonType;
import com.ballastlane.pokedex.domain.model.Stat;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetPokemonDetailServiceTest {

  private static final List<String> PREFERRED_LANGUAGES = List.of("en");

  @Mock private PokemonCatalogPort catalog;

  @InjectMocks private GetPokemonDetailService service;

  private final PokemonDetail pikachu =
      new PokemonDetail(
          25,
          "pikachu",
          "https://img/25.png",
          4,
          60,
          List.of(new PokemonType("electric", 1)),
          List.of(new Stat("hp", 35)),
          "Cuando se enfada, libera electricidad.",
          new EvolutionChain(
              List.of(
                  new EvolutionStage("pichu", 172, 0, null, null),
                  new EvolutionStage("pikachu", 25, 1, null, null),
                  new EvolutionStage("raichu", 26, 2, null, "use-item"))));

  @Test
  void combinesDetailSpeciesAndEvolutionChain() {
    when(catalog.findDetail(anyString(), anyList())).thenReturn(Optional.of(pikachu));

    PokemonDetail result = service.get("25");

    assertThat(result).isEqualTo(pikachu);
    assertThat(result.evolutionChain().stages()).hasSize(3);
    assertThat(result.description()).isEqualTo("Cuando se enfada, libera electricidad.");
  }

  @Test
  void searchesByIdOrNameNormalizedToLowercase() {
    when(catalog.findDetail(anyString(), anyList())).thenReturn(Optional.of(pikachu));

    service.get("  PiKaChU  ");
    service.get("25");

    verify(catalog).findDetail("pikachu", PREFERRED_LANGUAGES);
    verify(catalog).findDetail("25", PREFERRED_LANGUAGES);
  }

  @Test
  void unknownPokemonThrowsNotFound() {
    when(catalog.findDetail(anyString(), anyList())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.get("missingno"))
        .isInstanceOf(PokemonNotFoundException.class)
        .hasMessageContaining("missingno");
  }

  @Test
  void blankIdentifierThrowsNotFoundWithoutCallingCatalog() {
    assertThatThrownBy(() -> service.get("   ")).isInstanceOf(PokemonNotFoundException.class);
  }

  @Test
  void asksCatalogForEnglishDescription() {
    when(catalog.findDetail(anyString(), anyList())).thenReturn(Optional.of(pikachu));

    service.get("pikachu");

    verify(catalog).findDetail("pikachu", List.of("en"));
  }
}
