package com.ballastlane.pokedex.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ballastlane.pokedex.application.port.out.PokemonCatalogPort;
import com.ballastlane.pokedex.domain.exception.InvalidPaginationException;
import com.ballastlane.pokedex.domain.model.Ability;
import com.ballastlane.pokedex.domain.model.PageRequest;
import com.ballastlane.pokedex.domain.model.PageResult;
import com.ballastlane.pokedex.domain.model.PokemonSummary;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListPokemonServiceTest {

  @Mock private PokemonCatalogPort catalog;

  @InjectMocks private ListPokemonService service;

  private final PokemonSummary bulbasaur =
      new PokemonSummary(
          1,
          "bulbasaur",
          "https://img/1.png",
          "Seed Pokémon",
          69,
          List.of(new Ability("overgrow", false)));

  @Test
  void returnsPageWithSpriteCategoryWeightAndAbilities() {
    PageResult<PokemonSummary> page = new PageResult<>(List.of(bulbasaur), 0, 20, 1025L);
    when(catalog.findPage(any(PageRequest.class))).thenReturn(page);

    PageResult<PokemonSummary> result = service.list(0, 20);

    assertThat(result.content()).containsExactly(bulbasaur);
    assertThat(result.content().get(0).spriteUrl()).isEqualTo("https://img/1.png");
    assertThat(result.content().get(0).category()).isEqualTo("Seed Pokémon");
    assertThat(result.content().get(0).weight()).isEqualTo(69);
    assertThat(result.content().get(0).abilities()).hasSize(1);
    verify(catalog).findPage(new PageRequest(0, 20));
  }

  @Test
  void rejectsInvalidPaginationWithoutCallingCatalog() {
    assertThatThrownBy(() -> service.list(-1, 20)).isInstanceOf(InvalidPaginationException.class);
    assertThatThrownBy(() -> service.list(0, 51)).isInstanceOf(InvalidPaginationException.class);

    verifyNoInteractions(catalog);
  }

  @Test
  void pageOutOfRangeReturnsEmptyContentWithCorrectMetadata() {
    PageResult<PokemonSummary> emptyPage = new PageResult<>(List.of(), 10, 20, 40L);
    when(catalog.findPage(any(PageRequest.class))).thenReturn(emptyPage);

    PageResult<PokemonSummary> result = service.list(10, 20);

    assertThat(result.content()).isEmpty();
    assertThat(result.page()).isEqualTo(10);
    assertThat(result.size()).isEqualTo(20);
    assertThat(result.totalElements()).isEqualTo(40L);
    assertThat(result.totalPages()).isEqualTo(2);
  }
}
