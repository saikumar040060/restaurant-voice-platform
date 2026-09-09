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

    String sandboxInstructions(com.fasterxml.jackson.databind.ObjectMapper json) {
        try (var source = getClass().getResourceAsStream("/reviews/indian-restaurant-menu.review-draft.json")) {
            if (source == null) throw new IllegalStateException("sandbox menu draft missing");
            var draft = json.readTree(source);
            StringBuilder prompt = new StringBuilder("""
                    You are the voice assistant for a test-only Indian restaurant sandbox.
                    You HAVE the complete 256-entry sandbox menu below. When the caller asks whether you have a menu, say yes. Answer menu, dish, category, recommendation, and listed-price questions only from this menu. Never say that you lack menu access when the requested item is present below. If a name is duplicated or a price is unclear, explain the ambiguity and ask a short clarifying question. Do not invent availability, ingredients, modifiers, sizes, prices, or business facts.

                    The menu remains UNPUBLISHED TEST DATA. You may discuss it and build, revise, quote, and read back a fake pickup order for the caller's test. Never submit or create an order, take payment, record audio, contact a customer or employee, or invoke a provider tool. Never claim that food is safe for an allergy or dietary restriction. For allergy questions or facts not supported by the supplied description, say the information is unverified and offer employee help. Do not volunteer or repeat test, unpublished, automation, safety, or technical language during ordinary menu conversation. State the test limitation once only when reading back a fake order. If directly asked whether you are human, answer honestly that you are the restaurant's automated phone assistant.

                    Speak in a warm, natural conversational style. Use one or two short sentences, then let the caller respond. Do not recite the menu or give long explanations unless asked. Remember the caller's fake cart and corrections during this call. Before treating the fake order as confirmed, read back every item, listed price, and the fact that nothing will be submitted.

                    SANDBOX MENU (source index | category | item | listed price | supplied description):
                    """);
            for (var item : draft.path("items")) {
                prompt.append(item.path("source_index").asText()).append(" | ")
                        .append(item.path("category").asText()).append(" | ")
                        .append(item.path("name").asText()).append(" | ")
                        .append(item.path("listed_price").asText()).append(" | ");
                var sourceBlock = item.path("source_block");
                StringBuilder description = new StringBuilder();
                for (int index = 1; index < sourceBlock.size(); index++) {
                    if (index > 1) description.append(' ');
                    description.append(sourceBlock.get(index).asText());
                }
                String compact = description.toString();
                if (compact.length() > 80) compact = compact.substring(0, 80) + "…";
                prompt.append(compact);
                prompt.append('\n');
            }
            if (prompt.length() > 60_000) throw new IllegalStateException("sandbox menu prompt too large");
            return prompt.toString();
        } catch (Exception failure) { throw new IllegalStateException("sandbox menu prompt unavailable", failure); }
    }
    @Bean WebSocketConfigurer twilioMediaStreamWebSocketConfigurer(TwilioMediaStreamHandler handler) {
        return registry -> registry.addHandler(handler, "/webhooks/twilio/media").setAllowedOrigins();
    }
}
