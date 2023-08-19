package com.phorest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Configures JPA Auditing. */
@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "timeProvider")
public class AuditConfiguration {}
