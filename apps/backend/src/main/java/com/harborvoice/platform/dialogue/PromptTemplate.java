package com.harborvoice.platform.dialogue;

import java.util.Map;

public record PromptTemplate(String templateId, String text, Map<String, String> facts) {
    public PromptTemplate {
        if (templateId == null || !templateId.matches("[a-z][a-z0-9_.-]{1,119}")
                || text == null || text.isBlank() || text.length() > 8000) throw new IllegalArgumentException("invalid prompt template");
        facts = Map.copyOf(facts == null ? Map.of() : facts);
        if (facts.size() > 100 || facts.keySet().stream().anyMatch(key -> key == null || !key.matches("[a-z][a-z0-9_.-]{0,63}"))) {
            throw new IllegalArgumentException("invalid prompt facts");
        }
    }
    public String render() {
        String rendered = text;
        for (var entry : facts.entrySet()) {
            rendered = rendered.replace("{{" + entry.getKey() + "}}", entry.getValue());
            if (rendered.length() > 16_000) throw new IllegalArgumentException("rendered prompt too long");
        }
        return rendered;
    }
}
