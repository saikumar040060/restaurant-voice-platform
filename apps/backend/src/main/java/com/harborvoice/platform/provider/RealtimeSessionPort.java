package com.harborvoice.platform.provider;

import com.harborvoice.platform.media.MediaEnvelope;
import com.harborvoice.platform.speech.TextToSpeechPort;
import java.util.function.Consumer;

/** Provider-neutral, server-side realtime audio session boundary. */
public interface RealtimeSessionPort extends AutoCloseable {
    default void configure(String instructions) { }
    default void onOutput(Consumer<TextToSpeechPort.AudioSynthesis> listener) { }
    void accept(MediaEnvelope input);
    TextToSpeechPort.AudioSynthesis nextOutput(long epoch);
    void cancel(long epoch);
    @Override void close();
}
