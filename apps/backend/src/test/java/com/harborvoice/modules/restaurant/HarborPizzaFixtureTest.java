package com.harborvoice.modules.restaurant;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HarborPizzaFixtureTest {
    @Test
    void exposesRequiredFictionalBasePrices() {
        RestaurantMenuKnowledge menu = HarborPizzaFixture.menu();
        assertEquals(7, menu.items().size());
        assertEquals(999, menu.items().stream().filter(i -> i.sku().equals("CHEESE-S")).findFirst().orElseThrow().priceMinor());
        assertEquals(1749, menu.items().stream().filter(i -> i.sku().equals("PEPPERONI-L")).findFirst().orElseThrow().priceMinor());
        assertTrue(menu.items().stream().anyMatch(i -> i.sku().equals("BYO")));
    }
}
