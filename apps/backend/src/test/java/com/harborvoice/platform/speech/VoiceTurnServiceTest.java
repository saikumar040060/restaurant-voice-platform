package com.harborvoice.platform.speech;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.harborvoice.platform.dialogue.FixtureDialoguePort;
import com.harborvoice.platform.knowledge.GroundedContext;
import com.harborvoice.platform.escalation.FixtureEscalationPort;
import com.harborvoice.platform.escalation.EscalationCase;
import java.util.UUID;
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

    @Test void providerFailureCreatesScopedHumanTransferCaseWithoutInventingAudio() {
        var failingDialogue = (com.harborvoice.platform.dialogue.DialoguePort) (transcript, context) -> { throw new IllegalStateException("fixture failure"); };
        var port = new FixtureEscalationPort();
        var result = new VoiceTurnService(failingDialogue, FixtureSpeechPorts.tts()).handleSafely(
                UUID.randomUUID(), UUID.randomUUID(), "help", new GroundedContext("q", java.util.List.of()), 0, port);
        assertThat(result.turn()).isNull();
        assertThat(result.escalation().reason()).isEqualTo(EscalationCase.Reason.SYSTEM_FAILURE);
    }
}
