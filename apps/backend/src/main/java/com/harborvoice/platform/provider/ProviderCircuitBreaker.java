package com.harborvoice.platform.provider;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/** Small provider-neutral circuit breaker for sandbox adapters. */
public final class ProviderCircuitBreaker {
    public enum State { CLOSED, OPEN }
    private final int threshold;
    private final Duration coolDown;
    private final Clock clock;
    private int failures;
    private Instant openedAt;

    public ProviderCircuitBreaker(int threshold, Duration coolDown) { this(threshold, coolDown, Clock.systemUTC()); }
    ProviderCircuitBreaker(int threshold, Duration coolDown, Clock clock) {
        if (threshold < 1 || coolDown == null || coolDown.isNegative() || coolDown.isZero()) throw new IllegalArgumentException("invalid breaker");
        this.threshold = threshold; this.coolDown = coolDown; this.clock = clock;
    }
    public synchronized boolean allowRequest() {
        return openedAt == null || Duration.between(openedAt, Instant.now(clock)).compareTo(coolDown) >= 0;
    }
    public synchronized void recordSuccess() { failures = 0; openedAt = null; }
    public synchronized void recordFailure() { if (++failures >= threshold) openedAt = Instant.now(clock); }
    public synchronized State state() { return allowRequest() ? State.CLOSED : State.OPEN; }
}
