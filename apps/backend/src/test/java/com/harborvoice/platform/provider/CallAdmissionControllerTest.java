package com.harborvoice.platform.provider;

import static org.assertj.core.api.Assertions.*;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CallAdmissionControllerTest {
    @Test void enforcesAllowlistConcurrencyAndSpend() {
        var controller = new CallAdmissionController(new ProviderSafetyConfig(true, Set.of("+1"), 1, 60, 10));
        assertThat(controller.admit("+2")).isFalse();
        assertThat(controller.admit("+1")).isTrue();
        assertThat(controller.admit("+1")).isFalse();
        assertThat(controller.charge(6)).isTrue();
        assertThat(controller.charge(5)).isFalse();
        controller.release();
        assertThat(controller.admit("+1")).isTrue();
    }
}
