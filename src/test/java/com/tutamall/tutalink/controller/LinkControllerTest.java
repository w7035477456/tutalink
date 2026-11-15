package com.tutamall.tutalink.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tutamall.tutalink.config.TutalinkProperties;
import com.tutamall.tutalink.dto.LinkResponse;
import com.tutamall.tutalink.exception.GlobalExceptionHandler;
import com.tutamall.tutalink.exception.LinkNotFoundException;
import com.tutamall.tutalink.security.ApiKeyFilter;
import com.tutamall.tutalink.service.LinkService;
import com.tutamall.tutalink.service.StatsService;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith(MockitoExtension.class)
class LinkControllerTest {

	@Mock LinkService linkService;
	@Mock StatsService statsService;

	MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		TutalinkProperties props = new TutalinkProperties(
				"http://localhost:4100/l/", 7, Duration.ofHours(1), "test-api-key", "salt");
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();
		mockMvc = MockMvcBuilders.standaloneSetup(new LinkController(linkService, statsService))
				.setControllerAdvice(new GlobalExceptionHandler())
				.setValidator(validator)
				.addFilters(new ApiKeyFilter(props))
				.build();
	}

	@Test
	void missingApiKeyReturns401() throws Exception {
		mockMvc.perform(get("/tutalink/api/links/abc"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void validationErrorReturns400() throws Exception {
		mockMvc.perform(post("/tutalink/api/links")
						.header("X-API-Key", "test-api-key")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"url\":\"not-a-url\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void unknownLinkReturns404() throws Exception {
		when(linkService.getByCode("missing")).thenThrow(new LinkNotFoundException("missing"));
		mockMvc.perform(get("/tutalink/api/links/missing").header("X-API-Key", "test-api-key"))
				.andExpect(status().isNotFound());
	}

	@Test
	void createReturns201() throws Exception {
		when(linkService.create(any())).thenReturn(
				new LinkResponse(1L, "abc", "https://tutamall.com", "http://localhost:4100/l/abc",
						null, null, true));

		mockMvc.perform(post("/tutalink/api/links")
						.header("X-API-Key", "test-api-key")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"url\":\"https://tutamall.com\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.code").value("abc"));
	}
}
