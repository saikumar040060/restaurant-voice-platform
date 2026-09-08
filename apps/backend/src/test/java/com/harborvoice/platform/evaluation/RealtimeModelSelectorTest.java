package com.harborvoice.platform.evaluation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class RealtimeModelSelectorTest {
    private final RealtimeEvaluationThresholds thresholds = new RealtimeEvaluationThresholds(9_800, 1_500, 1, 0);

    @Test void selectsTheCheapestFullyPassingCandidateRatherThanTheStrongestName() {
        var selected = RealtimeModelSelector.selectCheapestPassing(List.of(
                new RealtimeEvaluationResult("strong", 100, 100, 900, 0, 0, 0, 45),
                new RealtimeEvaluationResult("economy", 100, 99, 1_100, 1, 0, 0, 12),
                new RealtimeEvaluationResult("unsafe", 100, 100, 500, 0, 1, 0, 1)), thresholds);

        assertThat(selected.candidateId()).isEqualTo("economy");
    }

    @Test void declinesSelectionWhenNoCandidatePassesEveryThreshold() {
        assertThat(RealtimeModelSelector.selectCheapestPassing(List.of(
                new RealtimeEvaluationResult("fast-but-unsafe", 10, 10, 100, 0, 1, 0, 1)), thresholds)).isNull();
    }
}
