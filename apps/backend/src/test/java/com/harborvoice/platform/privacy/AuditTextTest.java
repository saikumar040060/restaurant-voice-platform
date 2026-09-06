package com.harborvoice.platform.privacy;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class AuditTextTest {
    @Test void redactsBeforeStorage() {
        assertThat(new AuditText("contact a@b.example").value()).doesNotContain("a@b.example");
    }
}
