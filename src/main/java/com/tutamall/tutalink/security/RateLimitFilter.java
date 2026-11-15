package com.tutamall.tutalink.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Rate-limits POST /tutalink/api/links to 30 req/min per API key using Redis INCR + EXPIRE.
 * Fail-open if Redis is down (allow the request, log a warning).
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);
	private static final int LIMIT = 30;

	private final StringRedisTemplate redis;

	public RateLimitFilter(StringRedisTemplate redis) {
		this.redis = redis;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return !(HttpMethod.POST.matches(request.getMethod())
				&& "/tutalink/api/links".equals(request.getRequestURI()));
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String apiKey = request.getHeader(ApiKeyFilter.HEADER);
		String bucket = apiKey != null ? apiKey : request.getRemoteAddr();
		String redisKey = "tutalink:ratelimit:" + bucket;
		try {
			Long count = redis.opsForValue().increment(redisKey);
			if (count != null && count == 1L) {
				redis.expire(redisKey, Duration.ofMinutes(1));
			}
			if (count != null && count > LIMIT) {
				response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
				response.setContentType("application/problem+json");
				response.getWriter().write("""
						{"type":"about:blank","title":"Too Many Requests","status":429,"detail":"Rate limit 30/min exceeded"}
						""");
				return;
			}
		} catch (Exception ex) {
			log.warn("Redis rate-limit unavailable — fail open: {}", ex.getMessage());
		}
		filterChain.doFilter(request, response);
	}
}
