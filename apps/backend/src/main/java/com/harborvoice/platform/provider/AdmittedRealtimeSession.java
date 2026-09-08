package com.harborvoice.platform.provider;

import com.harborvoice.platform.media.MediaEnvelope;
import com.harborvoice.platform.speech.TextToSpeechPort;
import java.time.Clock;
import java.util.Objects;

/** Provider-neutral realtime wrapper that enforces an admitted sandbox lease for every operation. */
public final class AdmittedRealtimeSession implements RealtimeSessionPort {
    private final RealtimeSessionPort delegate;
    private final SandboxSpendGate gate;
    private final SandboxCallLease lease;
    private final Clock clock;
    private boolean closed;

    public AdmittedRealtimeSession(RealtimeSessionPort delegate, SandboxSpendGate gate, SandboxCallLease lease, Clock clock) {
        this.delegate = Objects.requireNonNull(delegate, "realtime session required");
        this.gate = Objects.requireNonNull(gate, "sandbox gate required");
        this.lease = Objects.requireNonNull(lease, "sandbox lease required");
        this.clock = Objects.requireNonNull(clock, "clock required");
    }

    @Override public synchronized void accept(MediaEnvelope input) {
        requireActive();
        delegate.accept(input);
    }

    @Override public synchronized TextToSpeechPort.AudioSynthesis nextOutput(long epoch) {
        requireActive();
        return delegate.nextOutput(epoch);
    }

    @Override public synchronized void cancel(long epoch) {
        if (!closed) delegate.cancel(epoch);
    }

    @Override public synchronized void close() {
        if (closed) return;
        closed = true;
        try { delegate.close(); }
        finally { gate.release(lease); }
    }

    private void requireActive() {
        if (closed || !gate.active(lease, clock.instant())) {
            close();
            throw new IllegalStateException("sandbox realtime lease expired or unavailable");
        }
    }
}
