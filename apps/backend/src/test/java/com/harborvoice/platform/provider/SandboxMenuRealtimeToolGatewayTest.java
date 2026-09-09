package com.harborvoice.platform.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SandboxMenuRealtimeToolGatewayTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final UUID BUSINESS = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final SandboxMenuRealtimeToolGateway gateway = new SandboxMenuRealtimeToolGateway(JSON, BUSINESS);

    @Test void findsExactDishQuicklyWithPriceDescriptionAndReviewSafety() throws Exception {
        var result = JSON.readTree(gateway.execute(BUSINESS, SandboxMenuRealtimeToolGateway.TOOL_NAME,
                "{\"query\":\"Garlic Naan\"}"));

        assertThat(result.path("status").asText()).isEqualTo("UNPUBLISHED_TEST_DATA");
        assertThat(result.path("matches").size()).isGreaterThanOrEqualTo(1).isLessThanOrEqualTo(8);
        assertThat(result.at("/matches/0/name").asText()).isEqualTo("Garlic Naan");
        assertThat(result.at("/matches/0/listed_price").asText()).isEqualTo("$3.99");
        assertThat(result.at("/matches/0/supplied_description").asText()).containsIgnoringCase("garlic");
        assertThat(result.at("/matches/0/allergen_facts").asText()).isEqualTo("UNVERIFIED");
    }

    @Test void returnsCategoriesWithoutDumpingAll256Items() throws Exception {
        var result = JSON.readTree(gateway.execute(BUSINESS, SandboxMenuRealtimeToolGateway.TOOL_NAME,
                "{\"query\":\"what categories do you have\"}"));

        assertThat(result.path("categories").size()).isEqualTo(23);
        assertThat(result.toString()).contains("BREADS", "DESSERTS").doesNotContain("source_index");
    }

    @Test void naturalHaveQuestionStillFindsTheRequestedDish() throws Exception {
        var result = JSON.readTree(gateway.execute(BUSINESS, SandboxMenuRealtimeToolGateway.TOOL_NAME,
                "{\"query\":\"do you have naan\"}"));

        assertThat(result.path("matches").isArray()).isTrue();
        assertThat(result.path("matches").toString()).contains("Naan");
        assertThat(result.path("categories").isMissingNode()).isTrue();
    }

    @Test void reportsNoMatchAndRejectsCrossTenantOrUnapprovedTools() throws Exception {
        var result = JSON.readTree(gateway.execute(BUSINESS, SandboxMenuRealtimeToolGateway.TOOL_NAME,
                "{\"query\":\"pepperoni submarine sandwich\"}"));
        assertThat(result.path("status").asText()).isEqualTo("NO_MATCH");
        assertThatThrownBy(() -> gateway.execute(UUID.randomUUID(), SandboxMenuRealtimeToolGateway.TOOL_NAME,
                "{\"query\":\"naan\"}")).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tenant mismatch");
        assertThatThrownBy(() -> gateway.execute(BUSINESS, "create_order", "{}"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test void toolDefinitionContainsOnlyReadOnlyMenuLookup() {
        assertThat(gateway.definitions(BUSINESS)).hasSize(1);
        assertThat(gateway.definitions(BUSINESS).getFirst().get("name")).isEqualTo("restaurant_menu_lookup");
        assertThat(gateway.definitions(BUSINESS).toString()).doesNotContain("create_order", "payment", "customer_data");
    }
}
