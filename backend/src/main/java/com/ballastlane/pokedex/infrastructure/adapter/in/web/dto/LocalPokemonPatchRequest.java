package com.ballastlane.pokedex.infrastructure.adapter.in.web.dto;

import com.ballastlane.pokedex.domain.model.FieldPatch;
import com.ballastlane.pokedex.domain.model.LocalPokemonPatch;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.util.List;

/**
 * PATCH body. Each setter is only called when the property is present in the JSON, even when its
 * value is null, so "absent" and "explicit null" can be told apart.
 */
public class LocalPokemonPatchRequest {

  private FieldPatch<String> localName = FieldPatch.absent();
  private FieldPatch<String> region = FieldPatch.absent();
  private FieldPatch<List<String>> tags = FieldPatch.absent();

  @JsonSetter("localName")
  void setLocalName(String value) {
    this.localName = FieldPatch.of(value);
  }

  @JsonSetter("region")
  void setRegion(String value) {
    this.region = FieldPatch.of(value);
  }

  @JsonSetter("tags")
  void setTags(List<String> value) {
    this.tags = FieldPatch.of(value);
  }

  public LocalPokemonPatch toPatch() {
    return new LocalPokemonPatch(localName, region, tags);
  }
}
