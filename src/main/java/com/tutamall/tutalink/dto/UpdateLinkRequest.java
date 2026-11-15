package com.tutamall.tutalink.dto;

import jakarta.validation.constraints.Future;
import java.time.Instant;

public record UpdateLinkRequest(
		Boolean active,
		@Future(message = "expiresAt must be in the future")
		Instant expiresAt
) {
}
