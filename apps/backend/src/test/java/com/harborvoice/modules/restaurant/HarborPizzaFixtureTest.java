package com.harborvoice.modules.restaurant;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HarborPizzaFixtureTest {
    @Test
    void exposesRequiredFictionalBasePrices() {
        RestaurantMenuKnowledge menu = HarborPizzaFixture.menu();
        assertEquals(13, menu.items().size());
        assertEquals(999, menu.items().stream().filter(i -> i.sku().equals("CHEESE-S")).findFirst().orElseThrow().priceMinor());
        assertEquals(1749, menu.items().stream().filter(i -> i.sku().equals("PEPPERONI-L")).findFirst().orElseThrow().priceMinor());
        assertTrue(menu.items().stream().anyMatch(i -> i.sku().equals("BYO")));
        assertTrue(menu.dish("PEPPERONI-M").ingredients().contains("pepperoni"));
        assertTrue(menu.dish("PEPPERONI-M").allergens().contains("milk"));
        assertTrue(menu.dish("BYO").description().contains("selected toppings"));
        assertEquals(549, menu.items().stream().filter(i -> i.sku().equals("GARLIC-BREAD")).findFirst().orElseThrow().priceMinor());
        assertEquals(599, menu.items().stream().filter(i -> i.sku().equals("SALAD-S")).findFirst().orElseThrow().priceMinor());
        assertEquals(299, menu.items().stream().filter(i -> i.sku().equals("DRINK-L")).findFirst().orElseThrow().priceMinor());
    }

    @Test void declaresTheFictionalSeedHoursForEveryDay() {
        var hours = HarborPizzaFixture.openingHours();
        assertThat(hours.timezone().getId()).isEqualTo("America/Detroit");
        assertThat(hours.windows()).hasSize(DayOfWeek.values().length)
                .allSatisfy((day, window) -> {
                    assertThat(window.opens()).isEqualTo(LocalTime.of(11, 0));
                    assertThat(window.closes()).isEqualTo(LocalTime.of(22, 0));
                });
    }
}
