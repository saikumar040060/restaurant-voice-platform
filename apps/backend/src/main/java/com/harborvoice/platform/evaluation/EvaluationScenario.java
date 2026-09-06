package com.harborvoice.platform.evaluation;

import java.util.List;

public record EvaluationScenario(String id, String moduleId, List<String> utterances,
                                 String expectedOutcome) {
    public EvaluationScenario {
        if (id == null || id.isBlank() || moduleId == null || moduleId.isBlank()
                || utterances == null || utterances.isEmpty() || expectedOutcome == null || expectedOutcome.isBlank()) {
            throw new IllegalArgumentException("invalid evaluation scenario");
        }
        utterances = List.copyOf(utterances);
    }
}
