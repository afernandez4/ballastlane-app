package com.ballastlane.pokedex.domain.model;

/** Application user. {@code id} is {@code null} until the user is persisted. */
public record User(Long id, String username, String email, String passwordHash, Role role) {}
