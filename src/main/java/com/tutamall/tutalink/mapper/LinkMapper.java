package com.tutamall.tutalink.mapper;

import com.tutamall.tutalink.config.TutalinkProperties;
import com.tutamall.tutalink.dto.LinkResponse;
import com.tutamall.tutalink.entity.Link;
import org.springframework.stereotype.Component;

/** {@code @Component} — generic Spring-managed bean (neither @Service nor @Repository). */
@Component
public class LinkMapper {

	private final TutalinkProperties properties;

	public LinkMapper(TutalinkProperties properties) {
		this.properties = properties;
	}

	public LinkResponse toResponse(Link link) {
		String base = properties.baseUrl();
		if (!base.endsWith("/")) {
			base = base + "/";
		}
		return new LinkResponse(
				link.getId(),
				link.getCode(),
				link.getOriginalUrl(),
				base + link.getCode(),
				link.getCreatedAt(),
				link.getExpiresAt(),
				link.isActive()
		);
	}
}
