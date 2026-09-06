package com.harborvoice.modules.restaurant;

import java.util.List;

public record RestaurantMenuKnowledge(List<MenuItem> items, List<DishProfile> dishes) {
    public RestaurantMenuKnowledge {
        items = List.copyOf(items == null ? List.of() : items);
        dishes = List.copyOf(dishes == null ? List.of() : dishes);
        if (items.stream().map(MenuItem::sku).distinct().count() != items.size()
                || dishes.stream().map(DishProfile::sku).distinct().count() != dishes.size()) {
            throw new IllegalArgumentException("duplicate menu identity");
        }
    }

    public DishProfile dish(String sku) {
        return dishes.stream().filter(item -> item.sku().equals(sku)).findFirst().orElse(null);
    }
}
