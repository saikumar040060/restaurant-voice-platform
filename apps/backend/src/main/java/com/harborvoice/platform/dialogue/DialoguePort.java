package com.harborvoice.platform.dialogue;

import com.harborvoice.platform.knowledge.GroundedContext;
import com.harborvoice.platform.workflow.WorkflowEvent;

public interface DialoguePort {
    DialogueResult respond(String transcript, GroundedContext context);

    record DialogueResult(String responseText, WorkflowEvent event) {
        public DialogueResult {
            if (responseText == null || responseText.isBlank() || event == null) {
                throw new IllegalArgumentException("invalid dialogue result");
            }
        }
    }
}
