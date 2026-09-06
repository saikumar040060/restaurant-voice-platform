package com.harborvoice.platform.speech;

import static org.assertj.core.api.Assertions.assertThat;
import com.harborvoice.platform.media.MediaEnvelope;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class LocalStreamingPipelineTest {
    @Test void dropsStaleAudioAfterInterruption() {
        var transcript = new AtomicReference<SpeechToTextPort.Transcript>();
        var pipeline = new LocalStreamingPipeline(FixtureSpeechPorts.stt(), transcript::set);
        var id = UUID.randomUUID();
        assertThat(pipeline.accept(new MediaEnvelope(id, "fixture", 0, 0, "pcm", new byte[] {1}, false))).isTrue();
        pipeline.interrupt(1);
        assertThat(pipeline.accept(new MediaEnvelope(id, "fixture", 1, 0, "pcm", new byte[] {2}, false))).isFalse();
        assertThat(pipeline.accept(new MediaEnvelope(id, "fixture", 0, 1, "pcm", new byte[] {3}, false))).isTrue();
        assertThat(transcript.get().epoch()).isEqualTo(1);
    }
}
