package com.harborvoice.modules.restaurant;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class RestaurantBusinessModuleTest {
    @Test void exposesRestaurantIntentsThroughPlatformContract() {
        var module = new RestaurantBusinessModule();
        assertThat(module.descriptor().moduleId()).isEqualTo("restaurant");
        assertThat(module.supportedIntents()).contains("menu_question", "pickup_order", "human_transfer");
    }
}
