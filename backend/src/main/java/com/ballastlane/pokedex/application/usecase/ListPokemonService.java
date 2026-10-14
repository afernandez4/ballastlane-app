package com.ballastlane.pokedex.application.usecase;

import com.ballastlane.pokedex.application.port.in.ListPokemonUseCase;
import com.ballastlane.pokedex.application.port.out.PokemonCatalogPort;
import com.ballastlane.pokedex.domain.model.PageRequest;
import com.ballastlane.pokedex.domain.model.PageResult;
import com.ballastlane.pokedex.domain.model.PokemonSummary;

/** US01: validates pagination and delegates the catalogue lookup to the outbound port. */
public class ListPokemonService implements ListPokemonUseCase {

  private final PokemonCatalogPort catalog;

  public ListPokemonService(PokemonCatalogPort catalog) {
    this.catalog = catalog;
  }

  @Override
  public PageResult<PokemonSummary> list(int page, int size) {
    return catalog.findPage(new PageRequest(page, size));
  }
}
