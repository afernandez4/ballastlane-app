package com.ballastlane.pokedex.domain.model;

import java.util.List;

/** Partial update of the user-editable fields (PATCH). */
public record LocalPokemonPatch(
    FieldPatch<String> localName, FieldPatch<String> region, FieldPatch<List<String>> tags) {

  public boolean isEmpty() {
    return !localName.present() && !region.present() && !tags.present();
  }
}
