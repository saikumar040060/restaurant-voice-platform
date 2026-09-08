package com.harborvoice.platform.ops;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.harborvoice.identity.Actor;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class OperationsControllerTest {
    @Test void exposesOnlyRedactionSafeSnapshotToOwnerAndManager() {
        var registry = new ProviderHealthRegistry();
        registry.record(new ProviderHealth("fixture", ProviderHealth.State.HEALTHY, Instant.parse("2026-09-08T12:00:00Z"), "fixture"));
        var controller = new OperationsController(registry, new UsageBudget(100, 50));

        var snapshot = controller.snapshot(actor(Actor.Role.MANAGER));
        assertThat(snapshot.providers()).containsOnlyKeys("fixture");
        assertThat(snapshot.providers().get("fixture").reason()).isEmpty();
        assertThat(snapshot.audioMillisRemaining()).isEqualTo(100);
    }

    @Test void deniesEmployeeAndUnauthenticatedOperationsViews() {
        var controller = new OperationsController(new ProviderHealthRegistry(), new UsageBudget(100, 50));
        assertThatThrownBy(() -> controller.snapshot(actor(Actor.Role.EMPLOYEE)))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", org.springframework.http.HttpStatus.FORBIDDEN);
        assertThatThrownBy(() -> controller.snapshot(null)).isInstanceOf(ResponseStatusException.class);
    }

    private static Actor actor(Actor.Role role) { return new Actor(UUID.randomUUID(), UUID.randomUUID(), role); }
}
