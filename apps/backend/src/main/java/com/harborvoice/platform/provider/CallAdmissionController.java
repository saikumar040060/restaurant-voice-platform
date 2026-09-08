package com.harborvoice.platform.provider;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** Enforces sandbox allowlist, concurrency, spend, and fixed call-duration limits. */
public final class CallAdmissionController {
    private final ProviderSafetyConfig config;
    private final AtomicInteger active = new AtomicInteger();
    private final ConcurrentHashMap<UUID, SandboxCallLease> leases = new ConcurrentHashMap<>();
    private long spentMinor;

    public CallAdmissionController(ProviderSafetyConfig config) {
        if (config == null) throw new IllegalArgumentException("provider safety configuration required");
        this.config = config;
    }

    /** Legacy slot admission. New realtime paths must retain the returned lease instead. */
    public boolean admit(String phoneNumber) { return admitLease(phoneNumber, Instant.now()).isPresent(); }

    public Optional<SandboxCallLease> admitLease(String phoneNumber, Instant now) {
        if (now == null || !config.enabled() || !config.allowedPhoneNumbers().contains(phoneNumber)) return Optional.empty();
        while (true) {
            int current = active.get();
            if (current >= config.maxConcurrentCalls()) return Optional.empty();
            if (active.compareAndSet(current, current + 1)) {
                SandboxCallLease lease = new SandboxCallLease(UUID.randomUUID(), now.plusSeconds(config.maxCallSeconds()));
                leases.put(lease.id(), lease);
                return Optional.of(lease);
            }
        }
    }

    public boolean active(SandboxCallLease lease, Instant now) {
        if (lease == null || now == null || !lease.equals(leases.get(lease.id()))) return false;
        if (!now.isBefore(lease.expiresAt())) {
            release(lease);
            return false;
        }
        return true;
    }

    public void release(SandboxCallLease lease) {
        if (lease != null && leases.remove(lease.id(), lease)) active.decrementAndGet();
    }

    /** Releases a legacy slot; retained only for existing fixture callers. */
    public void release() { active.updateAndGet(value -> Math.max(0, value - 1)); }

    public synchronized boolean charge(long amountMinor) {
        if (amountMinor < 0 || spentMinor > config.maxSpendMinor() - amountMinor) return false;
        spentMinor += amountMinor; return true;
    }

    public int activeCalls() { return active.get(); }
    public synchronized long remainingSpendMinor() { return config.maxSpendMinor() - spentMinor; }
}
