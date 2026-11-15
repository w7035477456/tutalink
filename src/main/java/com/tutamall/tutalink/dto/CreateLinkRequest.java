package com.tutamall.tutalink.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import org.hibernate.validator.constraints.URL;

/**
 * Request body for POST /tutalink/api/links.
 * {@code @Valid} on the controller triggers these Bean Validation constraints.
 */
public record CreateLinkRequest(
		@NotBlank(message = "url is required")
		@URL(message = "url must be a valid HTTP(S) URL")
		String url,

		@Pattern(regexp = "^[a-zA-Z0-9_-]{3,32}$", message = "customCode must be 3-32 alphanumeric, _ or -")
		String customCode,

		@Future(message = "expiresAt must be in the future")
		Instant expiresAt
) {
}
