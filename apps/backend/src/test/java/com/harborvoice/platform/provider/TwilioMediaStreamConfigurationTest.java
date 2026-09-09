package com.harborvoice.platform.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import java.net.URI;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

class TwilioMediaStreamConfigurationTest {
    @Test void registrationDoesNotRequireTheConfigurationBeanToConstructItsOwnHandler() {
        var config = new TwilioMediaStreamConfiguration();
        var gate = new SandboxSpendGate(new CallAdmissionController(new ProviderSafetyConfig(false, Set.of(), 1, 60, 100)));
        var handler = config.twilioMediaStreamHandler(TwilioWebhookConfig.disabled(),
                config.twilioMediaStreamAdmission(gate), null, new com.fasterxml.jackson.databind.ObjectMapper());
        assertThatCode(() -> config.twilioMediaStreamWebSocketConfigurer(handler)).doesNotThrowAnyException();
    }

    @Test void sandboxPromptRequiresFastGroundedLookupWithoutEmbeddingTheEntireMenu() {
        String prompt = new TwilioMediaStreamConfiguration().sandboxInstructions(
                new com.fasterxml.jackson.databind.ObjectMapper());

        assertThat(prompt)
                .contains("restaurant_menu_lookup")
                .contains("Call that tool before every answer")
                .contains("UNPUBLISHED TEST DATA")
                .contains("one or two short sentences", "respond as soon as")
                .contains("If directly asked whether you are human")
                .contains("nothing will be submitted")
                .doesNotContain("Chicken Supreme", "Hyderabad Chicken Dum Biriyani");
        assertThat(prompt.length()).isLessThan(3_000);
    }
}
