package com.harborvoice.platform.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Read-only lookup over the unpublished sandbox draft. It cannot create orders or call providers. */
public final class SandboxMenuRealtimeToolGateway implements RealtimeToolGateway {
    public static final String TOOL_NAME = "restaurant_menu_lookup";
    private static final int MAX_ARGUMENTS = 2_000;
    private static final int MAX_RESULTS = 8;
    private final ObjectMapper json;
    private final UUID sandboxBusinessId;
    private final List<MenuItem> items;
    private final List<String> categories;

    public SandboxMenuRealtimeToolGateway(ObjectMapper json, UUID sandboxBusinessId) {
        this.json = java.util.Objects.requireNonNull(json, "json required");
        this.sandboxBusinessId = java.util.Objects.requireNonNull(sandboxBusinessId, "sandbox business required");
        try (InputStream source = getClass().getResourceAsStream("/reviews/indian-restaurant-menu.review-draft.json")) {
            if (source == null) throw new IllegalStateException("sandbox menu draft missing");
            JsonNode draft = json.readTree(source);
            if (!"NON_EXECUTABLE_OWNER_REVIEW_REQUIRED".equals(draft.path("status").asText())) {
                throw new IllegalStateException("only the unpublished review draft may be queried");
            }
            List<MenuItem> loaded = new ArrayList<>();
            Set<String> categoryNames = new LinkedHashSet<>();
            for (JsonNode item : draft.path("items")) {
                String description = description(item.path("source_block"));
                var flags = new ArrayList<String>();
                item.path("review_flags").forEach(flag -> flags.add(flag.asText()));
                loaded.add(new MenuItem(item.path("source_index").asInt(), item.path("category").asText(),
                        item.path("name").asText(), item.path("listed_price").asText(), description,
                        List.copyOf(flags), normalize(item.path("name").asText() + " " + item.path("category").asText()
                                + " " + description)));
                categoryNames.add(item.path("category").asText());
            }
            this.items = List.copyOf(loaded);
            this.categories = List.copyOf(categoryNames);
        } catch (Exception failure) {
            throw new IllegalStateException("sandbox menu lookup unavailable", failure);
        }
    }

    @Override public List<Map<String, Object>> definitions(UUID businessId) {
        requireTenant(businessId);
        return List.of(Map.of(
                "type", "function",
                "name", TOOL_NAME,
                "description", "Look up unpublished sandbox menu items, categories, descriptions, and listed prices. Use for every menu, dish, recommendation, price, ingredient, or category question.",
                "parameters", Map.of(
                        "type", "object",
                        "properties", Map.of("query", Map.of("type", "string", "description", "The caller's concise menu question or requested item/category")),
                        "required", List.of("query"),
                        "additionalProperties", false)));
    }

    @Override public String execute(UUID businessId, String toolName, String argumentsJson) {
        requireTenant(businessId);
        if (!TOOL_NAME.equals(toolName) || argumentsJson == null || argumentsJson.length() > MAX_ARGUMENTS) {
            throw new IllegalArgumentException("approved bounded menu lookup required");
        }
        try {
            String query = json.readTree(argumentsJson).path("query").asText("").trim();
            if (query.isEmpty() || query.length() > 300) throw new IllegalArgumentException("bounded menu query required");
            String normalized = normalize(query);
            if (categoryOverview(normalized)) {
                return json.writeValueAsString(Map.of("status", "UNPUBLISHED_TEST_DATA", "categories", categories,
                        "instruction", "Ask which category or dish the caller wants; do not recite every item."));
            }
            List<ScoredItem> matches = items.stream().map(item -> new ScoredItem(item, score(item, normalized)))
                    .filter(match -> match.score() > 0)
                    .sorted(Comparator.comparingInt(ScoredItem::score).reversed().thenComparingInt(match -> match.item().sourceIndex()))
                    .limit(MAX_RESULTS).toList();
            if (matches.isEmpty()) {
                return json.writeValueAsString(Map.of("status", "NO_MATCH", "query", query,
                        "instruction", "Say you could not find that in the supplied draft and ask for another name or category."));
            }
            List<Map<String, Object>> results = matches.stream().map(match -> {
                MenuItem item = match.item();
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("source_index", item.sourceIndex());
                result.put("category", item.category());
                result.put("name", item.name());
                result.put("listed_price", item.price());
                if (!item.description().isBlank()) result.put("supplied_description", item.description());
                if (!item.flags().isEmpty()) result.put("review_flags", item.flags());
                result.put("allergen_facts", "UNVERIFIED");
                return result;
            }).toList();
            return json.writeValueAsString(Map.of("status", "UNPUBLISHED_TEST_DATA", "query", query, "matches", results));
        } catch (IllegalArgumentException failure) {
            throw failure;
        } catch (Exception failure) {
            throw new IllegalArgumentException("invalid menu lookup arguments", failure);
        }
    }

    private void requireTenant(UUID businessId) {
        if (!sandboxBusinessId.equals(businessId)) throw new IllegalArgumentException("sandbox tenant mismatch");
    }

    private static int score(MenuItem item, String query) {
        String name = normalize(item.name());
        String category = normalize(item.category());
        if (name.equals(query)) return 10_000;
        if (name.contains(query) || query.contains(name)) return 5_000;
        if (category.equals(query) || category.contains(query)) return 3_000;
        int score = 0;
        for (String token : tokens(query)) {
            if (token.length() < 2) continue;
            if (name.contains(token)) score += 100;
            else if (category.contains(token)) score += 40;
            else if (item.searchable().contains(token)) score += 5;
        }
        return score;
    }

    private static boolean categoryOverview(String query) {
        return query.matches(".*\\b(categories|category)\\b.*")
                || Set.of("menu", "the menu", "menu options", "what is on the menu", "what do you serve",
                        "what do you have").contains(query);
    }

    private static Set<String> tokens(String value) {
        return new LinkedHashSet<>(List.of(value.split("\\s+")));
    }

    private static String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFKD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim();
    }

    private static String description(JsonNode sourceBlock) {
        StringBuilder value = new StringBuilder();
        for (int index = 1; index < sourceBlock.size(); index++) {
            if (!value.isEmpty()) value.append(' ');
            value.append(sourceBlock.get(index).asText());
        }
        return value.toString();
    }

    private record MenuItem(int sourceIndex, String category, String name, String price, String description,
                            List<String> flags, String searchable) { }
    private record ScoredItem(MenuItem item, int score) { }
}
