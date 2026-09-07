package com.harborvoice.platform.provider;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Bounds duplicate signed webhook acceptance without retaining request contents. */
public final class TwilioWebhookReplayGuard {
    private final long windowMillis;
    private final Map<String, Long> seen = new ConcurrentHashMap<>();

    public TwilioWebhookReplayGuard(int windowSeconds) {
        this.windowMillis = windowSeconds * 1000L;
    }

    public boolean firstSeen(String signedRequest) {
        long now = System.currentTimeMillis();
        seen.entrySet().removeIf(entry -> entry.getValue() <= now);
        String fingerprint = fingerprint(signedRequest);
        return seen.putIfAbsent(fingerprint, now + windowMillis) == null;
    }

    private String fingerprint(String signedRequest) {
        try {
            return Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256")
                    .digest(signedRequest.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }
}
