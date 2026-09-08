package com.harborvoice.platform.ops;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.harborvoice.identity.Actor;
import com.harborvoice.platform.escalation.EscalationCase;
import com.harborvoice.platform.escalation.FixtureEscalationPort;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class EscalationQueueControllerTest {
    @Test void showsOnlyTheAuthorizedTenantsRedactionSafeQueue() {
        var queue = new FixtureEscalationPort(); var business = UUID.randomUUID();
        queue.request(business, UUID.randomUUID(), EscalationCase.Reason.SYSTEM_FAILURE);
        queue.request(UUID.randomUUID(), UUID.randomUUID(), EscalationCase.Reason.POLICY_BLOCK);
        var view = new EscalationQueueController(queue).recent(new Actor(UUID.randomUUID(), business, Actor.Role.MANAGER), 50);
        assertThat(view).hasSize(1).allSatisfy(item -> assertThat(item.reason()).isEqualTo(EscalationCase.Reason.SYSTEM_FAILURE));
    }
    @Test void deniesEmployees() {
        assertThatThrownBy(() -> new EscalationQueueController(new FixtureEscalationPort())
                .recent(new Actor(UUID.randomUUID(), UUID.randomUUID(), Actor.Role.EMPLOYEE), 50))
                .isInstanceOf(ResponseStatusException.class);
    }
}
