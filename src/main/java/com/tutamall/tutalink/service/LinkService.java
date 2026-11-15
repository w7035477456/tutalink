package com.tutamall.tutalink.service;

import com.tutamall.tutalink.dto.CreateLinkRequest;
import com.tutamall.tutalink.dto.LinkResponse;
import com.tutamall.tutalink.dto.UpdateLinkRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LinkService {
	LinkResponse create(CreateLinkRequest request);
	LinkResponse getByCode(String code);
	Page<LinkResponse> list(Pageable pageable);
	LinkResponse update(String code, UpdateLinkRequest request);
	void delete(String code);
	/** Resolve for public redirect — throws 404/410 as needed. Returns original URL. */
	String resolveForRedirect(String code);
}
