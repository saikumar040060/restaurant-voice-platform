package com.harborvoice.platform.evaluation;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import org.junit.jupiter.api.Test;

class EvaluationScenarioTest {
    @Test void capturesModuleSpecificConversationExpectation() {
        var scenario = new EvaluationScenario("restaurant-menu-001", "restaurant",
                List.of("What is in the soup?"), "menu_question");
        assertThat(scenario.moduleId()).isEqualTo("restaurant");
        assertThat(scenario.utterances()).containsExactly("What is in the soup?");
    }
}
