package com.harborvoice.platform.provider;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Narrow, tenant-scoped boundary between a realtime model and approved read-only tools. */
public interface RealtimeToolGateway {
    List<Map<String, Object>> definitions(UUID businessId);
    String execute(UUID businessId, String toolName, String argumentsJson);

    static RealtimeToolGateway disabled() {
        return new RealtimeToolGateway() {
            @Override public List<Map<String, Object>> definitions(UUID businessId) { return List.of(); }
            @Override public String execute(UUID businessId, String toolName, String argumentsJson) {
                throw new IllegalArgumentException("realtime tool unavailable");
            }
        };
    }
}
