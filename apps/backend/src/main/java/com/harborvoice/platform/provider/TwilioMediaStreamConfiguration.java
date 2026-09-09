package com.harborvoice.platform.provider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.beans.factory.annotation.Value;
import java.util.UUID;

@Configuration @EnableWebSocket
public class TwilioMediaStreamConfiguration {
    @Bean TwilioMediaStreamAdmission twilioMediaStreamAdmission(SandboxSpendGate gate) { return new TwilioMediaStreamAdmission(gate); }
    @Bean TwilioCallAdmissionService twilioCallAdmissionService(TwilioMediaStreamAdmission admission,
            @Value("${VOICE_TWILIO_SANDBOX_BUSINESS_ID:00000000-0000-0000-0000-000000000000}") String business,
            @Value("${VOICE_TWILIO_MEDIA_WSS_URL:wss://restaurant-voice-platform-production.up.railway.app/webhooks/twilio/media}") String url) {
        return new TwilioCallAdmissionService(admission, UUID.fromString(business), url);
    }
    @Bean TwilioMediaStreamHandler twilioMediaStreamHandler(TwilioWebhookConfig config,
            TwilioMediaStreamAdmission admission, AdmittedRealtimeSessionFactory sessions,
            com.fasterxml.jackson.databind.ObjectMapper json) {
        return new TwilioMediaStreamHandler(config, admission, sessions, json, sandboxInstructions(json));
    }

    private String sandboxInstructions(com.fasterxml.jackson.databind.ObjectMapper json) {
        try (var source = getClass().getResourceAsStream("/reviews/indian-restaurant-menu.review-draft.json")) {
            if (source == null) throw new IllegalStateException("sandbox menu draft missing");
            var draft = json.readTree(source);
            StringBuilder prompt = new StringBuilder("You are a test-only restaurant voice assistant. The menu is unpublished. Never create orders, take payments, claim allergy safety, or expose other customers. For allergy or uncertain facts, offer employee help. You may build and read back a fake order only. Menu:\n");
            for (var item : draft.path("items")) prompt.append(item.path("name").asText()).append(" | ")
                    .append(item.path("listed_price").asText()).append(" | ").append(item.path("category").asText()).append('\n');
            if (prompt.length() > 60_000) throw new IllegalStateException("sandbox menu prompt too large");
            return prompt.toString();
        } catch (Exception failure) { throw new IllegalStateException("sandbox menu prompt unavailable", failure); }
    }
    @Bean WebSocketConfigurer twilioMediaStreamWebSocketConfigurer(TwilioMediaStreamHandler handler) {
        return registry -> registry.addHandler(handler, "/webhooks/twilio/media").setAllowedOrigins();
    }
}
