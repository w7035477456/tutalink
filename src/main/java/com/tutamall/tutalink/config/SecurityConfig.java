package com.tutamall.tutalink.config;

import com.tutamall.tutalink.security.ApiKeyFilter;
import com.tutamall.tutalink.security.RateLimitFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Stateless SecurityFilterChain. Public: redirects, health, OpenAPI.
 * Everything under /tutalink/api/** requires the API key filter.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final ApiKeyFilter apiKeyFilter;
	private final RateLimitFilter rateLimitFilter;

	public SecurityConfig(ApiKeyFilter apiKeyFilter, RateLimitFilter rateLimitFilter) {
		this.apiKeyFilter = apiKeyFilter;
		this.rateLimitFilter = rateLimitFilter;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.GET, "/l/**").permitAll()
						.requestMatchers("/tutalink/actuator/health", "/tutalink/actuator/health/**").permitAll()
						.requestMatchers("/tutalink/api/docs", "/tutalink/api/docs/**",
								"/v3/api-docs/**", "/swagger-ui/**").permitAll()
						.requestMatchers("/tutalink/api/**").authenticated()
						.anyRequest().permitAll()
				)
				.httpBasic(Customizer.withDefaults())
				.addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
				.addFilterAfter(rateLimitFilter, ApiKeyFilter.class);
		return http.build();
	}
}
