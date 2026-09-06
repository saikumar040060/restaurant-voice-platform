package com.harborvoice.platform.dialogue;

import java.util.Map;

public record PromptTemplate(String templateId, String text, Map<String, String> facts) {
    public PromptTemplate {
        if (templateId == null || !templateId.matches("[a-z][a-z0-9_.-]{1,119}")
                || text == null || text.isBlank() || text.length() > 8000) throw new IllegalArgumentException("invalid prompt template");
        facts = Map.copyOf(facts == null ? Map.of() : facts);
    }
    public String render() {
        String rendered = text;
        for (var entry : facts.entrySet()) rendered = rendered.replace("{{" + entry.getKey() + "}}", entry.getValue());
        return rendered;
    }
}
