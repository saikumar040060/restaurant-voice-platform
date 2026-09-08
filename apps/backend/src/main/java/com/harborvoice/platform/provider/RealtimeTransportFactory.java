package com.harborvoice.platform.provider;

@FunctionalInterface
public interface RealtimeTransportFactory {
    RealtimeTransport connect(OpenAiRealtimeConfig config);
}
