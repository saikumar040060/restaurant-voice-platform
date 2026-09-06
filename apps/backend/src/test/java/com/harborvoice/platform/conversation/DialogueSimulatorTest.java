package com.harborvoice.platform.conversation;

import static org.assertj.core.api.Assertions.assertThat;

import com.harborvoice.platform.knowledge.GroundedContext;
import com.harborvoice.platform.workflow.ReferenceWorkflowReducer;
import org.junit.jupiter.api.Test;

class DialogueSimulatorTest {
    @Test
    void emitsDeterministicEventsAndPreservesGrounding() {
        var context = new GroundedContext("hours", java.util.List.of());
        var result = new DialogueSimulator().respond(new ReferenceWorkflowReducer().initial(), "Please call me back", context);
        assertThat(result.eventType()).isEqualTo("callback_requested");
        assertThat(result.state().phase()).isEqualTo("callback_pending");
        assertThat(result.context()).isSameAs(context);
    }
}
