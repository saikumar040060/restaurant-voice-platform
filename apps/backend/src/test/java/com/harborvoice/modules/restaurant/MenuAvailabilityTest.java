package com.harborvoice.modules.restaurant;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MenuAvailabilityTest {
    @Test void defaultsUnknownItemsToUnavailable() {
        var item = new MenuItem("soup", "Fictional Soup", 800, Map.of());
        assertThat(MenuAvailability.isAvailable(item, Map.of())).isFalse();
        assertThat(MenuAvailability.isAvailable(item, Map.of("soup", true))).isTrue();
    }
}
