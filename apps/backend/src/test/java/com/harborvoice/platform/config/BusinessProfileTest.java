package com.harborvoice.platform.config;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class BusinessProfileTest {
    @Test void rejectsBlankLocaleAndTimezone() {
        assertThatThrownBy(() -> new BusinessProfile(UUID.randomUUID(), 1, "", "UTC", null,
                BusinessProfile.ApprovalState.DRAFT)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new BusinessProfile(UUID.randomUUID(), 1, "en-US", "", null,
                BusinessProfile.ApprovalState.DRAFT)).isInstanceOf(IllegalArgumentException.class);
    }
}
