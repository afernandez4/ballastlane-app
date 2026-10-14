package com.ballastlane.pokedex.infrastructure.adapter.in.web.dto;

/** Validation happens in the domain (Registration) so every rule has one definition. */
public record RegisterRequest(String username, String email, String password) {}
