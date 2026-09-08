package com.harborvoice.platform.provider;

/** Reserves bounded spend before a realtime session starts. Amounts are integer minor units. */
public final class SandboxSpendGate {
    private final CallAdmissionController admission;
    public SandboxSpendGate(CallAdmissionController admission) { this.admission = admission; }
    public boolean reserve(String caller, long worstCaseMinor) {
        if (worstCaseMinor < 0 || !admission.admit(caller)) return false;
        if (admission.charge(worstCaseMinor)) return true;
        admission.release();
        return false;
    }
    public void release() { admission.release(); }
}
