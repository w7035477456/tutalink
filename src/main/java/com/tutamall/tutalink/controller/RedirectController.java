package com.tutamall.tutalink.controller;

import com.tutamall.tutalink.service.ClickService;
import com.tutamall.tutalink.service.LinkService;
import com.tutamall.tutalink.web.ClientInfoExtractor;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** Public 302 redirect — no API key. Click recorded asynchronously. */
@RestController
public class RedirectController {

	private final LinkService linkService;
	private final ClickService clickService;
	private final ClientInfoExtractor clientInfoExtractor;

	public RedirectController(LinkService linkService, ClickService clickService,
			ClientInfoExtractor clientInfoExtractor) {
		this.linkService = linkService;
		this.clickService = clickService;
		this.clientInfoExtractor = clientInfoExtractor;
	}

	@GetMapping("/l/{code}")
	public ResponseEntity<Void> redirect(@PathVariable String code, HttpServletRequest request) {
		String target = linkService.resolveForRedirect(code);
		clickService.recordClickAsync(code, clientInfoExtractor.extract(request));
		return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(target)).build();
	}
}
