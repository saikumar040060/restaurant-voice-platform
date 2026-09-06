package com.harborvoice.platform.dialogue;

import com.harborvoice.identity.Actor;
import com.harborvoice.platform.knowledge.GroundedContext;
import com.harborvoice.platform.knowledge.GroundedKnowledgeService;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

/** Minimal authenticated console seam; retrieval is deliberately injected later. */
@RestController
public class TextConsoleController {
    private final DialoguePort dialogue;
    private final GroundedKnowledgeService knowledge;

    public TextConsoleController() { this(new FixtureDialoguePort(), null); }
    @Autowired
    TextConsoleController(GroundedKnowledgeService knowledge) { this(new FixtureDialoguePort(), knowledge); }
    TextConsoleController(DialoguePort dialogue, GroundedKnowledgeService knowledge) {
        this.dialogue = dialogue; this.knowledge = knowledge;
    }

    public record Request(String utterance) { }
    public record Response(UUID tenantId, String responseText, String eventType) { }

    @PostMapping("/api/v1/console/text")
    Response respond(@AuthenticationPrincipal Actor actor, @RequestBody Request request) {
        if (actor == null || request == null || request.utterance() == null || request.utterance().isBlank()) {
            throw new IllegalArgumentException("authenticated utterance required");
        }
        GroundedContext context = knowledge == null ? new GroundedContext(request.utterance(), java.util.List.of())
                : knowledge.context(actor.tenantId(), null, request.utterance());
        DialoguePort.DialogueResult result = dialogue.respond(request.utterance(), context);
        return new Response(actor.tenantId(), result.responseText(), result.event().type());
    }
}
