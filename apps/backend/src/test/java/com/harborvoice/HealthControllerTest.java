package com.harborvoice;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class HealthControllerTest {
    @Test void livenessProbeContainsNoOperationalDetails() {
        var response = new HealthController().healthz();
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getHeaders().getCacheControl()).contains("no-store");
        assertThat(response.getBody()).hasSize(1).containsEntry("status", "ok");
    }
}
