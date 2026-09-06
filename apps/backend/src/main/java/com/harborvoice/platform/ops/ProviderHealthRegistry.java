package com.harborvoice.platform.ops;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory provider health registry for routing fixtures; no provider calls are made. */
public final class ProviderHealthRegistry {
    private final Map<String, ProviderHealth> states = new ConcurrentHashMap<>();

    public void record(ProviderHealth health) {
        if (health == null) throw new IllegalArgumentException("health required");
        states.put(health.provider(), health);
    }

    public Optional<ProviderHealth> find(String provider) {
        if (provider == null || provider.isBlank()) return Optional.empty();
        return Optional.ofNullable(states.get(provider));
    }

    public void remove(String provider) {
        if (provider != null) states.remove(provider);
    }
}
