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
}
