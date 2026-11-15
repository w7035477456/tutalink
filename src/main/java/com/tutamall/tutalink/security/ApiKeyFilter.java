package com.tutamall.tutalink.security;

import com.tutamall.tutalink.config.TutalinkProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Custom {@link OncePerRequestFilter} that authenticates /tutalink/api/** via X-API-Key.
 * Sets a simple Authentication in the SecurityContext when the key matches.
 */
@Component
public class ApiKeyFilter extends OncePerRequestFilter {

	public static final String HEADER = "X-API-Key";

	private final String expectedKey;

	public ApiKeyFilter(TutalinkProperties properties) {
		this.expectedKey = properties.apiKey();
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		return !path.startsWith("/tutalink/api/");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String key = request.getHeader(HEADER);
		if (expectedKey == null || expectedKey.isBlank() || key == null || !expectedKey.equals(key)) {
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
			response.setContentType("application/problem+json");
			response.getWriter().write("""
					{"type":"about:blank","title":"Unauthorized","status":401,"detail":"Invalid or missing X-API-Key"}
					""");
			return;
		}
		var auth = new UsernamePasswordAuthenticationToken(
				"tutalink-admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
		SecurityContextHolder.getContext().setAuthentication(auth);
		filterChain.doFilter(request, response);
	}
}
