package com.harborvoice.platform.ops;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OperationsConfiguration {
    @Bean ProviderHealthRegistry providerHealthRegistry() { return new ProviderHealthRegistry(); }

    @Bean UsageBudget usageBudget(
            @Value("${VOICE_OPS_MAX_AUDIO_MILLIS:300000}") long maxAudioMillis,
            @Value("${VOICE_OPS_MAX_INPUT_CHARACTERS:100000}") long maxInputCharacters) {
        return new UsageBudget(maxAudioMillis, maxInputCharacters);
    }
}
