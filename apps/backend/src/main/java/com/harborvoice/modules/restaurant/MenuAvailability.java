package com.harborvoice.modules.restaurant;

import java.util.Map;

public final class MenuAvailability {
    private MenuAvailability() { }

    public static boolean isAvailable(MenuItem item, Map<String, Boolean> availability) {
        if (item == null) throw new IllegalArgumentException("menu item required");
        return availability != null && Boolean.TRUE.equals(availability.get(item.sku()));
    }
}
