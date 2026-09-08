package com.harborvoice.platform.provider;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/** Disabled-by-default Media Streams ingress. It discards frames and never records or orders. */
public final class TwilioMediaStreamHandler extends TextWebSocketHandler {
    private final TwilioWebhookConfig config; private final TwilioMediaStreamAdmission admissions;
    public TwilioMediaStreamHandler(TwilioWebhookConfig config, TwilioMediaStreamAdmission admissions) {
        this.config = config; this.admissions = admissions;
    }
    @Override public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        if (!config.enabled()) { session.close(CloseStatus.POLICY_VIOLATION); return; }
        try { session.getAttributes().put("grant", admissions.consume(token(session.getUri()), Instant.now())); }
        catch (RuntimeException denied) { session.close(CloseStatus.POLICY_VIOLATION); }
    }
    @Override protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        if (!session.getAttributes().containsKey("grant") || message.getPayload().length() > 64 * 1024) {
            session.close(CloseStatus.POLICY_VIOLATION);
        }
        // Intentionally discard media events until a separately approved, tested media pipeline is enabled.
    }
    @Override public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Object grant = session.getAttributes().get("grant");
        if (grant instanceof TwilioMediaStreamAdmission.Grant item) admissions.close(item);
    }
    private static UUID token(URI uri) {
        if (uri == null || uri.getQuery() == null) throw new IllegalArgumentException("token required");
        for (String value : uri.getQuery().split("&")) if (value.startsWith("token=")) return UUID.fromString(value.substring(6));
        throw new IllegalArgumentException("token required");
    }
}
