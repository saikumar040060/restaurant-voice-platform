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

    @Test
    void exposesOnlyStableHealthAndBudgetDataForOperations() {
        var registry = new ProviderHealthRegistry();
        registry.record(new ProviderHealth("openai", ProviderHealth.State.HEALTHY, Instant.EPOCH, "ok"));
        var budget = new UsageBudget(100, 200);
        assertTrue(budget.consume(25, 40));

        var snapshot = OperationsSnapshot.from(registry, budget);
        assertTrue(snapshot.providers().containsKey("openai"));
        assertEquals(75, snapshot.audioMillisRemaining());
        assertEquals(160, snapshot.inputCharactersRemaining());
    }
}
