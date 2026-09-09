package com.harborvoice.platform.provider;

import com.harborvoice.platform.media.MediaEnvelope;
import com.harborvoice.platform.speech.TextToSpeechPort;

/** Provider-neutral, server-side realtime audio session boundary. */
public interface RealtimeSessionPort extends AutoCloseable {
    default void configure(String instructions) { }
    void accept(MediaEnvelope input);
    TextToSpeechPort.AudioSynthesis nextOutput(long epoch);
    void cancel(long epoch);
    @Override void close();
}
