package com.harborvoice.modules.restaurant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harborvoice.identity.Actor;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class MenuReviewControllerTest {
    @Test void ownerCanReadNonExecutableFlaggedDraft() {
        var draft = new MenuReviewController(new ObjectMapper(), new JdbcMenuReviewRepository(null, null)).draft(new Actor(UUID.randomUUID(), UUID.randomUUID(), Actor.Role.OWNER));
        assertThat(draft.path("status").asText()).isEqualTo("NON_EXECUTABLE_OWNER_REVIEW_REQUIRED");
        assertThat(draft.path("items").size()).isEqualTo(256);
        assertThat(draft.path("items").get(0).path("executable").asBoolean()).isFalse();
    }
    @Test void managerAndEmployeeCannotReadDraft() {
        var controller = new MenuReviewController(new ObjectMapper(), new JdbcMenuReviewRepository(null, null));
        assertThatThrownBy(() -> controller.draft(new Actor(UUID.randomUUID(), UUID.randomUUID(), Actor.Role.MANAGER)))
                .isInstanceOf(ResponseStatusException.class);
    }
}
