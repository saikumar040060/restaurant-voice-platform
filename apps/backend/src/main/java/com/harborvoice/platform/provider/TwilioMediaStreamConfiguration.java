package com.harborvoice.platform.provider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.beans.factory.annotation.Value;
import java.util.UUID;

@Configuration @EnableWebSocket
public class TwilioMediaStreamConfiguration implements WebSocketConfigurer {
    private final TwilioMediaStreamHandler handler;
    public TwilioMediaStreamConfiguration(TwilioMediaStreamHandler handler) { this.handler = handler; }
    @Bean TwilioMediaStreamAdmission twilioMediaStreamAdmission(SandboxSpendGate gate) { return new TwilioMediaStreamAdmission(gate); }
    @Bean TwilioCallAdmissionService twilioCallAdmissionService(TwilioMediaStreamAdmission admission,
            @Value("${VOICE_TWILIO_SANDBOX_BUSINESS_ID:00000000-0000-0000-0000-000000000000}") String business,
            @Value("${VOICE_TWILIO_MEDIA_WSS_URL:wss://restaurant-voice-platform-production.up.railway.app/webhooks/twilio/media}") String url) {
        return new TwilioCallAdmissionService(admission, UUID.fromString(business), url);
    }
    @Bean TwilioMediaStreamHandler twilioMediaStreamHandler(TwilioWebhookConfig config, TwilioMediaStreamAdmission admission) { return new TwilioMediaStreamHandler(config, admission); }
    @Override public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) { registry.addHandler(handler, "/webhooks/twilio/media").setAllowedOrigins(); }
}
