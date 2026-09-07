package com.harborvoice.platform.provider;

import java.net.URI;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwilioWebhookConfiguration {
    @Bean
    TwilioWebhookConfig twilioWebhookConfig(
            @Value("${VOICE_TWILIO_ENABLED:false}") boolean enabled,
            @Value("${VOICE_TWILIO_AUTH_TOKEN:}") String authToken,
            @Value("${VOICE_TWILIO_PUBLIC_BASE_URL:}") String publicBaseUrl,
            @Value("${VOICE_TWILIO_ALLOWED_FROM:}") String allowedFrom,
            @Value("${VOICE_TWILIO_REPLAY_WINDOW_SECONDS:300}") int replayWindowSeconds) {
        Set<String> numbers = Arrays.stream(allowedFrom.split(","))
                .map(String::trim).filter(value -> !value.isEmpty()).collect(Collectors.toUnmodifiableSet());
        return new TwilioWebhookConfig(enabled, authToken,
                publicBaseUrl.isBlank() ? null : URI.create(publicBaseUrl), numbers, replayWindowSeconds);
    }

    @Bean
    TwilioRequestVerifier twilioRequestVerifier() {
        return new TwilioRequestVerifier();
    }
}
