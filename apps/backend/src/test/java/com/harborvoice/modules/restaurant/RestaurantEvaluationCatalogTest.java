package com.harborvoice.modules.restaurant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RestaurantEvaluationCatalogTest {
    @Test void preservesAllThirtyRequiredScenariosAsTheEvaluationDenominator() {
        var scenarios = RestaurantEvaluationCatalog.requiredScenarios();

        assertThat(scenarios).hasSize(30);
        assertThat(scenarios).extracting(scenario -> scenario.id()).doesNotHaveDuplicates();
        assertThat(scenarios).allMatch(scenario -> scenario.moduleId().equals("restaurant"));
        assertThat(scenarios).extracting(scenario -> scenario.expectedOutcome())
                .contains("single_submission", "allergy_transfer", "cross_tenant_denial", "redacted_transcript");
    }
}
