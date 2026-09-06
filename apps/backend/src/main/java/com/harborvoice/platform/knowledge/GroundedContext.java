package com.harborvoice.platform.knowledge;

import java.util.List;
import java.util.UUID;

public record GroundedContext(String query, List<Source> sources) {
    public GroundedContext {
        if (query == null || query.isBlank()) throw new IllegalArgumentException("query required");
        sources = List.copyOf(sources == null ? List.of() : sources);
    }

    public record Source(UUID id, String key, String content, String provenance, int version) {
        public Source {
            if (id == null || key == null || key.isBlank() || content == null || content.isBlank()
                    || content.length() > 16_000 || provenance == null || provenance.isBlank() || version < 1) {
                throw new IllegalArgumentException("invalid grounded source");
            }
        }
    }
}
