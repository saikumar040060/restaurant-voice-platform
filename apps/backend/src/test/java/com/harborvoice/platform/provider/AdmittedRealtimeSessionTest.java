package com.harborvoice.platform.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.harborvoice.platform.media.MediaEnvelope;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AdmittedRealtimeSessionTest {
    @Test void allowsActiveLeaseAndClosesOnceWhenItExpires() {
        Instant start = Instant.parse("2026-09-08T12:00:00Z");
        var controller = new CallAdmissionController(new ProviderSafetyConfig(true, Set.of("+1"), 1, 60, 100));
        var gate = new SandboxSpendGate(controller);
        var lease = gate.reserveLease("+1", 1, start).orElseThrow();
        var session = new AdmittedRealtimeSession(new FixtureRealtimeSession(), gate, lease,
                Clock.fixed(start.plusSeconds(59), ZoneOffset.UTC));
        session.accept(envelope());
        assertThat(session.nextOutput(0)).isNotNull();
        assertThat(controller.activeCalls()).isOne();

        var expired = new AdmittedRealtimeSession(new FixtureRealtimeSession(), gate, lease,
                Clock.fixed(start.plusSeconds(60), ZoneOffset.UTC));
        assertThatThrownBy(() -> expired.accept(envelope()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("sandbox realtime lease expired or unavailable");
        assertThat(controller.activeCalls()).isZero();
        expired.close();
        assertThat(controller.activeCalls()).isZero();
    }

    private static MediaEnvelope envelope() {
        return new MediaEnvelope(UUID.randomUUID(), "fixture", 0, 0, "pcm", "audio".getBytes(StandardCharsets.UTF_8), true);
    }
}
