package com.ballastlane.pokedex.domain.model;

import com.ballastlane.pokedex.domain.exception.InvalidPaginationException;

/** Pagination request, independent of Spring Data. Zero-based page index. */
public record PageRequest(int page, int size) {

  public static final int MAX_SIZE = 50;

  public PageRequest {
    if (page < 0) {
      throw new InvalidPaginationException("page must be >= 0, was " + page);
    }
    if (size < 1 || size > MAX_SIZE) {
      throw new InvalidPaginationException(
          "size must be between 1 and " + MAX_SIZE + ", was " + size);
    }
  }
}
