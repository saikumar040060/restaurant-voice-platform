package com.harborvoice.platform.provider;

import static org.assertj.core.api.Assertions.assertThat;

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
