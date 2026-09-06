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

    @Test void collapsesDuplicateSkuRecommendations() {
        var first = new MenuItem("pizza", "Pizza", 1000, Map.of());
        var duplicate = new MenuItem("pizza", "Pizza", 1200, Map.of());
        assertThat(MenuRecommendations.recommend(List.of(first, duplicate), Map.of("pizza", true), 5))
                .containsExactly(first);
    }
}
