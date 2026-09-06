package com.harborvoice.platform.ops;

import org.junit.jupiter.api.Test;
import java.time.Instant;
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
}
