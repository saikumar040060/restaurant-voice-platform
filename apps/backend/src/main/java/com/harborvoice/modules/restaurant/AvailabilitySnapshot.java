package com.harborvoice.modules.restaurant;

import java.util.Map;

/** Immutable, named availability revision used by deterministic quote flows. */
public record AvailabilitySnapshot(String revision, Map<String, Boolean> values) {
    public AvailabilitySnapshot {
        if (revision == null || revision.isBlank()) throw new IllegalArgumentException("revision required");
        values = Map.copyOf(values == null ? Map.of() : values);
    }

    public boolean available(String sku) {
        return sku != null && Boolean.TRUE.equals(values.get(sku));
    }
}
