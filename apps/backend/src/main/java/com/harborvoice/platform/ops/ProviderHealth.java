package com.harborvoice.platform.ops;

import java.time.Instant;
import java.time.Duration;

/** Provider-neutral health signal consumed by routing and escalation policy. */
public record ProviderHealth(String provider, State state, Instant observedAt, String reason) {
    public ProviderHealth {
        if (provider == null || provider.isBlank() || state == null || observedAt == null) {
            throw new IllegalArgumentException("invalid provider health");
        }
        reason = reason == null ? "" : reason.trim();
        if (reason.length() > 500) throw new IllegalArgumentException("provider health reason too long");
    }

    public enum State { HEALTHY, DEGRADED, UNAVAILABLE }

    public boolean canServe() { return state != State.UNAVAILABLE; }

    public boolean staleAt(Instant now, Duration maxAge) {
        if (now == null || maxAge == null || maxAge.isNegative()) throw new IllegalArgumentException("invalid health age");
        return observedAt.plus(maxAge).isBefore(now);
    }
}
