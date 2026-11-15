package com.tutamall.tutalink.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tutamall.tutalink.exception.GlobalExceptionHandler;
import com.tutamall.tutalink.exception.LinkExpiredException;
import com.tutamall.tutalink.exception.LinkNotFoundException;
import com.tutamall.tutalink.service.ClickService;
import com.tutamall.tutalink.service.LinkService;
import com.tutamall.tutalink.web.ClientInfoExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class RedirectControllerTest {

	@Mock LinkService linkService;
	@Mock ClickService clickService;
	@Mock ClientInfoExtractor clientInfoExtractor;

	MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(
						new RedirectController(linkService, clickService, clientInfoExtractor))
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void redirectReturns302() throws Exception {
		when(linkService.resolveForRedirect("fb3")).thenReturn("https://tutamall.com/mall");
		when(clientInfoExtractor.extract(any())).thenReturn(
				new ClientInfoExtractor.ClientInfo("1.2.3.4", "US", null, null, null, null, null));

		mockMvc.perform(get("/l/fb3"))
				.andExpect(status().isFound())
				.andExpect(header().string("Location", "https://tutamall.com/mall"));
		verify(clickService).recordClickAsync(eq("fb3"), any());
	}

	@Test
	void unknownReturns404() throws Exception {
		when(linkService.resolveForRedirect("x")).thenThrow(new LinkNotFoundException("x"));
		mockMvc.perform(get("/l/x")).andExpect(status().isNotFound());
	}

	@Test
	void expiredReturns410() throws Exception {
		when(linkService.resolveForRedirect("old")).thenThrow(new LinkExpiredException("old"));
		mockMvc.perform(get("/l/old")).andExpect(status().isGone());
	}
}
