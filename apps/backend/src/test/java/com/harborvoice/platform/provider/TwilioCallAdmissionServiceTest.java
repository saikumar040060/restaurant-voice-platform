package com.harborvoice.platform.provider;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class TwilioCallAdmissionServiceTest {
    @Test void placesOneUseAdmissionInAStreamCustomParameter() {
        var gate = new SandboxSpendGate(new CallAdmissionController(
                new ProviderSafetyConfig(true, Set.of("+15550000001"), 1, 60, 100)));
        var streams = new TwilioMediaStreamAdmission(gate);
        var service = new TwilioCallAdmissionService(streams, UUID.randomUUID(),
                "wss://example.test/webhooks/twilio/media");

        String twiml = service.admit("+15550000001", "CA123456", 1, Instant.now());

        assertThat(twiml).contains("<Stream url=\"wss://example.test/webhooks/twilio/media\">",
                "<Parameter name=\"admission\" value=\"");
        assertThat(twiml).doesNotContain("?token=", ">runtime-secret<");
        var matcher = Pattern.compile("name=\"admission\" value=\"([0-9a-f-]{36})\"").matcher(twiml);
        assertThat(matcher.find()).isTrue();
        assertThat(streams.consume(UUID.fromString(matcher.group(1)), Instant.now()).businessId()).isNotNull();
    }
}
