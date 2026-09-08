package com.harborvoice.platform.evaluation;

/** Owner-approved pass thresholds for synthetic sandbox evidence. */
public record RealtimeEvaluationThresholds(int minimumAccuracyBasisPoints, long maximumFirstAudioP95Millis,
                                           int maximumInterruptionFailures, int maximumReliabilityFailures) {
    public RealtimeEvaluationThresholds {
        if (minimumAccuracyBasisPoints < 0 || minimumAccuracyBasisPoints > 10_000 || maximumFirstAudioP95Millis < 1
                || maximumInterruptionFailures < 0 || maximumReliabilityFailures < 0) {
            throw new IllegalArgumentException("invalid realtime evaluation thresholds");
        }
    }
}
