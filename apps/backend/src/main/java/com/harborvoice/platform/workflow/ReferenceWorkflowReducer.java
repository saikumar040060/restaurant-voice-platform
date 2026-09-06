package com.harborvoice.platform.workflow;

import java.util.Map;

/** Small deterministic reference reducer used by simulator tests. */
public final class ReferenceWorkflowReducer implements WorkflowReducer<ReferenceWorkflowReducer.State> {
    public record State(String phase, Map<String, String> fields) {
        public State {
            if (phase == null || phase.isBlank()) throw new IllegalArgumentException("phase required");
            fields = Map.copyOf(fields == null ? Map.of() : fields);
        }
    }

    @Override public State initial() { return new State("greeting", Map.of()); }

    @Override
    public State apply(State state, WorkflowEvent event) {
        return switch (event.type()) {
            case "question_answered" -> new State("active", state.fields());
            case "callback_requested" -> new State("callback_pending", state.fields());
            case "human_transfer_requested" -> new State("transferring", state.fields());
            case "ended" -> new State("ended", state.fields());
            default -> throw new IllegalArgumentException("unsupported workflow event: " + event.type());
        };
    }
}
