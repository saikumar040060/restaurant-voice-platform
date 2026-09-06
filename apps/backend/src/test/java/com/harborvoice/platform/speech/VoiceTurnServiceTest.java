package com.harborvoice.platform.speech;

import static org.assertj.core.api.Assertions.assertThat;
import com.harborvoice.platform.dialogue.FixtureDialoguePort;
import com.harborvoice.platform.knowledge.GroundedContext;
import org.junit.jupiter.api.Test;

class VoiceTurnServiceTest {
    @Test void preservesEpochFromDialogueToSynthesizedAudio() {
        var context = new GroundedContext("hours", java.util.List.of());
        var result = new VoiceTurnService(new FixtureDialoguePort(), FixtureSpeechPorts.tts())
                .handle("What are your hours?", context, 4);
        assertThat(result.audio().epoch()).isEqualTo(4);
        assertThat(result.dialogue().event().type()).isEqualTo("question_answered");
    }
}
