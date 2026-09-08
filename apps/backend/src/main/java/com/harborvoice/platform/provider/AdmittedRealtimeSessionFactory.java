package com.harborvoice.platform.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.util.Objects;

/** Fail-closed factory: reserve safety limits before opening any realtime provider transport. */
public final class AdmittedRealtimeSessionFactory {
    private final OpenAiRealtimeConfig config;
    private final SandboxSpendGate gate;
    private final RealtimeTransportFactory transports;
    private final ObjectMapper json;
    private final Clock clock;
    private final BoundedProviderCall<RealtimeTransport> connectionCall;

    public AdmittedRealtimeSessionFactory(OpenAiRealtimeConfig config, SandboxSpendGate gate,
            RealtimeTransportFactory transports, ObjectMapper json, Clock clock,
            BoundedProviderCall<RealtimeTransport> connectionCall) {
        this.config = Objects.requireNonNull(config, "realtime configuration required");
        this.gate = Objects.requireNonNull(gate, "sandbox gate required");
        this.transports = Objects.requireNonNull(transports, "transport factory required");
        this.json = Objects.requireNonNull(json, "json required");
        this.clock = Objects.requireNonNull(clock, "clock required");
        this.connectionCall = Objects.requireNonNull(connectionCall, "connection deadline required");
    }

    public RealtimeSessionPort open(String caller, long worstCaseMinor) {
        if (!config.enabled()) throw new IllegalStateException("realtime provider disabled");
        SandboxCallLease lease = gate.reserveLease(caller, worstCaseMinor, clock.instant())
                .orElseThrow(() -> new IllegalStateException("sandbox realtime admission denied"));
        try {
            var transport = connectionCall.execute(() -> transports.connect(config));
            var session = new OpenAiRealtimeSession(config, transport, json);
            return new AdmittedRealtimeSession(session, gate, lease, clock);
        } catch (RuntimeException ex) {
            gate.release(lease);
            throw ex;
        }
    }
}
