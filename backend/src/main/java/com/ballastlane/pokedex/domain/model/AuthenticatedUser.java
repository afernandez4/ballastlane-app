package com.ballastlane.pokedex.domain.model;

/** Identity recovered from a valid token. */
public record AuthenticatedUser(String username, Role role) {}
