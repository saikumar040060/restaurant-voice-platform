package com.harborvoice.platform.conversation;

import com.harborvoice.platform.knowledge.GroundedContext;
import com.harborvoice.platform.speech.FixtureSpeechPorts;
import com.harborvoice.platform.speech.LocalStreamingPipeline;
import com.harborvoice.platform.workflow.ReferenceWorkflowReducer;
import java.util.UUID;

/** In-memory fixture for exercising media, speech, grounding, and workflow boundaries together. */
public final class LocalConversationSimulator {
    private final UUID conversationId = UUID.randomUUID();
    private final LocalStreamingPipeline pipeline;
    private ReferenceWorkflowReducer.State state = new ReferenceWorkflowReducer().initial();

    public LocalConversationSimulator() {
        pipeline = new LocalStreamingPipeline(FixtureSpeechPorts.stt(), transcript -> {
            if (transcript.finalText()) state = new ReferenceWorkflowReducer()
                    .apply(state, new com.harborvoice.platform.workflow.WorkflowEvent("question_answered", java.util.Map.of()));
        });
    }

    public void acceptAudio(long sequence, long epoch, byte[] payload) {
        pipeline.accept(new com.harborvoice.platform.media.MediaEnvelope(conversationId, "fixture", sequence,
                epoch, "pcm", payload, false));
    }

    public void interrupt(long nextEpoch) { pipeline.interrupt(nextEpoch); }
    public ReferenceWorkflowReducer.State state() { return state; }
}
