package com.harborvoice.platform.dialogue;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class PromptTemplateTest {
    @Test void rendersFactsAsData() {
        var prompt = new PromptTemplate("greeting", "Hello {{business_name}}", java.util.Map.of("business_name", "Fictional Cafe"));
        assertThat(prompt.render()).isEqualTo("Hello Fictional Cafe");
    }
}
