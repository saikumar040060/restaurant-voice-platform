package com.harborvoice.modules.restaurant;

import java.time.Instant;

public record PickupDetails(String customerName, String contact, Instant requestedAt) {
    public PickupDetails {
        if (customerName == null || customerName.isBlank() || customerName.length() > 120
                || contact == null || contact.isBlank() || requestedAt == null) {
            throw new IllegalArgumentException("pickup details required");
        }
    }
}
