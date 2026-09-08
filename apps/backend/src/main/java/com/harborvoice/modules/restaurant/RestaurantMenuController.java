package com.harborvoice.modules.restaurant;

import com.harborvoice.identity.Actor;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

/** Read-only, authenticated restaurant menu surface for the future shared dashboard. */
@RestController
public final class RestaurantMenuController {
    private final RestaurantMenuService menus;
    private final RestaurantOrderQuery orders;

    public RestaurantMenuController(RestaurantMenuService menus, RestaurantOrderQuery orders) {
        this.menus = menus;
        this.orders = orders;
    }

    public record ItemView(String sku, String name, int priceMinor, Map<String, Integer> modifiers,
                           java.util.Set<String> requiredModifierGroups) { }
    public record MenuView(List<ItemView> items, OpeningHours hours) { }

    @GetMapping("/api/v1/restaurant/menu")
    MenuView menu(@AuthenticationPrincipal Actor actor) {
        return new MenuView(menus.approvedMenu(actor).items().stream()
                .map(item -> new ItemView(item.sku(), item.name(), item.priceMinor(), item.modifiers(),
                        item.requiredModifierGroups()))
                .toList(), HarborPizzaFixture.openingHours());
    }

    @GetMapping("/api/v1/restaurant/orders")
    List<RestaurantOrderQuery.Summary> orders(@AuthenticationPrincipal Actor actor,
            @RequestParam(defaultValue = "50") int limit) {
        menus.approvedMenu(actor);
        return orders.recent(actor.tenantId(), limit);
    }
}
