package com.harborvoice.platform.provider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration @EnableWebSocket
public class TwilioMediaStreamConfiguration implements WebSocketConfigurer {
    private final TwilioMediaStreamHandler handler;
    public TwilioMediaStreamConfiguration(TwilioMediaStreamHandler handler) { this.handler = handler; }
    @Bean TwilioMediaStreamAdmission twilioMediaStreamAdmission(SandboxSpendGate gate) { return new TwilioMediaStreamAdmission(gate); }
    @Bean TwilioMediaStreamHandler twilioMediaStreamHandler(TwilioWebhookConfig config, TwilioMediaStreamAdmission admission) { return new TwilioMediaStreamHandler(config, admission); }
    @Override public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) { registry.addHandler(handler, "/webhooks/twilio/media").setAllowedOrigins(); }
}
