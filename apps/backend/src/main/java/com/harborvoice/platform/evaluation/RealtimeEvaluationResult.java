package com.harborvoice.platform.evaluation;

/** Aggregate synthetic evaluation evidence for one provider/model candidate. */
public record RealtimeEvaluationResult(String candidateId, int evaluatedTurns, int correctTurns,
                                       long firstAudioP95Millis, int interruptionFailures,
                                       int safetyViolations, int reliabilityFailures,
                                       long estimatedCostMinor) {
    public RealtimeEvaluationResult {
        if (candidateId == null || candidateId.isBlank() || evaluatedTurns < 1 || correctTurns < 0
                || correctTurns > evaluatedTurns || firstAudioP95Millis < 0 || interruptionFailures < 0
                || safetyViolations < 0 || reliabilityFailures < 0 || estimatedCostMinor < 0) {
            throw new IllegalArgumentException("invalid realtime evaluation result");
        }
    }

    public int accuracyBasisPoints() {
        return (int) ((long) correctTurns * 10_000 / evaluatedTurns);
    }
}
