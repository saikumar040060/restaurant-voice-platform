package com.harborvoice.platform.provider;

import static org.assertj.core.api.Assertions.assertThat;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.InetSocketAddress;
import java.security.Principal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.CloseStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketExtension;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

class TwilioMediaStreamHandlerTest {
    private static final String CALLER = "+15550000001";
    private static final ObjectMapper JSON = new ObjectMapper();

    @Test void bridgesSyntheticPcmuWithoutExposingAnOrderOperation() throws Exception {
        var fixture = fixture();
        var socket = socket(fixture.token());
        fixture.handler().afterConnectionEstablished(socket.session());
        fixture.handler().handleTextMessage(socket.session(), new TextMessage(start("MZ-test-one", fixture.token())));
        fixture.handler().handleTextMessage(socket.session(), new TextMessage(
                "{\"event\":\"media\",\"media\":{\"payload\":\"aGVsbG8=\"}}"));

        assertThat(fixture.transport().sent.getFirst()).contains("session.update", "audio/pcmu", "server_vad");
        assertThat(fixture.transport().sent).anyMatch(value -> value.contains("input_audio_buffer.append"));
        assertThat(socket.outbound()).hasSize(1);
        var response = JSON.readTree(socket.outbound().getFirst().getPayload());
        assertThat(response.path("event").asText()).isEqualTo("media");
        assertThat(response.path("streamSid").asText()).isEqualTo("MZ-test-one");
        assertThat(response.at("/media/payload").asText()).isEqualTo("dGVzdC1hdWRpbw==");
        assertThat(fixture.transport().sent).noneMatch(value -> value.contains("function_call") || value.contains("tools"));
    }

    @Test void forwardsProviderAudioImmediatelyWithoutWaitingForAnotherInboundPhoneFrame() throws Exception {
        var fixture = fixture();
        var socket = socket(fixture.token());
        fixture.handler().afterConnectionEstablished(socket.session());
        fixture.handler().handleTextMessage(socket.session(), new TextMessage(start("MZ-push", fixture.token())));

        fixture.transport().emit("{\"type\":\"response.output_audio.delta\",\"delta\":\"cHVzaGVk\"}");

        assertThat(socket.outbound()).hasSize(1);
        assertThat(JSON.readTree(socket.outbound().getFirst().getPayload()).at("/media/payload").asText())
                .isEqualTo("cHVzaGVk");
    }

    @Test void clearsBufferedSpeechAndDeliversASecondAnswerWhenCallerAsksANewQuestion() throws Exception {
        var fixture = fixture();
        var socket = socket(fixture.token());
        fixture.handler().afterConnectionEstablished(socket.session());
        fixture.handler().handleTextMessage(socket.session(), new TextMessage(start("MZ-multiturn", fixture.token())));

        fixture.transport().emit("{\"type\":\"response.output_audio.delta\",\"delta\":\"Zmlyc3Q=\"}");
        fixture.transport().emit("{\"type\":\"input_audio_buffer.speech_started\"}");
        fixture.transport().emit("{\"type\":\"response.output_audio.delta\",\"delta\":\"c2Vjb25k\"}");

        assertThat(socket.outbound()).hasSize(3);
        assertThat(JSON.readTree(socket.outbound().get(0).getPayload()).at("/media/payload").asText())
                .isEqualTo("Zmlyc3Q=");
        assertThat(JSON.readTree(socket.outbound().get(1).getPayload()).path("event").asText())
                .isEqualTo("clear");
        assertThat(JSON.readTree(socket.outbound().get(2).getPayload()).at("/media/payload").asText())
                .isEqualTo("c2Vjb25k");
    }

    @Test void failsClosedForMediaBeforeStartAndForReusedAdmissionToken() throws Exception {
        var noStart = fixture();
        var first = socket(noStart.token());
        noStart.handler().afterConnectionEstablished(first.session());
        noStart.handler().handleTextMessage(first.session(), new TextMessage(
                "{\"event\":\"media\",\"media\":{\"payload\":\"aGVsbG8=\"}}"));
        assertThat(first.session().closeStatus).isEqualTo(CloseStatus.SERVER_ERROR);

        var fixture = fixture();
        var admitted = socket(fixture.token());
        fixture.handler().afterConnectionEstablished(admitted.session());
        fixture.handler().handleTextMessage(admitted.session(), new TextMessage(start("MZ-first", fixture.token())));
        var replay = socket(fixture.token());
        fixture.handler().afterConnectionEstablished(replay.session());
        fixture.handler().handleTextMessage(replay.session(), new TextMessage(start("MZ-replay", fixture.token())));
        assertThat(replay.session().closeStatus).isEqualTo(CloseStatus.POLICY_VIOLATION);
    }

    @Test void rejectsUnboundedFramesAndClosesProviderOnNormalStop() throws Exception {
        var fixture = fixture();
        var socket = socket(fixture.token());
        fixture.handler().afterConnectionEstablished(socket.session());
        fixture.handler().handleTextMessage(socket.session(), new TextMessage(start("MZ-test-two", fixture.token())));
        String oversized = java.util.Base64.getEncoder().encodeToString(new byte[8 * 1024 + 1]);
        fixture.handler().handleTextMessage(socket.session(), new TextMessage(
                "{\"event\":\"media\",\"media\":{\"payload\":\"" + oversized + "\"}}"));
        assertThat(socket.session().closeStatus).isEqualTo(CloseStatus.POLICY_VIOLATION);

        fixture.handler().afterConnectionClosed(socket.session(), CloseStatus.SERVER_ERROR);
        assertThat(fixture.transport().closed).isTrue();
    }

    private static Fixture fixture() {
        var safety = new ProviderSafetyConfig(true, Set.of(CALLER), 1, 60, 100);
        var gate = new SandboxSpendGate(new CallAdmissionController(safety));
        var admissions = new TwilioMediaStreamAdmission(gate);
        UUID token = admissions.issue(UUID.randomUUID(), UUID.randomUUID(), CALLER, 1, Instant.now());
        var transport = new FakeTransport();
        Executor direct = Runnable::run;
        var factory = new AdmittedRealtimeSessionFactory(
                new OpenAiRealtimeConfig(true, "runtime-secret", "evaluated-test-model", 256), gate,
                ignored -> transport, JSON, Clock.systemUTC(),
                new BoundedProviderCall<>(new ProviderCircuitBreaker(2, Duration.ofSeconds(1)), Duration.ofSeconds(1), direct));
        var twilio = new TwilioWebhookConfig(true, "runtime-secret", URI.create("https://example.test"), Set.of(CALLER), 300);
        return new Fixture(new TwilioMediaStreamHandler(twilio, admissions, factory, JSON,
                "Test only. Build a fake order; never create an order or payment."), transport, token);
    }

    private static Socket socket(UUID token) throws Exception {
        var session = new FakeWebSocketSession(URI.create("wss://example.test/webhooks/twilio/media"));
        return new Socket(session, session.outbound);
    }

    private static String start(String streamSid, UUID token) {
        return "{\"event\":\"start\",\"start\":{\"streamSid\":\"" + streamSid
                + "\",\"customParameters\":{\"admission\":\"" + token + "\"}}}";
    }

    private record Fixture(TwilioMediaStreamHandler handler, FakeTransport transport, UUID token) { }
    private record Socket(FakeWebSocketSession session, List<TextMessage> outbound) { }

    private static final class FakeWebSocketSession implements WebSocketSession {
        final URI uri;
        final HashMap<String, Object> attributes = new HashMap<>();
        final List<TextMessage> outbound = new ArrayList<>();
        boolean open = true;
        CloseStatus closeStatus;
        int textLimit = 64 * 1024;
        int binaryLimit = 64 * 1024;
        FakeWebSocketSession(URI uri) { this.uri = uri; }
        @Override public String getId() { return "fixture"; }
        @Override public URI getUri() { return uri; }
        @Override public HttpHeaders getHandshakeHeaders() { return HttpHeaders.EMPTY; }
        @Override public HashMap<String, Object> getAttributes() { return attributes; }
        @Override public Principal getPrincipal() { return null; }
        @Override public InetSocketAddress getLocalAddress() { return null; }
        @Override public InetSocketAddress getRemoteAddress() { return null; }
        @Override public String getAcceptedProtocol() { return null; }
        @Override public void setTextMessageSizeLimit(int messageSizeLimit) { textLimit = messageSizeLimit; }
        @Override public int getTextMessageSizeLimit() { return textLimit; }
        @Override public void setBinaryMessageSizeLimit(int messageSizeLimit) { binaryLimit = messageSizeLimit; }
        @Override public int getBinaryMessageSizeLimit() { return binaryLimit; }
        @Override public List<WebSocketExtension> getExtensions() { return List.of(); }
        @Override public void sendMessage(WebSocketMessage<?> message) {
            if (message instanceof TextMessage text) outbound.add(text);
            else if (message instanceof BinaryMessage) throw new AssertionError("binary output not expected");
        }
        @Override public boolean isOpen() { return open; }
        @Override public void close() { open = false; closeStatus = CloseStatus.NORMAL; }
        @Override public void close(CloseStatus status) { open = false; closeStatus = status; }
    }

    private static final class FakeTransport implements RealtimeTransport {
        final List<String> sent = new ArrayList<>();
        Consumer<String> receiver;
        boolean closed;
        @Override public void send(String eventJson) {
            sent.add(eventJson);
            if (eventJson.contains("input_audio_buffer.append"))
                receiver.accept("{\"type\":\"response.output_audio.delta\",\"delta\":\"dGVzdC1hdWRpbw==\"}");
        }
        @Override public void onEvent(Consumer<String> eventHandler) { receiver = eventHandler; }
        @Override public void close() { closed = true; }
        void emit(String event) { receiver.accept(event); }
    }
}
