package com.harborvoice.modules.restaurant;

/** Customer order note with bounded content; payment and sensitive data stay outside this field. */
public record SpecialInstructions(String text) {
    public SpecialInstructions {
        if (text == null || text.length() > 500 || text.matches(".*(?i)(card|cvv|password|ssn).*")) {
            throw new IllegalArgumentException("invalid special instructions");
        }
        text = text.trim();
    }
}
