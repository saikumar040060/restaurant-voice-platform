package com.harborvoice.platform.provider;

import java.util.Set;

/** Credential-free safety envelope for sandbox provider adapters. */
public record ProviderSafetyConfig(boolean enabled, Set<String> allowedPhoneNumbers,
                                   int maxConcurrentCalls, int maxCallSeconds, long maxSpendMinor) {
    public ProviderSafetyConfig {
        allowedPhoneNumbers = Set.copyOf(allowedPhoneNumbers == null ? Set.of() : allowedPhoneNumbers);
        if (maxConcurrentCalls < 1 || maxConcurrentCalls > 100 || maxCallSeconds < 1 || maxCallSeconds > 3600
                || maxSpendMinor < 0) throw new IllegalArgumentException("invalid provider safety limits");
        if (enabled && allowedPhoneNumbers.isEmpty()) throw new IllegalArgumentException("enabled provider needs phone allowlist");
    }
}
