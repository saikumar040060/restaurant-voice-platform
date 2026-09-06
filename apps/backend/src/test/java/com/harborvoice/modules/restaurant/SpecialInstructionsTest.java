package com.harborvoice.modules.restaurant;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SpecialInstructionsTest {
    @Test
    void trimsAndBoundsNonSensitiveNotes() {
        assertEquals("extra crispy", new SpecialInstructions("  extra crispy ").text());
        assertEquals("cardboard sign", new SpecialInstructions("cardboard sign").text());
        assertThrows(IllegalArgumentException.class, () -> new SpecialInstructions("use card 4111111111111111"));
        assertThrows(IllegalArgumentException.class, () -> new SpecialInstructions("x".repeat(501)));
    }
}
