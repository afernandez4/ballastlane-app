package com.ballastlane.pokedex.infrastructure.adapter.in.web.dto;

import com.ballastlane.pokedex.domain.model.LocalPokemonFields;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record LocalPokemonCreateRequest(
    @Positive int pokeApiId,
    @NotBlank @Size(max = 100) String localName,
    @Size(max = 100) String region,
    @Size(max = 10) List<@NotNull @Size(max = 30) String> tags) {

  public LocalPokemonFields toFields() {
    return new LocalPokemonFields(localName, region, tags);
  }
}
