package com.ballastlane.pokedex.domain.model;

import java.util.List;

/** Page of results, independent of Spring Data. */
public record PageResult<T>(List<T> content, int page, int size, long totalElements) {

  public PageResult {
    content = content == null ? List.of() : List.copyOf(content);
  }

  public int totalPages() {
    return size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
  }
}
