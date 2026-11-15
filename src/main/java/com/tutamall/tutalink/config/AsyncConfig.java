package com.tutamall.tutalink.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/** Named executor for {@code @Async("clickExecutor")} click recording. */
@Configuration
public class AsyncConfig {

	@Bean(name = "clickExecutor")
	public Executor clickExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(8);
		executor.setQueueCapacity(500);
		executor.setThreadNamePrefix("tutalink-click-");
		executor.initialize();
		return executor;
	}
}
