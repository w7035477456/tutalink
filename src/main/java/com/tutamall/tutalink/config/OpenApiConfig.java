package com.tutamall.tutalink.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI tutalinkOpenApi() {
		final String scheme = "ApiKey";
		return new OpenAPI()
				.info(new Info().title("tutalink API").version("1.0")
						.description("Link shortener for TutaMall marketing"))
				.addSecurityItem(new SecurityRequirement().addList(scheme))
				.components(new Components().addSecuritySchemes(scheme,
						new SecurityScheme()
								.name("X-API-Key")
								.type(SecurityScheme.Type.APIKEY)
								.in(SecurityScheme.In.HEADER)));
	}
}
