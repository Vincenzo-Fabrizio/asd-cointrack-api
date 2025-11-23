package com.asd.cointrack.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * MongoDB auditing configuration that enables automatic population of
 * {@code createdAt}, {@code updatedAt}, {@code createdBy} and {@code updatedBy}
 * fields on audited entities.
 */
@Configuration
@EnableMongoAuditing
public class MongoAuditingConfig {

    /**
     * Simple {@link AuditorAware} implementation that currently returns a static
     * {@code system} user. This can be replaced by an implementation that reads
     * the authenticated user from the security context.
     *
     * @return auditor provider
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of("system");
    }
}
