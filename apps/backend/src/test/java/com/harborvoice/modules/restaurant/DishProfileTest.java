package com.harborvoice.modules.restaurant;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import org.junit.jupiter.api.Test;

class DishProfileTest {
    @Test void preservesStructuredDishFacts() {
        var profile = new DishProfile("soup", "Vegetable soup", List.of("carrot"), List.of("none"), List.of("vegan"));
        assertThat(profile.allergens()).containsExactly("none");
        assertThat(profile.dietaryTags()).containsExactly("vegan");
    }
}
