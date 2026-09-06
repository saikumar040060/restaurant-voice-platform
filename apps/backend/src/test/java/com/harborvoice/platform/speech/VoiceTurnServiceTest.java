package com.harborvoice.platform.speech;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.harborvoice.platform.dialogue.FixtureDialoguePort;
import com.harborvoice.platform.knowledge.GroundedContext;
import org.junit.jupiter.api.Test;

class VoiceTurnServiceTest {

    @Test
    void rejectsInvalidTranscriptAndEpoch() {
        var service = new VoiceTurnService(new FixtureDialoguePort(), FixtureSpeechPorts.tts());
        var context = new GroundedContext("q", java.util.List.of());
        assertThrows(IllegalArgumentException.class, () -> service.handle(" ", context, 0));
        assertThrows(IllegalArgumentException.class, () -> service.handle("q", context, -1));
    }
    @Test void preservesEpochFromDialogueToSynthesizedAudio() {
        var context = new GroundedContext("hours", java.util.List.of());
        var result = new VoiceTurnService(new FixtureDialoguePort(), FixtureSpeechPorts.tts())
                .handle("What are your hours?", context, 4);
        assertThat(result.audio().epoch()).isEqualTo(4);
        assertThat(result.dialogue().event().type()).isEqualTo("question_answered");
    }
}
