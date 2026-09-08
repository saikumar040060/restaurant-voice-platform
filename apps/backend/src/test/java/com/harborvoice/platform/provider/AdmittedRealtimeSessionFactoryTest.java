package com.harborvoice.platform.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class AdmittedRealtimeSessionFactoryTest {
    @Test void reservesSafetyLimitsBeforeCreatingTheTransport() {
        var controller = new CallAdmissionController(new ProviderSafetyConfig(true, Set.of("+1"), 1, 60, 10));
        var attempts = new AtomicInteger();
        var factory = factory(true, controller, config -> {
            attempts.incrementAndGet();
            return new FakeTransport();
        });

        try (var session = factory.open("+1", 10)) {
            assertThat(attempts).hasValue(1);
            assertThat(controller.activeCalls()).isOne();
        }
        assertThat(controller.activeCalls()).isZero();
    }

    @Test void rejectsBeforeConnectionAndReleasesOnConnectionFailure() {
        var controller = new CallAdmissionController(new ProviderSafetyConfig(true, Set.of("+1"), 1, 60, 10));
        var denied = factory(true, controller, config -> { throw new AssertionError("transport must not run"); });
        assertThatThrownBy(() -> denied.open("+2", 1)).isInstanceOf(IllegalStateException.class)
                .hasMessage("sandbox realtime admission denied");

        var failing = factory(true, controller, config -> { throw new IllegalStateException("connect failed"); });
        assertThatThrownBy(() -> failing.open("+1", 1)).isInstanceOf(IllegalStateException.class)
                .hasMessage("connect failed");
        assertThat(controller.activeCalls()).isZero();
    }

    private static AdmittedRealtimeSessionFactory factory(boolean enabled, CallAdmissionController controller,
            RealtimeTransportFactory transports) {
        return new AdmittedRealtimeSessionFactory(new OpenAiRealtimeConfig(enabled, "runtime-secret", "candidate", 32),
                new SandboxSpendGate(controller), transports, new ObjectMapper(),
                Clock.fixed(Instant.parse("2026-09-08T12:00:00Z"), ZoneOffset.UTC));
    }

    private static final class FakeTransport implements RealtimeTransport {
        @Override public void send(String eventJson) { }
        @Override public void onEvent(java.util.function.Consumer<String> eventHandler) { }
        @Override public void close() { }
    }
}
