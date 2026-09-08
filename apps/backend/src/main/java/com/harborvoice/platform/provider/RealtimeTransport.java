package com.harborvoice.platform.provider;

import java.util.function.Consumer;

/** Narrow transport seam for a realtime provider; implementations own networking and credentials. */
public interface RealtimeTransport extends AutoCloseable {
    void send(String eventJson);
    void onEvent(Consumer<String> eventHandler);
    @Override void close();
}
