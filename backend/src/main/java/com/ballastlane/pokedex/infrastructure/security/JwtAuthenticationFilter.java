package com.ballastlane.pokedex.infrastructure.security;

import com.ballastlane.pokedex.application.port.out.TokenProviderPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Reads {@code Authorization: Bearer <jwt>}. A valid token sets the authentication; anything else
 * leaves the request anonymous, so protected endpoints answer 401 through the entry point.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String PREFIX = "Bearer ";

  private final TokenProviderPort tokens;

  public JwtAuthenticationFilter(TokenProviderPort tokens) {
    this.tokens = tokens;
  }

  @Override
  protected void doFilterInternal(
      jakarta.servlet.http.HttpServletRequest request,
      jakarta.servlet.http.HttpServletResponse response,
      FilterChain chain)
      throws ServletException, IOException {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header != null && header.startsWith(PREFIX)) {
      tokens
          .verify(header.substring(PREFIX.length()).trim())
          .ifPresent(
              user ->
                  SecurityContextHolder.getContext()
                      .setAuthentication(
                          new UsernamePasswordAuthenticationToken(
                              user.username(),
                              null,
                              List.of(new SimpleGrantedAuthority("ROLE_" + user.role().name())))));
    }
    chain.doFilter(request, response);
  }
}
