package com.harborvoice.modules.restaurant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.List;
import org.junit.jupiter.api.Test;

class RestaurantMenuKnowledgeTest {

    @Test
    void duplicateItemSkuFailsClosed() {
        MenuItem item = new MenuItem("same", "One", 100, java.util.Map.of());
        assertThatThrownBy(() -> new RestaurantMenuKnowledge(java.util.List.of(item, item), java.util.List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }
    @Test void resolvesDishFactsByStableSku() {
        var profile = new DishProfile("soup", "Vegetable soup", List.of("carrot"), List.of(), List.of("vegan"));
        var knowledge = new RestaurantMenuKnowledge(List.of(), List.of(profile));
        assertThat(knowledge.dish("soup")).isSameAs(profile);
        assertThat(knowledge.dish("missing")).isNull();
    }
}
