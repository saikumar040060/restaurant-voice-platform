package com.harborvoice.modules.restaurant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harborvoice.identity.Actor;
import java.io.InputStream;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Owner-only, read-only source for a review draft. It never writes, imports, or publishes menu data. */
@RestController
public final class MenuReviewController {
    private final ObjectMapper json;
    private final JdbcMenuReviewRepository decisions;
    public MenuReviewController(ObjectMapper json, JdbcMenuReviewRepository decisions) { this.json = json; this.decisions = decisions; }
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
    @GetMapping("/api/v1/restaurant/menu-review-draft/decisions")
    List<MenuReviewDecision> decisions(@AuthenticationPrincipal Actor actor) {
        if (actor == null || actor.role() != Actor.Role.OWNER) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "owner review access required");
        return decisions.decisions(actor);
    }
    public record DecisionInput(int itemIndex, MenuReviewDecision.Decision decision, String correction, int expectedVersion) { }
    @PostMapping("/api/v1/restaurant/menu-review-draft/decisions")
    MenuReviewDecision decide(@AuthenticationPrincipal Actor actor, @RequestBody DecisionInput input) {
        if (actor == null || actor.role() != Actor.Role.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "owner review access required");
        }
        try {
            return decisions.decide(actor, input.itemIndex(), input.decision(), input.correction(), input.expectedVersion());
        } catch (IllegalStateException conflict) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "review decision changed; reload before retrying", conflict);
        } catch (IllegalArgumentException invalid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, invalid.getMessage(), invalid);
        }
    }
}
