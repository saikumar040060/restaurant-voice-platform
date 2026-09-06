package com.harborvoice.platform.action;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ConfirmationEvidenceTest {
    @Test
    void rejectsNonCanonicalUtteranceHash() {
        assertThrows(IllegalArgumentException.class, () -> new ConfirmationEvidence(UUID.randomUUID(), 0,
                "confirm", "short", Instant.now()));
    }
}
