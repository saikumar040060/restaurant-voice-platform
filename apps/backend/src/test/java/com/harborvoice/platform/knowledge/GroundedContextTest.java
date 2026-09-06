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
}
