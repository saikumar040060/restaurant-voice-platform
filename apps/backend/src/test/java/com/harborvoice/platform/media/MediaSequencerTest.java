package com.harborvoice.platform.media;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MediaSequencerTest {
    @Test void rejectsDuplicatesAndStaleEpochs() {
        var id = UUID.randomUUID(); var sequencer = new MediaSequencer();
        var first = new MediaEnvelope(id, "stream", 1, 0, "pcm", new byte[] {1}, false);
        assertThat(sequencer.accept(first)).isTrue();
        assertThat(sequencer.accept(first)).isFalse();
        sequencer.interrupt(1);
        assertThat(sequencer.accept(new MediaEnvelope(id, "stream", 1, 0, "pcm", new byte[] {1}, false))).isFalse();
        assertThat(sequencer.accept(new MediaEnvelope(id, "stream", 0, 1, "pcm", new byte[] {1}, false))).isTrue();
        assertThatThrownBy(() -> sequencer.interrupt(1)).isInstanceOf(IllegalArgumentException.class);
    }
}
