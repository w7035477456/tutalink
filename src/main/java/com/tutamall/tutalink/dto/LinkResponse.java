package com.tutamall.tutalink.dto;

import java.time.Instant;

public record LinkResponse(
		Long id,
		String code,
		String originalUrl,
		String shortUrl,
		Instant createdAt,
		Instant expiresAt,
		boolean active
) {
}
