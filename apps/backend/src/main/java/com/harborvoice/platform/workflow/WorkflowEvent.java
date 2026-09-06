package com.harborvoice.platform.workflow;

import java.util.Map;

public record WorkflowEvent(String type, Map<String, Object> data) {
    public WorkflowEvent {
        if (type == null || type.isBlank()) throw new IllegalArgumentException("event type required");
        data = Map.copyOf(data == null ? Map.of() : data);
    }
}
