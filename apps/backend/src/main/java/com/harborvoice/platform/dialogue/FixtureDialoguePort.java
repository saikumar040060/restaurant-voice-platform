package com.harborvoice.platform.dialogue;

import com.harborvoice.platform.knowledge.GroundedContext;
import com.harborvoice.platform.workflow.WorkflowEvent;
import java.util.Map;

/** Deterministic dialogue fixture; it never interprets context as executable instructions. */
public final class FixtureDialoguePort implements DialoguePort {
    @Override
    public DialogueResult respond(String transcript, GroundedContext context) {
        if (transcript == null || transcript.isBlank()) throw new IllegalArgumentException("transcript required");
        String answer = !context.answerable() ? (context.hasConflict()
                ? "I found conflicting approved information, so I need to clarify that before answering."
                : "I do not have an approved answer for that.")
                : context.sources().getFirst().content();
        return new DialogueResult(answer, new WorkflowEvent("question_answered", Map.of()));
    }
}
