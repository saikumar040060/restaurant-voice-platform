package com.harborvoice.platform.provider;

import java.time.Instant;
import java.util.Base64;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harborvoice.platform.media.MediaEnvelope;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/** Disabled-by-default, test-only Media Streams bridge. It stores no audio and exposes no order tools. */
public final class TwilioMediaStreamHandler extends TextWebSocketHandler {
    private final TwilioWebhookConfig config; private final TwilioMediaStreamAdmission admissions;
    private final AdmittedRealtimeSessionFactory sessions; private final ObjectMapper json; private final String instructions;
    public TwilioMediaStreamHandler(TwilioWebhookConfig config, TwilioMediaStreamAdmission admissions) {
        this(config, admissions, null, new ObjectMapper(), "test-only unavailable");
    }
    public TwilioMediaStreamHandler(TwilioWebhookConfig config, TwilioMediaStreamAdmission admissions,
                                    AdmittedRealtimeSessionFactory sessions, ObjectMapper json, String instructions) {
        this.config = config; this.admissions = admissions; this.sessions = sessions; this.json = json; this.instructions = instructions;
    }
    @Override public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        if (!config.enabled()) { session.close(CloseStatus.POLICY_VIOLATION); return; }
    }
    @Override protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        if (message.getPayload().length() > 64 * 1024) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        try {
            var event = json.readTree(message.getPayload());
            if ("start".equals(event.path("event").asText())) {
                if (session.getAttributes().containsKey("realtime")) throw new IllegalStateException("duplicate start event");
                String admission = event.path("start").path("customParameters").path("admission").asText();
                var grant = admissions.consume(java.util.UUID.fromString(admission), Instant.now());
                session.getAttributes().put("grant", grant);
                String streamSid = event.path("start").path("streamSid").asText();
                if (streamSid.isBlank() || streamSid.length() > 128) throw new IllegalArgumentException("bounded stream id required");
                session.getAttributes().put("streamSid", streamSid);
                if (sessions == null) throw new IllegalStateException("realtime bridge unavailable");
                RealtimeSessionPort realtime = sessions.open(grant.lease(), grant.businessId());
                realtime.onOutput(output -> sendOutput(session, streamSid, output));
                realtime.onInterruption(() -> clearOutput(session, streamSid));
                realtime.configure(instructions);
                session.getAttributes().put("realtime", realtime);
                session.getAttributes().put("sequence", 0L);
            } else if ("media".equals(event.path("event").asText())) {
                var realtime = (RealtimeSessionPort) session.getAttributes().get("realtime");
                if (realtime == null) throw new IllegalStateException("start event required");
                long sequence = ((Number) session.getAttributes().get("sequence")).longValue();
                var grant = (TwilioMediaStreamAdmission.Grant) session.getAttributes().get("grant");
                byte[] audio = Base64.getDecoder().decode(event.path("media").path("payload").asText());
                if (audio.length == 0 || audio.length > 8 * 1024) throw new IllegalArgumentException("bounded audio frame required");
                realtime.accept(new MediaEnvelope(grant.conversationId(), "twilio", sequence, 0,
                        "audio/pcmu", audio, false));
                session.getAttributes().put("sequence", sequence + 1);
            } else if ("stop".equals(event.path("event").asText())) session.close(CloseStatus.NORMAL);
        } catch (IllegalArgumentException denied) {
            session.close(CloseStatus.POLICY_VIOLATION);
        } catch (RuntimeException failure) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }
    @Override public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Object realtime = session.getAttributes().get("realtime");
        if (realtime instanceof RealtimeSessionPort active) active.close();
        Object grant = session.getAttributes().get("grant");
        if (grant instanceof TwilioMediaStreamAdmission.Grant item) admissions.close(item);
    }

    private void sendOutput(WebSocketSession session, String streamSid,
                            com.harborvoice.platform.speech.TextToSpeechPort.AudioSynthesis output) {
        try {
            synchronized (session) {
                if (!session.isOpen()) return;
                session.sendMessage(new TextMessage(json.writeValueAsString(java.util.Map.of(
                        "event", "media", "streamSid", streamSid,
                        "media", java.util.Map.of("payload", Base64.getEncoder().encodeToString(output.payload()))))));
            }
        } catch (Exception failure) {
            try { session.close(CloseStatus.SERVER_ERROR); }
            catch (Exception ignored) { }
        }
    }

    private void clearOutput(WebSocketSession session, String streamSid) {
        try {
            synchronized (session) {
                if (!session.isOpen()) return;
                session.sendMessage(new TextMessage(json.writeValueAsString(java.util.Map.of(
                        "event", "clear", "streamSid", streamSid))));
            }
        } catch (Exception failure) {
            try { session.close(CloseStatus.SERVER_ERROR); }
            catch (Exception ignored) { }
        }
    }
}
