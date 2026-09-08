package com.harborvoice.platform.provider;

import static org.assertj.core.api.Assertions.assertThatCode;
import java.net.URI;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

class TwilioMediaStreamConfigurationTest {
    @Test void registrationDoesNotRequireTheConfigurationBeanToConstructItsOwnHandler() {
        var config = new TwilioMediaStreamConfiguration();
        var gate = new SandboxSpendGate(new CallAdmissionController(new ProviderSafetyConfig(false, Set.of(), 1, 60, 100)));
        var handler = config.twilioMediaStreamHandler(TwilioWebhookConfig.disabled(), config.twilioMediaStreamAdmission(gate));
        assertThatCode(() -> config.twilioMediaStreamWebSocketConfigurer(handler)).doesNotThrowAnyException();
    }
}
