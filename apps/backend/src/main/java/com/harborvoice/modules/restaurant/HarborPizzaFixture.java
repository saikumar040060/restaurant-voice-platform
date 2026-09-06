package com.harborvoice.modules.restaurant;

/** Fictional, deterministic menu used by offline platform evaluation only. */
public final class HarborPizzaFixture {
    private HarborPizzaFixture() { }

    public static RestaurantMenuKnowledge menu() {
        return new RestaurantMenuKnowledge(java.util.List.of(
                item("CHEESE-S", "Cheese Pizza (small)", 999),
                item("CHEESE-M", "Cheese Pizza (medium)", 1299),
                item("CHEESE-L", "Cheese Pizza (large)", 1599),
                item("PEPPERONI-S", "Pepperoni Pizza (small)", 1149),
                item("PEPPERONI-M", "Pepperoni Pizza (medium)", 1449),
                item("PEPPERONI-L", "Pepperoni Pizza (large)", 1749),
                new MenuItem("BYO", "Build Your Own Pizza", 999,
                        java.util.Map.of("medium", 300, "large", 600, "topping", 150))),
                java.util.List.of(
                        profile("CHEESE-S", "Classic cheese pizza with tomato sauce and mozzarella.",
                                java.util.List.of("dough", "tomato sauce", "mozzarella")),
                        profile("CHEESE-M", "Classic cheese pizza with tomato sauce and mozzarella.",
                                java.util.List.of("dough", "tomato sauce", "mozzarella")),
                        profile("CHEESE-L", "Classic cheese pizza with tomato sauce and mozzarella.",
                                java.util.List.of("dough", "tomato sauce", "mozzarella")),
                        profile("PEPPERONI-S", "Cheese pizza topped with pepperoni.",
                                java.util.List.of("dough", "tomato sauce", "mozzarella", "pepperoni")),
                        profile("PEPPERONI-M", "Cheese pizza topped with pepperoni.",
                                java.util.List.of("dough", "tomato sauce", "mozzarella", "pepperoni")),
                        profile("PEPPERONI-L", "Cheese pizza topped with pepperoni.",
                                java.util.List.of("dough", "tomato sauce", "mozzarella", "pepperoni")),
                        profile("BYO", "Build your own pizza with a size and selected toppings.",
                                java.util.List.of("dough", "tomato sauce", "mozzarella"))));
    }

    private static MenuItem item(String sku, String name, int price) {
        return new MenuItem(sku, name, price, java.util.Map.of());
    }

    private static DishProfile profile(String sku, String description, java.util.List<String> ingredients) {
        return new DishProfile(sku, description, ingredients, java.util.List.of("wheat", "milk"), java.util.List.of());
    }
}
