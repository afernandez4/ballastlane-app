package com.ballastlane.pokedex.infrastructure.adapter.out.pokeapi;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/** Loads JSON fixtures captured from the real PokeAPI. */
final class TestFixtures {

  private TestFixtures() {}

  static String read(String fileName) {
    String path = "/fixtures/pokeapi/" + fileName;
    try (InputStream in = TestFixtures.class.getResourceAsStream(path)) {
      if (in == null) {
        throw new IllegalArgumentException("Missing fixture: " + path);
      }
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
