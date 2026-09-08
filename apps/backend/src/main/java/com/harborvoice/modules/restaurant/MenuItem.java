package com.harborvoice.modules.restaurant;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public record MenuItem(String sku, String name, int priceMinor, Map<String, Integer> modifiers,
                       Set<String> requiredModifierGroups) {
    public MenuItem {
        Objects.requireNonNull(sku); Objects.requireNonNull(name);
        modifiers = Map.copyOf(modifiers == null ? Map.of() : modifiers);
        requiredModifierGroups = Set.copyOf(requiredModifierGroups == null ? Set.of() : requiredModifierGroups);
        if (sku.isBlank() || name.isBlank() || priceMinor < 0 || modifiers.values().stream().anyMatch(value -> value < 0)
                || modifiers.keySet().stream().anyMatch(key -> key.isBlank())
                || requiredModifierGroups.stream().anyMatch(group -> group.isBlank())) {
            throw new IllegalArgumentException("invalid menu item");
        }
        for (String group : requiredModifierGroups) {
            if (modifiers.keySet().stream().noneMatch(key -> key.startsWith(group + ":"))) {
                throw new IllegalArgumentException("required modifier group has no options");
            }
        }
    }

    public MenuItem(String sku, String name, int priceMinor, Map<String, Integer> modifiers) {
        this(sku, name, priceMinor, modifiers, Set.of());
    }
}
