package com.harborvoice.modules.restaurant;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class MenuRecommendations {
    private MenuRecommendations() { }

    public static List<MenuItem> recommend(List<MenuItem> items, Map<String, Boolean> availability, int limit) {
        if (items == null || limit < 1) throw new IllegalArgumentException("items and limit required");
        return items.stream().filter(item -> MenuAvailability.isAvailable(item, availability))
                .sorted(Comparator.comparing(MenuItem::name).thenComparing(MenuItem::sku))
                .limit(limit).toList();
    }
}
