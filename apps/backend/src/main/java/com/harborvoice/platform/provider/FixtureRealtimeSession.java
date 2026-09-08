package com.harborvoice.platform.provider;

import com.harborvoice.platform.media.MediaEnvelope;
import com.harborvoice.platform.speech.TextToSpeechPort;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;

/** Bounded local realtime fixture. It has no provider SDK, network activity, or secret access. */
public final class FixtureRealtimeSession implements RealtimeSessionPort {
    private static final int MAX_OUTPUTS = 32;
    private final Deque<TextToSpeechPort.AudioSynthesis> outputs = new ArrayDeque<>();
    private long epoch;
    private boolean closed;

    @Override public synchronized void accept(MediaEnvelope input) {
        if (closed) throw new IllegalStateException("realtime session closed");
        if (input.epoch() < epoch) return;
        if (input.epoch() > epoch) {
            epoch = input.epoch();
            outputs.clear();
        }
        if (!input.finalFrame()) return;
        if (outputs.size() >= MAX_OUTPUTS) throw new IllegalStateException("fixture output queue full");
        byte[] response = ("fixture:" + new String(input.payload(), StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8);
        outputs.addLast(new TextToSpeechPort.AudioSynthesis(input.codec(), response, epoch));
    }

    @Override public synchronized TextToSpeechPort.AudioSynthesis nextOutput(long requestedEpoch) {
        if (closed || requestedEpoch != epoch) return null;
        return outputs.pollFirst();
    }

    @Override public synchronized void cancel(long nextEpoch) {
        if (nextEpoch < epoch) return;
        epoch = nextEpoch;
        outputs.clear();
    }

    @Override public synchronized void close() {
        closed = true;
        outputs.clear();
    }
}
