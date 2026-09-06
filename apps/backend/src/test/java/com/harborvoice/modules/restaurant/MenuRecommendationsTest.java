package com.harborvoice.modules.restaurant;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MenuRecommendationsTest {
    @Test void ranksOnlyAvailableItemsDeterministically() {
        var soup = new MenuItem("soup", "Soup", 800, Map.of());
        var salad = new MenuItem("salad", "Salad", 700, Map.of());
        assertThat(MenuRecommendations.recommend(List.of(soup, salad), Map.of("soup", true, "salad", false), 5))
                .containsExactly(soup);
    }
}
