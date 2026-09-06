package com.harborvoice.platform.speech;

import com.harborvoice.platform.dialogue.DialoguePort;
import com.harborvoice.platform.knowledge.GroundedContext;
import java.util.Objects;

public final class VoiceTurnService {
    private final DialoguePort dialogue;
    private final TextToSpeechPort tts;

    public VoiceTurnService(DialoguePort dialogue, TextToSpeechPort tts) {
        this.dialogue = Objects.requireNonNull(dialogue); this.tts = Objects.requireNonNull(tts);
    }

    public Result handle(String transcript, GroundedContext context, long epoch) {
        if (transcript == null || transcript.isBlank() || context == null || epoch < 0) {
            throw new IllegalArgumentException("invalid voice turn");
        }
        var response = dialogue.respond(transcript, context);
        return new Result(response, tts.synthesize(response.responseText(), epoch));
    }

    public record Result(DialoguePort.DialogueResult dialogue, TextToSpeechPort.AudioSynthesis audio) { }
}
