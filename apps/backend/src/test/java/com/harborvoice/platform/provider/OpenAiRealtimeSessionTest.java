package com.harborvoice.platform.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harborvoice.platform.media.MediaEnvelope;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

class OpenAiRealtimeSessionTest {
    @Test void mapsSyntheticRealtimeFramesAndDropsInterruptedOutput() {
        var transport = new FakeTransport();
        var session = new OpenAiRealtimeSession(config(), transport, new ObjectMapper());
        session.accept(envelope(0, false, "first"));
        assertThat(transport.sent).hasSize(1);
        assertThat(transport.sent.getFirst()).contains("input_audio_buffer.append");
        transport.emit("{\"type\":\"response.output_audio.delta\",\"delta\":\"aGVsbG8=\"}");
        assertThat(new String(session.nextOutput(0).payload(), StandardCharsets.UTF_8)).isEqualTo("hello");

        transport.emit("{\"type\":\"response.audio.delta\",\"delta\":\"b2xk\"}");
        session.cancel(1);
        assertThat(session.nextOutput(0)).isNull();
        assertThat(session.nextOutput(1)).isNull();
        assertThat(transport.sent).anyMatch(value -> value.contains("response.cancel"));
    }

    @Test void rejectsDisabledConfigAndNeverLetsMalformedFramesReachPlayback() {
        var transport = new FakeTransport();
        assertThatThrownBy(() -> new OpenAiRealtimeSession(new OpenAiRealtimeConfig(false, "", "", 16), transport, new ObjectMapper()))
                .isInstanceOf(IllegalArgumentException.class);
        var session = new OpenAiRealtimeSession(config(), transport, new ObjectMapper());
        transport.emit("not json");
        assertThat(session.nextOutput(0)).isNull();
    }

    private static OpenAiRealtimeConfig config() {
        return new OpenAiRealtimeConfig(true, "runtime-secret", "candidate", 256);
    }

    private static MediaEnvelope envelope(long epoch, boolean finalFrame, String payload) {
        return new MediaEnvelope(UUID.randomUUID(), "fixture", 0, epoch, "pcm", payload.getBytes(StandardCharsets.UTF_8), finalFrame);
    }

    private static final class FakeTransport implements RealtimeTransport {
        final List<String> sent = new ArrayList<>();
        Consumer<String> handler;
        @Override public void send(String eventJson) { sent.add(eventJson); }
        @Override public void onEvent(Consumer<String> eventHandler) { handler = eventHandler; }
        @Override public void close() { }
        void emit(String event) { handler.accept(event); }
    }
}
