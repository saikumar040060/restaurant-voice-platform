package com.harborvoice.platform.ops;

import java.time.Instant;

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
}
