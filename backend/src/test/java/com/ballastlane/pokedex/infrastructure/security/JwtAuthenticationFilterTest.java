package com.ballastlane.pokedex.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ballastlane.pokedex.application.port.out.TokenProviderPort;
import com.ballastlane.pokedex.domain.model.AuthenticatedUser;
import com.ballastlane.pokedex.domain.model.Role;
import jakarta.servlet.FilterChain;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthenticationFilterTest {

  private final TokenProviderPort tokens = mock(TokenProviderPort.class);
  private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(tokens);
  private final FilterChain chain = mock(FilterChain.class);

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void validBearerTokenAuthenticatesWithRoleAuthority() throws Exception {
    when(tokens.verify("good")).thenReturn(Optional.of(new AuthenticatedUser("admin", Role.ADMIN)));
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer good");

    filter.doFilter(request, new MockHttpServletResponse(), chain);

    var authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication.getName()).isEqualTo("admin");
    assertThat(authentication.getAuthorities())
        .extracting(Object::toString)
        .containsExactly("ROLE_ADMIN");
    verify(chain)
        .doFilter(org.mockito.ArgumentMatchers.eq(request), org.mockito.ArgumentMatchers.any());
  }

  @Test
  void invalidOrExpiredTokenLeavesRequestAnonymous() throws Exception {
    when(tokens.verify(anyString())).thenReturn(Optional.empty());
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer expired");

    filter.doFilter(request, new MockHttpServletResponse(), chain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  void missingOrNonBearerHeaderIsIgnored() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Basic abc");

    filter.doFilter(request, new MockHttpServletResponse(), chain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }
}
