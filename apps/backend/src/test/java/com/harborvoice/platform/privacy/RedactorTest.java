package com.harborvoice.platform.privacy;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class RedactorTest {
    @Test void removesCommonSensitiveTextPatterns() {
        assertThat(Redactor.redact("Email a@b.example or call (555) 123-4567 card 4111 1111 1111 1111"))
                .doesNotContain("a@b.example", "555", "4111");
    }
}
