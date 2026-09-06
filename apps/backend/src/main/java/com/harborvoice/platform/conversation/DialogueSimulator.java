package com.harborvoice.platform.conversation;

import com.harborvoice.platform.knowledge.GroundedContext;
import com.harborvoice.platform.workflow.ReferenceWorkflowReducer;
import com.harborvoice.platform.workflow.WorkflowEvent;
import java.util.Locale;

/** Deterministic local fixture for exercising dialogue boundaries without a model provider. */
public final class DialogueSimulator {
    private final ReferenceWorkflowReducer reducer = new ReferenceWorkflowReducer();

    public Result respond(ReferenceWorkflowReducer.State state, String utterance, GroundedContext context) {
        if (utterance == null || utterance.isBlank()) throw new IllegalArgumentException("utterance required");
        String normalized = utterance.toLowerCase(Locale.ROOT);
        String eventType = normalized.contains("callback") || (normalized.contains("call") && normalized.contains("back")) ? "callback_requested"
                : normalized.contains("human") || normalized.contains("agent") ? "human_transfer_requested"
                : normalized.contains("bye") || normalized.contains("end") ? "ended" : "question_answered";
        var next = reducer.apply(state, new WorkflowEvent(eventType, java.util.Map.of()));
        return new Result(next, context, eventType);
    }

    public record Result(ReferenceWorkflowReducer.State state, GroundedContext context, String eventType) { }
}
