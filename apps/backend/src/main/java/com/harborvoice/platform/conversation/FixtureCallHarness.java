package com.harborvoice.platform.conversation;

import com.harborvoice.platform.module.ModuleRegistry;
import com.harborvoice.platform.workflow.ReferenceWorkflowReducer;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provider-free concurrent-call fixture. It validates compiled module selection
 * and per-call media state without creating a telephony or provider connection.
 */
public final class FixtureCallHarness {
    private record Call(UUID businessId, String moduleId, LocalConversationSimulator simulator) { }

    private final ModuleRegistry modules;
    private final ConcurrentHashMap<UUID, Call> calls = new ConcurrentHashMap<>();

    public FixtureCallHarness(ModuleRegistry modules) {
        this.modules = Objects.requireNonNull(modules, "module registry required");
    }

    public UUID start(UUID businessId, String moduleId) {
        if (businessId == null) throw new IllegalArgumentException("business required");
        modules.require(moduleId);
        UUID id = UUID.randomUUID();
        calls.put(id, new Call(businessId, moduleId, new LocalConversationSimulator()));
        return id;
    }

    public boolean acceptAudio(UUID callId, long sequence, long epoch, byte[] payload) {
        Call call = calls.get(callId);
        if (call == null) throw new IllegalArgumentException("unknown fixture call");
        call.simulator().acceptAudio(sequence, epoch, payload);
        return true;
    }

    public ReferenceWorkflowReducer.State state(UUID callId, UUID businessId, String moduleId) {
        Call call = calls.get(callId);
        if (call == null || !call.businessId().equals(businessId) || !call.moduleId().equals(moduleId)) {
            throw new IllegalArgumentException("fixture call scope denied");
        }
        return call.simulator().state();
    }
}
