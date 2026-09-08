package com.harborvoice.platform.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class OpenAiWebSocketTransportTest {
    @Test void constructsTheDocumentedRealtimeEndpointWithoutConnecting() {
        assertThat(OpenAiWebSocketTransport.endpoint("model/with space").toString())
                .isEqualTo("wss://api.openai.com/v1/realtime?model=model%2Fwith+space");
    }

    @Test void reassemblesFragmentedProviderEventsBeforeDelivery() {
        var transport = new OpenAiWebSocketTransport();
        var received = new AtomicReference<String>();
        transport.onEvent(received::set);
        transport.onText(new RequestingSocket(), "{\"type\":", false);
        assertThat(received.get()).isNull();
        transport.onText(new RequestingSocket(), "\"response.done\"}", true);
        assertThat(received.get()).isEqualTo("{\"type\":\"response.done\"}");
    }

    @Test void rejectsMissingModelBeforeAnyConnectionAttempt() {
        assertThatThrownBy(() -> OpenAiWebSocketTransport.endpoint(" "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static final class RequestingSocket implements WebSocket {
        @Override public CompletableFuture<WebSocket> sendText(CharSequence data, boolean last) { return CompletableFuture.completedFuture(this); }
        @Override public CompletableFuture<WebSocket> sendBinary(java.nio.ByteBuffer data, boolean last) { return CompletableFuture.completedFuture(this); }
        @Override public CompletableFuture<WebSocket> sendPing(java.nio.ByteBuffer message) { return CompletableFuture.completedFuture(this); }
        @Override public CompletableFuture<WebSocket> sendPong(java.nio.ByteBuffer message) { return CompletableFuture.completedFuture(this); }
        @Override public CompletableFuture<WebSocket> sendClose(int statusCode, String reason) { return CompletableFuture.completedFuture(this); }
        @Override public void request(long n) { }
        @Override public String getSubprotocol() { return ""; }
        @Override public boolean isOutputClosed() { return false; }
        @Override public boolean isInputClosed() { return false; }
        @Override public void abort() { }
    }
}
