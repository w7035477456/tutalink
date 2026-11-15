package com.tutamall.tutalink.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Adds X-Tutalink-Node so round-robin across web hosts (xbox0 ALL-IN-ONE; xbox2+xbox3 MINI_PRODUCTION) can be verified. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class NodeHeaderFilter extends OncePerRequestFilter {

	private final String hostname;

	public NodeHeaderFilter() {
		String host;
		try {
			host = InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
			host = "unknown";
		}
		this.hostname = host;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		response.setHeader("X-Tutalink-Node", hostname);
		filterChain.doFilter(request, response);
	}
}
