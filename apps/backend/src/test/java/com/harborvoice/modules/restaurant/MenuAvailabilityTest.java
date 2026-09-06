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

    @Test void snapshotCopiesValuesAndFailsClosedForUnknownSku() {
        var values = new java.util.HashMap<String, Boolean>();
        values.put("soup", true);
        var snapshot = new AvailabilitySnapshot("rev-1", values);
        values.put("soup", false);
        assertThat(snapshot.available("soup")).isTrue();
        assertThat(snapshot.available("missing")).isFalse();
        assertThat(MenuAvailability.isAvailable(new MenuItem("soup", "Soup", 100, Map.of()), snapshot)).isTrue();
    }

    @Test void rejectsMalformedSnapshotRevision() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> new AvailabilitySnapshot("bad revision", Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
