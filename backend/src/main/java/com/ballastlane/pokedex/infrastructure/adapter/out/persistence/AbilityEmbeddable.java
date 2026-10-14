package com.ballastlane.pokedex.infrastructure.adapter.out.persistence;

import jakarta.persistence.Embeddable;

@Embeddable
class AbilityEmbeddable {

  private String name;

  private boolean hidden;

  protected AbilityEmbeddable() {}

  AbilityEmbeddable(String name, boolean hidden) {
    this.name = name;
    this.hidden = hidden;
  }

  String getName() {
    return name;
  }

  boolean isHidden() {
    return hidden;
  }
}
