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

    @Test void configuresPcmuInBothDirectionsWithServerVadAndBoundedInstructions() throws Exception {
        var transport = new FakeTransport();
        var session = new OpenAiRealtimeSession(config(), transport, new ObjectMapper());
        session.configure("Only discuss the synthetic menu.");

        var update = new ObjectMapper().readTree(transport.sent.getFirst());
        assertThat(update.path("type").asText()).isEqualTo("session.update");
        assertThat(update.at("/session/audio/input/format/type").asText()).isEqualTo("audio/pcmu");
        assertThat(update.at("/session/audio/input/turn_detection/type").asText()).isEqualTo("server_vad");
        assertThat(update.at("/session/audio/input/turn_detection/create_response").asBoolean()).isTrue();
        assertThat(update.at("/session/audio/input/turn_detection/interrupt_response").asBoolean()).isTrue();
        assertThat(update.at("/session/audio/input/turn_detection/silence_duration_ms").asInt()).isEqualTo(350);
        assertThat(update.at("/session/audio/output/format/type").asText()).isEqualTo("audio/pcmu");
        assertThat(update.at("/session/audio/output/voice").asText()).isEqualTo("marin");
        assertThat(update.at("/session/max_output_tokens").asInt()).isEqualTo(256);
        assertThat(transport.sent).anyMatch(value -> value.contains("response.create")
                && value.contains("Thanks for calling") && !value.contains("test restaurant assistant"));
        assertThatThrownBy(() -> session.configure(" ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test void retainsAnOrdinaryProviderAudioBurstUntilThePhoneBridgeDrainsIt() {
        var transport = new FakeTransport();
        var session = new OpenAiRealtimeSession(config(), transport, new ObjectMapper());

        for (int index = 0; index < 128; index++) {
            transport.emit("{\"type\":\"response.output_audio.delta\",\"delta\":\"aGVsbG8=\"}");
        }

        int drained = 0;
        while (session.nextOutput(0) != null) drained++;
        assertThat(drained).isEqualTo(128);
    }

    @Test void signalsCallerSpeechSoThePhoneProviderCanClearBufferedAudio() {
        var transport = new FakeTransport();
        var session = new OpenAiRealtimeSession(config(), transport, new ObjectMapper());
        var interruptions = new java.util.concurrent.atomic.AtomicInteger();
        session.onInterruption(interruptions::incrementAndGet);

        transport.emit("{\"type\":\"input_audio_buffer.speech_started\"}");

        assertThat(interruptions).hasValue(1);
    }

    @Test void exposesOnlyTenantScopedMenuLookupAndReturnsItsResultToTheModel() throws Exception {
        var transport = new FakeTransport();
        UUID businessId = UUID.randomUUID();
        var calls = new ArrayList<String>();
        RealtimeToolGateway tools = new RealtimeToolGateway() {
            @Override public List<java.util.Map<String, Object>> definitions(UUID scopedBusiness) {
                assertThat(scopedBusiness).isEqualTo(businessId);
                return List.of(java.util.Map.of("type", "function", "name", "restaurant_menu_lookup",
                        "parameters", java.util.Map.of("type", "object")));
            }
            @Override public String execute(UUID scopedBusiness, String name, String arguments) {
                assertThat(scopedBusiness).isEqualTo(businessId);
                calls.add(name + ":" + arguments);
                return "{\"status\":\"UNPUBLISHED_TEST_DATA\",\"name\":\"Garlic Naan\",\"price\":\"$3.99\"}";
            }
        };
        var session = new OpenAiRealtimeSession(config(), transport, new ObjectMapper(), businessId, tools);
        session.configure("Use the approved menu lookup.");

        var update = new ObjectMapper().readTree(transport.sent.getFirst());
        assertThat(update.at("/session/tools/0/name").asText()).isEqualTo("restaurant_menu_lookup");
        assertThat(update.at("/session/tool_choice").asText()).isEqualTo("auto");
        assertThat(update.path("session").path("tools").toString()).doesNotContain("order", "payment", "customer");

        transport.emit("{\"type\":\"response.function_call_arguments.done\",\"call_id\":\"call-1\",\"name\":\"restaurant_menu_lookup\",\"arguments\":\"{\\\"query\\\":\\\"garlic naan\\\"}\"}");

        assertThat(calls).containsExactly("restaurant_menu_lookup:{\"query\":\"garlic naan\"}");
        assertThat(transport.sent).anyMatch(value -> value.contains("conversation.item.create")
                && value.contains("function_call_output") && value.contains("Garlic Naan"));
        assertThat(transport.sent.getLast()).isEqualTo("{\"type\":\"response.create\"}");
    }

    @Test void deniedRealtimeToolCanNeverBecomeAnOrderOperation() {
        var transport = new FakeTransport();
        UUID businessId = UUID.randomUUID();
        RealtimeToolGateway tools = new RealtimeToolGateway() {
            @Override public List<java.util.Map<String, Object>> definitions(UUID scopedBusiness) { return List.of(); }
            @Override public String execute(UUID scopedBusiness, String name, String arguments) {
                throw new IllegalArgumentException("denied");
            }
        };
        var session = new OpenAiRealtimeSession(config(), transport, new ObjectMapper(), businessId, tools);

        transport.emit("{\"type\":\"response.function_call_arguments.done\",\"call_id\":\"call-2\",\"name\":\"create_order\",\"arguments\":\"{}\"}");

        assertThat(transport.sent).anyMatch(value -> value.contains("function_call_output") && value.contains("DENIED"));
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
