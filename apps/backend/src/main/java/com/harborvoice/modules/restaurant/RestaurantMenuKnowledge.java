package com.harborvoice.modules.restaurant;

import java.util.List;

public record RestaurantMenuKnowledge(List<MenuItem> items, List<DishProfile> dishes) {
    public RestaurantMenuKnowledge {
        items = List.copyOf(items == null ? List.of() : items);
        dishes = List.copyOf(dishes == null ? List.of() : dishes);
    }

    public DishProfile dish(String sku) {
        return dishes.stream().filter(item -> item.sku().equals(sku)).findFirst().orElse(null);
    }
}
