package com.ballastlane.pokedex.domain.model;

import java.time.Instant;

/** A bearer token and the instant it stops being valid. */
public record IssuedToken(String value, Instant expiresAt) {}
