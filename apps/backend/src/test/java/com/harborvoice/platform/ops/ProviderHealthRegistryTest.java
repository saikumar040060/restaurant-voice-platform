package com.harborvoice.platform.ops;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class ProviderHealthRegistryTest {
    @Test
    void recordsAndReplacesHealthByProvider() {
        var registry = new ProviderHealthRegistry();
        registry.record(new ProviderHealth("fixture", ProviderHealth.State.DEGRADED, Instant.now(), "slow"));
        registry.record(new ProviderHealth("fixture", ProviderHealth.State.HEALTHY, Instant.now(), "ok"));
        assertEquals(ProviderHealth.State.HEALTHY, registry.find("fixture").orElseThrow().state());
        assertTrue(registry.find("missing").isEmpty());
    }
}
