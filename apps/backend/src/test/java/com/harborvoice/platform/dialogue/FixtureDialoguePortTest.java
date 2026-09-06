package com.harborvoice.platform.dialogue;

import static org.assertj.core.api.Assertions.assertThat;
import com.harborvoice.platform.knowledge.GroundedContext;
import org.junit.jupiter.api.Test;

class FixtureDialoguePortTest {
    @Test void answersOnlyFromGroundedSource() {
        var context = new GroundedContext("hours", java.util.List.of(
                new GroundedContext.Source(java.util.UUID.randomUUID(), "hours", "Open 9-5", "owner", 1)));
        var result = new FixtureDialoguePort().respond("What are your hours?", context);
        assertThat(result.responseText()).isEqualTo("Open 9-5");
        assertThat(result.event().type()).isEqualTo("question_answered");
    }
}
