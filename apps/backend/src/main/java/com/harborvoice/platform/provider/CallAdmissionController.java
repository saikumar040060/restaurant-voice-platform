package com.harborvoice.platform.provider;

import java.util.concurrent.atomic.AtomicInteger;

/** Enforces sandbox admission limits before a telephony/provider call starts. */
public final class CallAdmissionController {
    private final ProviderSafetyConfig config;
    private final AtomicInteger active = new AtomicInteger();
    private long spentMinor;

    public CallAdmissionController(ProviderSafetyConfig config) { this.config = config; }

    public boolean admit(String phoneNumber) {
        if (!config.enabled() || !config.allowedPhoneNumbers().contains(phoneNumber)) return false;
        while (true) {
            int current = active.get();
            if (current >= config.maxConcurrentCalls() || !active.compareAndSet(current, current + 1)) {
                if (current >= config.maxConcurrentCalls()) return false;
            } else return true;
        }
    }

    public void release() { active.updateAndGet(value -> Math.max(0, value - 1)); }

    public synchronized boolean charge(long amountMinor) {
        if (amountMinor < 0 || spentMinor > config.maxSpendMinor() - amountMinor) return false;
        spentMinor += amountMinor; return true;
    }

    public int activeCalls() { return active.get(); }
    public synchronized long remainingSpendMinor() { return config.maxSpendMinor() - spentMinor; }
}
