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

    @Test void sandboxPromptMakesTheCompleteUnpublishedMenuAvailableForGroundedConversation() {
        String prompt = new TwilioMediaStreamConfiguration().sandboxInstructions(
                new com.fasterxml.jackson.databind.ObjectMapper());

        assertThat(prompt)
                .contains("You HAVE the complete 256-entry sandbox menu")
                .contains("Never say that you lack menu access")
                .contains("Chicken Supreme | $14.99")
                .contains("Hyderabad Chicken Dum Biriyani | $14.99")
                .contains("supplied description")
                .contains("UNPUBLISHED TEST DATA")
                .contains("one or two short sentences")
                .contains("If directly asked whether you are human")
                .contains("nothing will be submitted")
                .doesNotContain("function_call", "tools");
        assertThat(prompt.length()).isBetween(25_000, 40_000);
        assertThat(prompt.lines().filter(line -> line.matches("\\d+ \\|.*")).count()).isEqualTo(256);
    }
}
