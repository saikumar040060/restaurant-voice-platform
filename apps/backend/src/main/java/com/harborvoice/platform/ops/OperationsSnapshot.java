package com.harborvoice.platform.ops;

import java.util.Map;

/** Redaction-safe operational view; it intentionally carries no provider configuration or secrets. */
public record OperationsSnapshot(Map<String, ProviderHealth> providers, long audioMillisRemaining,
                                 long inputCharactersRemaining) {
    public OperationsSnapshot {
        providers = Map.copyOf(providers == null ? Map.of() : providers);
        if (audioMillisRemaining < 0 || inputCharactersRemaining < 0) {
            throw new IllegalArgumentException("remaining usage cannot be negative");
        }
    }

    public static OperationsSnapshot from(ProviderHealthRegistry registry, UsageBudget budget) {
        if (registry == null || budget == null) throw new IllegalArgumentException("operations state required");
        Map<String, ProviderHealth> safeProviders = registry.snapshot().entrySet().stream()
                .collect(java.util.stream.Collectors.toUnmodifiableMap(Map.Entry::getKey,
                        entry -> new ProviderHealth(entry.getValue().provider(), entry.getValue().state(),
                                entry.getValue().observedAt(), "")));
        return new OperationsSnapshot(safeProviders, budget.audioRemaining(), budget.inputRemaining());
    }
}
