package com.harborvoice.modules.restaurant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class PickupDetailsTest {
    @Test void rejectsMissingCustomerContact() {
        assertThatThrownBy(() -> new PickupDetails("Customer", "", Instant.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
