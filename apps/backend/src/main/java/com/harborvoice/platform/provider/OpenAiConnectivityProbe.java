package com.harborvoice.platform.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** One-shot sandbox socket check, disabled unless explicitly enabled in the deployment environment. */
@Component
public final class OpenAiConnectivityProbe implements CommandLineRunner {
    private final boolean enabled;
    private final String caller;
    private final AdmittedRealtimeSessionFactory sessions;

    public OpenAiConnectivityProbe(@Value("${VOICE_OPENAI_CONNECTIVITY_PROBE_ENABLED:false}") boolean enabled,
            @Value("${VOICE_TWILIO_ALLOWED_FROM:}") String caller, AdmittedRealtimeSessionFactory sessions) {
        this.enabled = enabled;
        this.caller = caller == null ? "" : caller.split(",", 2)[0].trim();
        this.sessions = sessions;
    }

    @Override public void run(String... args) {
        if (!enabled) return;
        try (var ignored = sessions.open(caller, 1)) {
            // Open/close validates authenticated connectivity. No audio, prompt, tool, or customer data is sent.
        }
    }
}
