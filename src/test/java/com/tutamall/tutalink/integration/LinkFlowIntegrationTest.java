package com.tutamall.tutalink.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class LinkFlowIntegrationTest {

	@BeforeAll
	static void requireDocker() {
		assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker required");
	}

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
			.withDatabaseName("outdatedDBOct2021")
			.withUsername("tutalink_user")
			.withPassword("test");

	@Container
	static RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:7"));

	@DynamicPropertySource
	static void props(DynamicPropertyRegistry registry) {
		String url = postgres.getJdbcUrl();
		registry.add("spring.datasource.primary.url", () -> url);
		registry.add("spring.datasource.replica.url", () -> url);
		registry.add("spring.datasource.primary.username", postgres::getUsername);
		registry.add("spring.datasource.primary.password", postgres::getPassword);
		registry.add("spring.datasource.replica.username", postgres::getUsername);
		registry.add("spring.datasource.replica.password", postgres::getPassword);
		registry.add("spring.flyway.url", () -> url);
		registry.add("spring.flyway.user", postgres::getUsername);
		registry.add("spring.flyway.password", postgres::getPassword);
		registry.add("spring.data.redis.url", () -> "redis://" + redis.getHost() + ":" + redis.getFirstMappedPort());
		registry.add("tutalink.api-key", () -> "test-api-key");
		registry.add("tutalink.ip-salt", () -> "salt");
		registry.add("tutalink.base-url", () -> "http://localhost:4100/l/");
		registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
	}

	@Autowired MockMvc mockMvc;

	@Test
	void createRedirectStats() throws Exception {
		MvcResult created = mockMvc.perform(post("/tutalink/api/links")
						.header("X-API-Key", "test-api-key")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"url\":\"https://tutamall.com/mall\",\"customCode\":\"intg01\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.code").value("intg01"))
				.andReturn();
		assertThat(created.getResponse().getContentAsString()).contains("intg01");

		mockMvc.perform(get("/l/intg01"))
				.andExpect(status().isFound())
				.andExpect(header().string("Location", "https://tutamall.com/mall"));

		Thread.sleep(500); // allow @Async click

		mockMvc.perform(get("/tutalink/api/links/intg01/stats").header("X-API-Key", "test-api-key"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalClicks").value(1));
	}
}
