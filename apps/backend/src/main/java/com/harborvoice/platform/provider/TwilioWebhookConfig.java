package com.harborvoice.platform.provider;

import java.net.URI;
import java.util.Set;

/** Immutable sandbox-only configuration for a signed Twilio webhook. */
public record TwilioWebhookConfig(boolean enabled, String authToken, URI publicBaseUrl,
                                  Set<String> allowedPhoneNumbers, int replayWindowSeconds) {
    public TwilioWebhookConfig {
        authToken = authToken == null ? "" : authToken.trim();
        allowedPhoneNumbers = Set.copyOf(allowedPhoneNumbers == null ? Set.of() : allowedPhoneNumbers);
        if (replayWindowSeconds < 1 || replayWindowSeconds > 3600) {
            throw new IllegalArgumentException("invalid webhook replay window");
        }
        if (enabled && (authToken.isBlank() || publicBaseUrl == null || !"https".equals(publicBaseUrl.getScheme())
                || allowedPhoneNumbers.isEmpty())) {
            throw new IllegalArgumentException("enabled Twilio webhook requires HTTPS URL, token, and phone allowlist");
        }
    }

    public static TwilioWebhookConfig disabled() {
        return new TwilioWebhookConfig(false, "", null, Set.of(), 300);
    }

    public String callbackUrl() {
        return publicBaseUrl.resolve("/webhooks/twilio/voice").toString();
    }
}
