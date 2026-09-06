package com.harborvoice.platform.dialogue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

class PromptTemplateTest {

    @Test
    void rejectsExpansionBeyondRenderedLimit() {
        var template = new PromptTemplate("tenant.prompt", "{{fact}}", java.util.Map.of("fact", "x".repeat(16_001)));
        assertThatThrownBy(template::render).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void rendersFactsAsData() {
        var prompt = new PromptTemplate("greeting", "Hello {{business_name}}", java.util.Map.of("business_name", "Fictional Cafe"));
        assertThat(prompt.render()).isEqualTo("Hello Fictional Cafe");
    }
}
