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
                        java.util.Map.of("size:small", 0, "size:medium", 300, "size:large", 600,
                                "topping:extra-cheese", 150, "topping:mushrooms", 100,
                                "topping:onions", 75, "topping:jalapenos", 75), java.util.Set.of("size")),
                item("GARLIC-BREAD", "Garlic Bread", 549),
                new MenuItem("SALAD-S", "House Salad (small)", 599, dressings(), java.util.Set.of("dressing")),
                new MenuItem("SALAD-L", "House Salad (large)", 899, dressings(), java.util.Set.of("dressing")),
                new MenuItem("DRINK-S", "Fountain Drink (small)", 199, flavors(), java.util.Set.of("flavor")),
                new MenuItem("DRINK-M", "Fountain Drink (medium)", 249, flavors(), java.util.Set.of("flavor")),
                new MenuItem("DRINK-L", "Fountain Drink (large)", 299, flavors(), java.util.Set.of("flavor"))),
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
                                java.util.List.of("dough", "tomato sauce", "mozzarella")),
                        profile("GARLIC-BREAD", "Garlic bread served as a side.", java.util.List.of("bread", "garlic", "butter")),
                        profile("SALAD-S", "House salad with a required dressing selection.", java.util.List.of("greens", "vegetables")),
                        profile("SALAD-L", "Large house salad with a required dressing selection.", java.util.List.of("greens", "vegetables")),
                        profile("DRINK-S", "Fountain drink with a required flavor selection.", java.util.List.of()),
                        profile("DRINK-M", "Fountain drink with a required flavor selection.", java.util.List.of()),
                        profile("DRINK-L", "Fountain drink with a required flavor selection.", java.util.List.of())));
    }

    /** Fictional local defaults: USD, 6% tax, 20 per line, $100 transfer threshold, 280-character notes. */
    public static RestaurantOrderingPolicy orderingPolicy() {
        return new RestaurantOrderingPolicy("USD", 600, 20, 10_000, 280);
    }

    private static MenuItem item(String sku, String name, int price) {
        return new MenuItem(sku, name, price, java.util.Map.of());
    }

    private static java.util.Map<String, Integer> dressings() {
        return java.util.Map.of("dressing:ranch", 0, "dressing:italian", 0, "dressing:vinaigrette", 0);
    }

    private static java.util.Map<String, Integer> flavors() {
        return java.util.Map.of("flavor:cola", 0, "flavor:lemon-lime", 0, "flavor:root-beer", 0);
    }

    private static DishProfile profile(String sku, String description, java.util.List<String> ingredients) {
        return new DishProfile(sku, description, ingredients, java.util.List.of("wheat", "milk"), java.util.List.of());
    }
}
