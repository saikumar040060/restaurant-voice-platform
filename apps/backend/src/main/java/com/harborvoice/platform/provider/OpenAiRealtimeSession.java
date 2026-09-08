package com.harborvoice.platform.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harborvoice.platform.media.MediaEnvelope;
import com.harborvoice.platform.speech.TextToSpeechPort;
import java.util.ArrayDeque;
import java.util.Base64;
import java.util.Deque;
import java.util.Objects;

/**
 * Server-side OpenAI Realtime event adapter. Networking is supplied separately
 * through {@link RealtimeTransport}; construction does not make a network call.
 */
public final class OpenAiRealtimeSession implements RealtimeSessionPort {
    private static final int MAX_OUTPUTS = 32;
    private final RealtimeTransport transport;
    private final ObjectMapper json;
    private final Deque<TextToSpeechPort.AudioSynthesis> outputs = new ArrayDeque<>();
    private long epoch;
    private boolean closed;

    public OpenAiRealtimeSession(OpenAiRealtimeConfig config, RealtimeTransport transport, ObjectMapper json) {
        if (config == null || !config.enabled()) throw new IllegalArgumentException("enabled realtime configuration required");
        this.transport = Objects.requireNonNull(transport, "transport required");
        this.json = Objects.requireNonNull(json, "json required");
        transport.onEvent(this::acceptEvent);
    }

    @Override public synchronized void accept(MediaEnvelope input) {
        if (closed) throw new IllegalStateException("realtime session closed");
        Objects.requireNonNull(input, "input required");
        if (input.epoch() < epoch) return;
        if (input.epoch() > epoch) {
            epoch = input.epoch();
            outputs.clear();
        }
        try {
            transport.send(json.writeValueAsString(java.util.Map.of(
                    "type", "input_audio_buffer.append",
                    "audio", Base64.getEncoder().encodeToString(input.payload()))));
            if (input.finalFrame()) {
                transport.send("{\"type\":\"input_audio_buffer.commit\"}");
                transport.send("{\"type\":\"response.create\"}");
            }
        } catch (Exception ex) {
            throw new IllegalStateException("realtime event serialization failed", ex);
        }
    }

    @Override public synchronized TextToSpeechPort.AudioSynthesis nextOutput(long requestedEpoch) {
        if (closed || requestedEpoch != epoch) return null;
        return outputs.pollFirst();
    }

    @Override public synchronized void cancel(long nextEpoch) {
        if (nextEpoch < epoch || closed) return;
        epoch = nextEpoch;
        outputs.clear();
        transport.send("{\"type\":\"response.cancel\"}");
        transport.send("{\"type\":\"input_audio_buffer.clear\"}");
    }

    @Override public synchronized void close() {
        if (!closed) transport.close();
        closed = true;
        outputs.clear();
    }

    private synchronized void acceptEvent(String eventJson) {
        if (closed || eventJson == null || eventJson.isBlank()) return;
        try {
            JsonNode event = json.readTree(eventJson);
            String type = event.path("type").asText();
            if (!"response.output_audio.delta".equals(type) && !"response.audio.delta".equals(type)) return;
            String delta = event.path("delta").asText();
            if (delta.isBlank() || outputs.size() >= MAX_OUTPUTS) return;
            outputs.addLast(new TextToSpeechPort.AudioSynthesis("audio/pcm", Base64.getDecoder().decode(delta), epoch));
        } catch (Exception ignored) {
            // Malformed provider frames must not reach customer playback or alter workflow state.
        }
    }
}
