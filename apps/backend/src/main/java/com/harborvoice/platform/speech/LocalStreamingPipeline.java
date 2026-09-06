package com.harborvoice.platform.speech;

import com.harborvoice.platform.media.MediaEnvelope;
import com.harborvoice.platform.media.MediaSequencer;
import java.util.Objects;
import java.util.function.Consumer;

/** Local-only pipeline proving ordering and interruption behavior around speech adapters. */
public final class LocalStreamingPipeline {
    private final MediaSequencer sequencer = new MediaSequencer();
    private final SpeechToTextPort stt;
    private final Consumer<SpeechToTextPort.Transcript> sink;

    public LocalStreamingPipeline(SpeechToTextPort stt, Consumer<SpeechToTextPort.Transcript> sink) {
        this.stt = Objects.requireNonNull(stt, "stt");
        this.sink = Objects.requireNonNull(sink, "sink");
    }

    public boolean accept(MediaEnvelope frame) {
        if (!sequencer.accept(frame)) return false;
        sink.accept(stt.accept(frame));
        return true;
    }

    public void interrupt(long nextEpoch) { sequencer.interrupt(nextEpoch); }
}
