package com.phorest.config;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.auditing.DateTimeProvider;

/**
 * Configures Clock beans for test and non-test profiles, and a DateTimeProvider for JPA Auditing.
 */
@Configuration
public class ClockConfiguration {
  private static final Instant LOCAL_INSTANT = Instant.parse("2023-08-21T00:05:00.00Z");

  @Bean
  @ConditionalOnMissingBean
  public Clock getSystemDefaultZoneClock() {
    return Clock.systemDefaultZone();
  }

  @Profile("test")
  @Bean("fixedClock")
  @Primary
  public Clock fixedClock() {
    return Clock.fixed(LOCAL_INSTANT, ZoneId.systemDefault());
  }

  @Bean
  public DateTimeProvider timeProvider(Clock clock) {
    return () -> Optional.of(LocalDateTime.now(clock));
  }
}
