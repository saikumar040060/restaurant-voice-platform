package com.harborvoice.platform.knowledge;

import java.util.List;
import java.util.UUID;

public record GroundedContext(String query, List<Source> sources) {
    public GroundedContext {
        if (query == null || query.isBlank()) throw new IllegalArgumentException("query required");
        sources = List.copyOf(sources == null ? List.of() : sources);
    }

    public record Source(UUID id, String key, String content, String provenance, int version) { }
}
