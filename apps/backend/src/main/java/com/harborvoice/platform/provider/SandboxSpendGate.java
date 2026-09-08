package com.harborvoice.platform.provider;

import java.time.Instant;
import java.util.Optional;

/** Reserves bounded spend and duration before a realtime sandbox session starts. */
public final class SandboxSpendGate {
    private final CallAdmissionController admission;
    public SandboxSpendGate(CallAdmissionController admission) {
        if (admission == null) throw new IllegalArgumentException("admission required");
        this.admission = admission;
    }
    public boolean reserve(String caller, long worstCaseMinor) {
        return reserveLease(caller, worstCaseMinor, Instant.now()).isPresent();
    }
    public Optional<SandboxCallLease> reserveLease(String caller, long worstCaseMinor, Instant now) {
        if (worstCaseMinor < 0) return Optional.empty();
        Optional<SandboxCallLease> lease = admission.admitLease(caller, now);
        if (lease.isEmpty()) return Optional.empty();
        if (admission.charge(worstCaseMinor)) return lease;
        admission.release(lease.get());
        return Optional.empty();
    }
    public boolean active(SandboxCallLease lease, Instant now) { return admission.active(lease, now); }
    public void release(SandboxCallLease lease) { admission.release(lease); }
    public void release() { admission.release(); }
}
