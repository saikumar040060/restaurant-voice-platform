package com.harborvoice.platform.provider;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** One-use, bounded server-side admission for a Twilio Media Stream. It stores no audio. */
public final class TwilioMediaStreamAdmission {
    public record Grant(UUID businessId, UUID conversationId, SandboxCallLease lease) { }
    private record Pending(Grant grant, Instant expiresAt) { }
    private final ConcurrentHashMap<UUID, Pending> pending = new ConcurrentHashMap<>();
    private final SandboxSpendGate gate;
    public TwilioMediaStreamAdmission(SandboxSpendGate gate) { this.gate = java.util.Objects.requireNonNull(gate); }

    public UUID issue(UUID businessId, UUID conversationId, String caller, long worstCaseMinor, Instant now) {
        if (businessId == null || conversationId == null || now == null) throw new IllegalArgumentException("scoped stream required");
        var lease = gate.reserveLease(caller, worstCaseMinor, now).orElseThrow(() -> new IllegalStateException("sandbox admission denied"));
        UUID token = UUID.randomUUID();
        pending.put(token, new Pending(new Grant(businessId, conversationId, lease), now.plus(Duration.ofMinutes(2))));
        return token;
    }
    public Grant consume(UUID token, Instant now) {
        Pending item = pending.remove(token);
        if (item == null || now == null || !now.isBefore(item.expiresAt()) || !gate.active(item.grant().lease(), now)) {
            if (item != null) gate.release(item.grant().lease());
            throw new IllegalArgumentException("media stream admission denied");
        }
        return item.grant();
    }
    public void close(Grant grant) { if (grant != null) gate.release(grant.lease()); }
}
