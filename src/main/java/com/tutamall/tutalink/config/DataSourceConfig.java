package com.tutamall.tutalink.config;

import com.zaxxer.hikari.HikariDataSource;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Dual-datasource routing for Primary (writes) / Replica (reads).
 *
 * How it works (interview topic):
 * 1. {@link AbstractRoutingDataSource} picks a target DataSource per lookup via
 *    {@code determineCurrentLookupKey()}.
 * 2. We key off {@link TransactionSynchronizationManager#isCurrentTransactionReadOnly()}.
 *    {@code @Transactional(readOnly = true)} → replica; everything else → primary.
 * 3. Wrapped in {@link LazyConnectionDataSourceProxy} so the physical connection is
 *    acquired *after* the transaction starts (when read-only status is known). Without
 *    the lazy proxy, the connection would be grabbed too early and always hit Primary.
 *
 * Flyway uses a separate Primary-only DataSource (see application.yml spring.flyway.url).
 */
@Configuration
public class DataSourceConfig {

	public enum DataSourceType {
		PRIMARY, REPLICA
	}

	@Bean
	@ConfigurationProperties("spring.datasource.primary")
	public DataSourceProperties primaryDataSourceProperties() {
		return new DataSourceProperties();
	}

	@Bean
	@ConfigurationProperties("spring.datasource.replica")
	public DataSourceProperties replicaDataSourceProperties() {
		return new DataSourceProperties();
	}

	@Bean
	public DataSource primaryDataSource(
			@Qualifier("primaryDataSourceProperties") DataSourceProperties properties) {
		return properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
	}

	@Bean
	public DataSource replicaDataSource(
			@Qualifier("replicaDataSourceProperties") DataSourceProperties properties) {
		return properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
	}

	@Bean
	public DataSource routingDataSource(
			@Qualifier("primaryDataSource") DataSource primary,
			@Qualifier("replicaDataSource") DataSource replica) {
		AbstractRoutingDataSource routing = new AbstractRoutingDataSource() {
			@Override
			protected Object determineCurrentLookupKey() {
				boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
				return readOnly ? DataSourceType.REPLICA : DataSourceType.PRIMARY;
			}
		};
		Map<Object, Object> targets = new HashMap<>();
		targets.put(DataSourceType.PRIMARY, primary);
		targets.put(DataSourceType.REPLICA, replica);
		routing.setTargetDataSources(targets);
		routing.setDefaultTargetDataSource(primary);
		routing.afterPropertiesSet();
		return routing;
	}

	/**
	 * {@code @Primary} DataSource used by JPA. Lazy proxy defers connection until
	 * first Statement, after @Transactional has set the read-only flag.
	 */
	@Bean
	@Primary
	public DataSource dataSource(@Qualifier("routingDataSource") DataSource routing) {
		return new LazyConnectionDataSourceProxy(routing);
	}
}
