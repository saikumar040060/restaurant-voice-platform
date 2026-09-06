package com.harborvoice.platform.ops;

public final class UsageBudget {
    private final long maxAudioMillis;
    private final long maxInputChars;
    private long audioMillis;
    private long inputChars;

    public UsageBudget(long maxAudioMillis, long maxInputChars) {
        if (maxAudioMillis < 1 || maxInputChars < 1) throw new IllegalArgumentException("positive limits required");
        this.maxAudioMillis = maxAudioMillis; this.maxInputChars = maxInputChars;
    }

    public synchronized boolean consume(long audio, long input) {
        if (audio < 0 || input < 0 || audioMillis > maxAudioMillis - audio || inputChars > maxInputChars - input) return false;
        audioMillis += audio; inputChars += input; return true;
    }

    public synchronized long audioRemaining() { return maxAudioMillis - audioMillis; }
    public synchronized long inputRemaining() { return maxInputChars - inputChars; }
}
