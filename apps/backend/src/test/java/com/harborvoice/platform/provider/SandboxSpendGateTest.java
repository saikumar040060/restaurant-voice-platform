package com.harborvoice.platform.provider;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SandboxSpendGateTest {
    @Test
    void releasesTheCallSlotWhenTheSpendReservationIsRejected() {
        var controller = new CallAdmissionController(
                new ProviderSafetyConfig(true, Set.of("+15550000001"), 1, 60, 10));
        var gate = new SandboxSpendGate(controller);

        assertThat(gate.reserve("+15550000001", 11)).isFalse();
        assertThat(controller.activeCalls()).isZero();
        assertThat(controller.remainingSpendMinor()).isEqualTo(10);
        assertThat(gate.reserve("+15550000001", 10)).isTrue();
        assertThat(controller.activeCalls()).isOne();
    }

    @Test
    void expiresAndReleasesALeaseAtTheConfiguredDuration() {
        var controller = new CallAdmissionController(
                new ProviderSafetyConfig(true, Set.of("+15550000001"), 1, 60, 100));
        var gate = new SandboxSpendGate(controller);
        Instant started = Instant.parse("2026-09-08T12:00:00Z");
        var lease = gate.reserveLease("+15550000001", 1, started).orElseThrow();

        assertThat(gate.active(lease, started.plusSeconds(59))).isTrue();
        assertThat(gate.active(lease, started.plusSeconds(60))).isFalse();
        assertThat(controller.activeCalls()).isZero();
        gate.release(lease);
        assertThat(controller.activeCalls()).isZero();
    }

    @Test
    void rejectsUnknownCallersAndNegativeReservationsWithoutCharging() {
        var controller = new CallAdmissionController(
                new ProviderSafetyConfig(true, Set.of("+15550000001"), 1, 60, 10));
        var gate = new SandboxSpendGate(controller);

        assertThat(gate.reserve("+15550000002", 1)).isFalse();
        assertThat(gate.reserve("+15550000001", -1)).isFalse();
        assertThat(controller.activeCalls()).isZero();
        assertThat(controller.remainingSpendMinor()).isEqualTo(10);
    }
}
