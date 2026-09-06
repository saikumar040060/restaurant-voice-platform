package com.harborvoice.platform.media;

import java.util.Objects;
import java.util.UUID;

public record MediaEnvelope(UUID conversationId, String providerStreamId, long sequence,
                           long epoch, String codec, byte[] payload, boolean finalFrame) {
    public MediaEnvelope {
        Objects.requireNonNull(conversationId, "conversationId");
        Objects.requireNonNull(providerStreamId, "providerStreamId");
        Objects.requireNonNull(codec, "codec");
        Objects.requireNonNull(payload, "payload");
        payload = payload.clone();
        if (sequence < 0 || epoch < 0 || providerStreamId.isBlank() || codec.isBlank()
                || payload.length > 64 * 1024) {
            throw new IllegalArgumentException("invalid media envelope");
        }
    }

    @Override public byte[] payload() { return payload.clone(); }
}
