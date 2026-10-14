package com.ballastlane.pokedex.infrastructure.adapter.out.persistence;

import jakarta.persistence.Embeddable;

@Embeddable
class TypeEmbeddable {

  private String name;

  private int slot;

  protected TypeEmbeddable() {}

  TypeEmbeddable(String name, int slot) {
    this.name = name;
    this.slot = slot;
  }

  String getName() {
    return name;
  }

  int getSlot() {
    return slot;
  }
}
