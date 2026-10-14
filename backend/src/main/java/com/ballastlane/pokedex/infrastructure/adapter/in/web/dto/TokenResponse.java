package com.ballastlane.pokedex.infrastructure.adapter.in.web.dto;

import java.time.Instant;

public record TokenResponse(String accessToken, String tokenType, Instant expiresAt) {}
