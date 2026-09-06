package com.harborvoice.platform.escalation;

import java.util.UUID;

public interface EscalationPort {
    EscalationCase request(UUID businessId, UUID conversationId, EscalationCase.Reason reason);
    EscalationCase transition(UUID businessId, UUID caseId, EscalationCase.State next);
}
