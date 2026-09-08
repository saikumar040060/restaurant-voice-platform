package com.harborvoice.platform.provider;

import com.harborvoice.platform.media.MediaEnvelope;
import com.harborvoice.platform.speech.TextToSpeechPort;

/** Fail-closed default: no secret or network activity occurs while disabled. */
public final class DisabledRealtimeSession implements RealtimeSessionPort {
    @Override public void accept(MediaEnvelope input) { throw new IllegalStateException("realtime provider disabled"); }
    @Override public TextToSpeechPort.AudioSynthesis nextOutput(long epoch) { throw new IllegalStateException("realtime provider disabled"); }
    @Override public void cancel(long epoch) { }
    @Override public void close() { }
}
