package com.harborvoice.platform.ops;

import com.harborvoice.identity.Actor;
import com.harborvoice.platform.escalation.EscalationCase;
import com.harborvoice.platform.escalation.FixtureEscalationPort;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Authorized operations projection; it deliberately excludes transcripts, destinations, and contact data. */
@RestController
public final class EscalationQueueController {
    private final FixtureEscalationPort queue;
    public EscalationQueueController(FixtureEscalationPort queue) { this.queue = queue; }
    public record View(UUID caseId, EscalationCase.Reason reason, EscalationCase.State state, Instant createdAt) { }

    @GetMapping("/api/v1/operations/escalations")
    List<View> recent(@AuthenticationPrincipal Actor actor, @RequestParam(defaultValue = "50") int limit) {
        if (actor == null || (actor.role() != Actor.Role.OWNER && actor.role() != Actor.Role.MANAGER)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "operations access denied");
        }
        return queue.recent(actor.tenantId(), limit).stream()
                .map(item -> new View(item.id(), item.reason(), item.state(), item.createdAt())).toList();
    }
}
