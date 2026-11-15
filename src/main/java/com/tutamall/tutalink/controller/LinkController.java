package com.tutamall.tutalink.controller;

import com.tutamall.tutalink.dto.CreateLinkRequest;
import com.tutamall.tutalink.dto.LinkResponse;
import com.tutamall.tutalink.dto.StatsResponse;
import com.tutamall.tutalink.dto.UpdateLinkRequest;
import com.tutamall.tutalink.service.LinkService;
import com.tutamall.tutalink.service.StatsService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code @RestController} = {@code @Controller} + {@code @ResponseBody}.
 * Returns JSON directly (no view resolver). Contrast with {@code @Controller}
 * which typically returns a view name for server-side rendering.
 */
@RestController
@RequestMapping("/tutalink/api/links")
public class LinkController {

	private final LinkService linkService;
	private final StatsService statsService;

	public LinkController(LinkService linkService, StatsService statsService) {
		this.linkService = linkService;
		this.statsService = statsService;
	}

	@PostMapping
	public ResponseEntity<LinkResponse> create(@Valid @RequestBody CreateLinkRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(linkService.create(request));
	}

	@GetMapping
	public Page<LinkResponse> list(
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		return linkService.list(pageable);
	}

	@GetMapping("/{code}")
	public LinkResponse get(@PathVariable String code) {
		return linkService.getByCode(code);
	}

	@GetMapping("/{code}/stats")
	public StatsResponse stats(@PathVariable String code) {
		return statsService.statsFor(code);
	}

	@PatchMapping("/{code}")
	public LinkResponse update(@PathVariable String code, @Valid @RequestBody UpdateLinkRequest request) {
		return linkService.update(code, request);
	}

	@DeleteMapping("/{code}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable String code) {
		linkService.delete(code);
	}
}
