package com.harborvoice.platform.speech;

import com.harborvoice.platform.dialogue.DialoguePort;
import com.harborvoice.platform.knowledge.GroundedContext;
import com.harborvoice.platform.escalation.EscalationCase;
import com.harborvoice.platform.escalation.EscalationPort;
import java.util.UUID;
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

    /**
     * Provider-neutral call boundary: a dialogue or synthesis failure never silently drops a
     * customer. The caller receives no invented response and a scoped human-transfer case is
     * created through the supplied adapter.
     */
    public SafeResult handleSafely(UUID businessId, UUID conversationId, String transcript,
                                   GroundedContext context, long epoch, EscalationPort escalations) {
        if (businessId == null || conversationId == null || escalations == null) {
            throw new IllegalArgumentException("business, conversation, and escalation port required");
        }
        try {
            return new SafeResult(handle(transcript, context, epoch), null);
        } catch (RuntimeException failure) {
            return new SafeResult(null, escalations.request(businessId, conversationId,
                    EscalationCase.Reason.SYSTEM_FAILURE));
        }
    }

    public record Result(DialoguePort.DialogueResult dialogue, TextToSpeechPort.AudioSynthesis audio) { }
    public record SafeResult(Result turn, EscalationCase escalation) { }
}
