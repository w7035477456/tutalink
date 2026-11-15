package com.tutamall.tutalink.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code @ConfigurationProperties} binds typed config from application.yml / env under prefix
 * {@code tutalink}. Using a record keeps it immutable.
 */
@ConfigurationProperties(prefix = "tutalink")
public record TutalinkProperties(
		String baseUrl,
		int codeLength,
		Duration cacheTtl,
		String apiKey,
		String ipSalt
) {
}
