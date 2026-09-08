package com.harborvoice.modules.restaurant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harborvoice.identity.Actor;
import java.io.InputStream;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Owner-only, read-only source for a review draft. It never writes, imports, or publishes menu data. */
@RestController
public final class MenuReviewController {
    private final ObjectMapper json;
    public MenuReviewController(ObjectMapper json) { this.json = json; }
    @GetMapping("/api/v1/restaurant/menu-review-draft")
    JsonNode draft(@AuthenticationPrincipal Actor actor) {
        if (actor == null || actor.role() != Actor.Role.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "owner review access required");
        }
        try (InputStream source = getClass().getResourceAsStream("/reviews/indian-restaurant-menu.review-draft.json")) {
            if (source == null) throw new IllegalStateException("review draft unavailable");
            return json.readTree(source);
        } catch (java.io.IOException failure) { throw new IllegalStateException("review draft unavailable", failure); }
    }
}
