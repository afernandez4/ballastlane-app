package com.ballastlane.pokedex.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ballastlane.pokedex.domain.exception.InvalidPaginationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PageRequestTest {

  @Test
  void acceptsValidPageAndSize() {
    PageRequest request = new PageRequest(0, 20);

    assertThat(request.page()).isZero();
    assertThat(request.size()).isEqualTo(20);
  }

  @Test
  void acceptsMaximumSize() {
    assertThat(new PageRequest(3, 50).size()).isEqualTo(50);
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, -10})
  void rejectsNegativePage(int page) {
    assertThatThrownBy(() -> new PageRequest(page, 20))
        .isInstanceOf(InvalidPaginationException.class);
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -5, 51, 1000})
  void rejectsSizeOutOfRange(int size) {
    assertThatThrownBy(() -> new PageRequest(0, size))
        .isInstanceOf(InvalidPaginationException.class);
  }
}
