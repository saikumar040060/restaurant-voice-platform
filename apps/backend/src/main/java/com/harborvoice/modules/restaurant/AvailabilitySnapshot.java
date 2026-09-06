package com.harborvoice.modules.restaurant;

import java.util.Map;

/** Immutable, named availability revision used by deterministic quote flows. */
public record AvailabilitySnapshot(String revision, Map<String, Boolean> values) {
    public AvailabilitySnapshot {
        if (revision == null || revision.isBlank() || revision.length() > 128
                || !revision.matches("[A-Za-z0-9._:-]+")) throw new IllegalArgumentException("invalid revision");
        values = Map.copyOf(values == null ? Map.of() : values);
        if (values.size() > 10_000) throw new IllegalArgumentException("availability snapshot too large");
    }

    public boolean available(String sku) {
        return sku != null && Boolean.TRUE.equals(values.get(sku));
    }
}
