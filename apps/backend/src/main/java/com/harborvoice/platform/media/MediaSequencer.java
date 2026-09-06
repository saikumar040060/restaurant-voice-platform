package com.harborvoice.platform.media;

/** Drops duplicate or stale frames and advances epochs only on explicit interruption. */
public final class MediaSequencer {
    private long lastSequence = -1;
    private long epoch;

    public synchronized boolean accept(MediaEnvelope frame) {
        if (frame.epoch() != epoch || frame.sequence() <= lastSequence) return false;
        lastSequence = frame.sequence();
        return true;
    }

    public synchronized void interrupt(long nextEpoch) {
        if (nextEpoch <= epoch) throw new IllegalArgumentException("epoch must advance");
        epoch = nextEpoch;
        lastSequence = -1;
    }
}
