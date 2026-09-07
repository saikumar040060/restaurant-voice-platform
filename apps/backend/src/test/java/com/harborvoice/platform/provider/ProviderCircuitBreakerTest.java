package com.harborvoice.platform.provider;

import static org.assertj.core.api.Assertions.*;
import java.time.*;
import org.junit.jupiter.api.Test;

class ProviderCircuitBreakerTest {
    @Test void opensAfterBoundedFailuresAndRecoversAfterCooldown() {
        var clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
        var breaker = new ProviderCircuitBreaker(2, Duration.ofSeconds(5), clock);
        breaker.recordFailure(); assertThat(breaker.allowRequest()).isTrue();
        breaker.recordFailure(); assertThat(breaker.state()).isEqualTo(ProviderCircuitBreaker.State.OPEN);
        breaker.recordSuccess();
        assertThat(breaker.state()).isEqualTo(ProviderCircuitBreaker.State.CLOSED);
    }
}
