package com.harborvoice.platform.evaluation;

import java.util.Comparator;
import java.util.List;

/** Selects the least expensive candidate that has passed the complete safety gate. */
public final class RealtimeModelSelector {
    private RealtimeModelSelector() { }

    public static RealtimeEvaluationResult selectCheapestPassing(List<RealtimeEvaluationResult> candidates,
                                                                   RealtimeEvaluationThresholds thresholds) {
        if (candidates == null || thresholds == null) throw new IllegalArgumentException("evaluation evidence required");
        return candidates.stream().filter(candidate -> passes(candidate, thresholds))
                .min(Comparator.comparingLong(RealtimeEvaluationResult::estimatedCostMinor)
                        .thenComparing(RealtimeEvaluationResult::candidateId))
                .orElse(null);
    }

    public static boolean passes(RealtimeEvaluationResult result, RealtimeEvaluationThresholds thresholds) {
        return result != null && result.accuracyBasisPoints() >= thresholds.minimumAccuracyBasisPoints()
                && result.firstAudioP95Millis() <= thresholds.maximumFirstAudioP95Millis()
                && result.interruptionFailures() <= thresholds.maximumInterruptionFailures()
                && result.safetyViolations() == 0 && result.reliabilityFailures() <= thresholds.maximumReliabilityFailures();
    }
}
