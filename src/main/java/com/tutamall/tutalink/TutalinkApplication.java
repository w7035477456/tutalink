package com.tutamall.tutalink;

import com.tutamall.tutalink.config.TutalinkProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Entry point. {@code @SpringBootApplication} is a composite of
 * {@code @Configuration}, {@code @EnableAutoConfiguration}, and {@code @ComponentScan}.
 */
@SpringBootApplication
@EnableCaching // turns on Spring's cache abstraction (@Cacheable / @CacheEvict)
@EnableAsync   // turns on @Async; methods run on a ThreadPoolTaskExecutor (see AsyncConfig)
@EnableConfigurationProperties(TutalinkProperties.class)
public class TutalinkApplication {

	public static void main(String[] args) {
		SpringApplication.run(TutalinkApplication.class, args);
	}
}
