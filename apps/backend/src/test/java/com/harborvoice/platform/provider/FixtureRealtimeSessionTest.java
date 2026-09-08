package com.harborvoice.platform.provider;

import static org.assertj.core.api.Assertions.assertThat;

import com.harborvoice.platform.media.MediaEnvelope;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FixtureRealtimeSessionTest {
    @Test void emitsOnlyFinalFramesAndDropsInterruptedEpochOutput() {
        var session = new FixtureRealtimeSession();
        session.accept(envelope(0, false, "partial"));
        assertThat(session.nextOutput(0)).isNull();

        session.accept(envelope(0, true, "hello"));
        assertThat(new String(session.nextOutput(0).payload(), StandardCharsets.UTF_8)).isEqualTo("fixture:hello");

        session.accept(envelope(0, true, "stale soon"));
        session.cancel(1);
        assertThat(session.nextOutput(0)).isNull();
        assertThat(session.nextOutput(1)).isNull();

        session.accept(envelope(0, true, "old"));
        session.accept(envelope(1, true, "new"));
        assertThat(new String(session.nextOutput(1).payload(), StandardCharsets.UTF_8)).isEqualTo("fixture:new");
    }

    private static MediaEnvelope envelope(long epoch, boolean finalFrame, String text) {
        return new MediaEnvelope(UUID.randomUUID(), "fixture", 1, epoch, "pcm", text.getBytes(StandardCharsets.UTF_8), finalFrame);
    }
}
