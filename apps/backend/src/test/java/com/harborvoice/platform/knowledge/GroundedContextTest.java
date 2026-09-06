package com.harborvoice.platform.knowledge;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class GroundedContextTest {
    @Test
    void rejectsInvalidEvidenceSource() {
        assertThrows(IllegalArgumentException.class, () -> new GroundedContext.Source(
                UUID.randomUUID(), "hours", "", "fixture", 1));
    }

    @Test
    void detectsConflictingEvidenceByCanonicalKey() {
        var key = "hours";
        var context = new GroundedContext("hours", java.util.List.of(
                new GroundedContext.Source(UUID.randomUUID(), key, "Open 9-5", "owner", 1),
                new GroundedContext.Source(UUID.randomUUID(), key, "Open 10-6", "manager", 2)));
        assertTrue(context.hasConflict());
        assertFalse(context.answerable());
    }
}
