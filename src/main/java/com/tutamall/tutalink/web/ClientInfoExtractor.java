package com.tutamall.tutalink.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * Reads real client data behind HAProxy / Cloudflare:
 * IP from CF-Connecting-IP then X-Forwarded-For; country from CF-IPCountry.
 */
@Component
public class ClientInfoExtractor {

	public record ClientInfo(
			String ip,
			String country,
			String referrer,
			String userAgent,
			String utmSource,
			String utmMedium,
			String utmCampaign
	) {
	}

	public ClientInfo extract(HttpServletRequest request) {
		return new ClientInfo(
				resolveIp(request),
				header(request, "CF-IPCountry"),
				header(request, "Referer"),
				header(request, "User-Agent"),
				request.getParameter("utm_source"),
				request.getParameter("utm_medium"),
				request.getParameter("utm_campaign")
		);
	}

	private static String resolveIp(HttpServletRequest request) {
		String cf = header(request, "CF-Connecting-IP");
		if (cf != null) {
			return cf;
		}
		String xff = header(request, "X-Forwarded-For");
		if (xff != null) {
			return xff.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}

	private static String header(HttpServletRequest request, String name) {
		String v = request.getHeader(name);
		return (v == null || v.isBlank()) ? null : v.trim();
	}
}
