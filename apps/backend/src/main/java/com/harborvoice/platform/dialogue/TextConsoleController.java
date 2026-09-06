package com.harborvoice.platform.dialogue;

import com.harborvoice.identity.Actor;
import com.harborvoice.platform.knowledge.GroundedContext;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Minimal authenticated console seam; retrieval is deliberately injected later. */
@RestController
public class TextConsoleController {
    private final DialoguePort dialogue;

    public TextConsoleController() { this(new FixtureDialoguePort()); }
    TextConsoleController(DialoguePort dialogue) { this.dialogue = dialogue; }

    public record Request(String utterance) { }
    public record Response(UUID tenantId, String responseText, String eventType) { }

    @PostMapping("/api/v1/console/text")
    Response respond(@AuthenticationPrincipal Actor actor, @RequestBody Request request) {
        if (actor == null || request == null || request.utterance() == null || request.utterance().isBlank()) {
            throw new IllegalArgumentException("authenticated utterance required");
        }
        DialoguePort.DialogueResult result = dialogue.respond(request.utterance(),
                new GroundedContext(request.utterance(), java.util.List.of()));
        return new Response(actor.tenantId(), result.responseText(), result.event().type());
    }
}
