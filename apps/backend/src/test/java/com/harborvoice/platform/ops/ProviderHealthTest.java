package com.harborvoice.platform.ops;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

class ProviderHealthTest {
    @Test
    void unavailableProviderCannotServe() {
        ProviderHealth health = new ProviderHealth("fixture", ProviderHealth.State.UNAVAILABLE, Instant.now(), "timeout");
        assertFalse(health.canServe());
    }

    @Test
    void boundsAndTrimsReason() {
        assertEquals("timeout", new ProviderHealth("fixture", ProviderHealth.State.DEGRADED,
                Instant.now(), " timeout ").reason());
        assertThrows(IllegalArgumentException.class, () -> new ProviderHealth("fixture", ProviderHealth.State.DEGRADED,
                Instant.now(), "x".repeat(501)));
    }

    @Test
    void detectsStaleHealthSignals() {
        var health = new ProviderHealth("fixture", ProviderHealth.State.HEALTHY,
                Instant.parse("2026-01-01T00:00:00Z"), "ok");
        assertTrue(health.staleAt(Instant.parse("2026-01-01T00:01:01Z"), Duration.ofMinutes(1)));
        assertFalse(health.staleAt(Instant.parse("2026-01-01T00:01:00Z"), Duration.ofMinutes(1)));
    }
}
