package com.harborvoice.modules.restaurant;

import java.util.List;

public record DishProfile(String sku, String description, List<String> ingredients,
                          List<String> allergens, List<String> dietaryTags) {
    public DishProfile {
        if (sku == null || sku.isBlank() || description == null || description.isBlank()) {
            throw new IllegalArgumentException("dish profile requires identity and description");
        }
        ingredients = List.copyOf(ingredients == null ? List.of() : ingredients);
        allergens = List.copyOf(allergens == null ? List.of() : allergens);
        dietaryTags = List.copyOf(dietaryTags == null ? List.of() : dietaryTags);
    }
}
