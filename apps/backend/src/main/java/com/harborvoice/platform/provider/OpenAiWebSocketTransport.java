package com.harborvoice.platform.provider;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

/**
 * Explicit OpenAI Realtime WebSocket transport. It never connects during
 * construction; callers must invoke {@link #connect(OpenAiRealtimeConfig)}.
 */
public final class OpenAiWebSocketTransport implements RealtimeTransport, WebSocket.Listener {
    private volatile WebSocket socket;
    private volatile Consumer<String> events = ignored -> { };
    private final StringBuilder partialEvent = new StringBuilder();

    OpenAiWebSocketTransport() { }

    public static URI endpoint(String model) {
        if (model == null || model.isBlank()) throw new IllegalArgumentException("realtime model required");
        return URI.create("wss://api.openai.com/v1/realtime?model="
                + URLEncoder.encode(model, StandardCharsets.UTF_8));
    }

    /** Opens a sandbox connection only when explicitly called by an authorized runtime path. */
    public static OpenAiWebSocketTransport connect(OpenAiRealtimeConfig config) {
        if (config == null || !config.enabled()) throw new IllegalArgumentException("enabled realtime configuration required");
        var transport = new OpenAiWebSocketTransport();
        try {
            transport.socket = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build()
                    .newWebSocketBuilder()
                    .header("Authorization", "Bearer " + config.apiKey())
                    .connectTimeout(Duration.ofSeconds(10))
                    .buildAsync(endpoint(config.model()), transport).join();
            return transport;
        } catch (RuntimeException ex) {
            transport.close();
            throw new IllegalStateException("OpenAI realtime connection failed", ex);
        }
    }

    @Override public void send(String eventJson) {
        if (eventJson == null || eventJson.isBlank()) throw new IllegalArgumentException("realtime event required");
        WebSocket active = socket;
        if (active == null) throw new IllegalStateException("realtime transport is not connected");
        active.sendText(eventJson, true).join();
    }

    @Override public void onEvent(Consumer<String> eventHandler) {
        events = Objects.requireNonNull(eventHandler, "event handler required");
    }

    @Override public synchronized CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        partialEvent.append(data);
        if (last) {
            String event = partialEvent.toString();
            partialEvent.setLength(0);
            events.accept(event);
        }
        webSocket.request(1);
        return WebSocket.Listener.super.onText(webSocket, data, last);
    }

    @Override public void onOpen(WebSocket webSocket) {
        webSocket.request(1);
        WebSocket.Listener.super.onOpen(webSocket);
    }

    @Override public void close() {
        WebSocket active = socket;
        socket = null;
        synchronized (this) { partialEvent.setLength(0); }
        if (active != null) active.sendClose(WebSocket.NORMAL_CLOSURE, "closed");
    }
}
