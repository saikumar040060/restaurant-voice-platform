package com.harborvoice.modules.restaurant;

import java.util.Map;
import java.util.Objects;

public record MenuItem(String sku, String name, int priceMinor, Map<String, Integer> modifiers) {
    public MenuItem {
        Objects.requireNonNull(sku); Objects.requireNonNull(name);
        modifiers = Map.copyOf(modifiers == null ? Map.of() : modifiers);
        if (sku.isBlank() || name.isBlank() || priceMinor < 0 || modifiers.values().stream().anyMatch(value -> value < 0)) {
            throw new IllegalArgumentException("invalid menu item");
        }
    }
}
