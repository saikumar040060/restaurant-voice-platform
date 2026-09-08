package com.harborvoice.platform.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Duration;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProviderSafetyConfiguration {
    @Bean ProviderSafetyConfig providerSafetyConfig(
            @Value("${VOICE_OPENAI_REALTIME_ENABLED:false}") boolean enabled,
            @Value("${VOICE_TWILIO_ALLOWED_FROM:}") String allowedFrom,
            @Value("${VOICE_SANDBOX_MAX_CONCURRENT_CALLS:1}") int maxConcurrentCalls,
            @Value("${VOICE_SANDBOX_MAX_CALL_SECONDS:60}") int maxCallSeconds,
            @Value("${VOICE_SANDBOX_MAX_SPEND_MINOR:100}") long maxSpendMinor) {
        Set<String> callers = Arrays.stream(allowedFrom.split(",")).map(String::trim)
                .filter(value -> !value.isEmpty()).collect(Collectors.toUnmodifiableSet());
        return new ProviderSafetyConfig(enabled, callers, maxConcurrentCalls, maxCallSeconds, maxSpendMinor);
    }

    @Bean SandboxSpendGate sandboxSpendGate(ProviderSafetyConfig config) {
        return new SandboxSpendGate(new CallAdmissionController(config));
    }

    @Bean AdmittedRealtimeSessionFactory admittedRealtimeSessionFactory(OpenAiRealtimeConfig config,
            SandboxSpendGate gate, ObjectMapper json) {
        return new AdmittedRealtimeSessionFactory(config, gate, OpenAiWebSocketTransport::connect, json, Clock.systemUTC(),
                new BoundedProviderCall<>(new ProviderCircuitBreaker(2, Duration.ofMinutes(1)),
                        Duration.ofSeconds(10), ForkJoinPool.commonPool()));
    }
}
