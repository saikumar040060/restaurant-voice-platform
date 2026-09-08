package com.harborvoice.modules.restaurant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.harborvoice.identity.Actor;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
    @GetMapping("/api/v1/restaurant/menu-review-draft/summary")
    MenuReviewSummary summary(@AuthenticationPrincipal Actor actor) {
        if (actor == null || actor.role() != Actor.Role.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "owner review access required");
        }
        return decisions.summary(actor, MenuReviewDraft.ITEM_COUNT);
    }

    @GetMapping("/api/v1/restaurant/menu-review-draft/completion")
    CompletionView completion(@AuthenticationPrincipal Actor actor) {
        if (actor == null || actor.role() != Actor.Role.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "owner review access required");
        }
        MenuReviewCompletion completion = decisions.completion(actor);
        return new CompletionView(completion != null, completion);
    }

    public record CompletionView(boolean completed, MenuReviewCompletion completion) { }

    @PostMapping("/api/v1/restaurant/menu-review-draft/completion")
    MenuReviewCompletion complete(@AuthenticationPrincipal Actor actor) {
        if (actor == null || actor.role() != Actor.Role.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "owner review access required");
        }
        try {
            return decisions.complete(actor);
        } catch (IllegalStateException incomplete) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, incomplete.getMessage(), incomplete);
        }
    }

    @GetMapping("/api/v1/restaurant/menu-review-draft/report")
    ObjectNode report(@AuthenticationPrincipal Actor actor) {
        if (actor == null || actor.role() != Actor.Role.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "owner review access required");
        }
        MenuReviewCompletion completion = decisions.completion(actor);
        List<MenuReviewDecision> saved = decisions.decisions(actor);
        if (completion == null || saved.size() != MenuReviewDraft.ITEM_COUNT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "completed menu review required");
        }
        UUID auditEventId = decisions.completionAuditEventId(actor);
        if (auditEventId == null) {
            throw new IllegalStateException("menu review completion audit unavailable");
        }
        Map<Integer, MenuReviewDecision> byIndex = new LinkedHashMap<>();
        saved.forEach(decision -> byIndex.put(decision.itemIndex(), decision));
        JsonNode source = draft(actor);
        ObjectNode report = json.createObjectNode();
        report.put("label", "UNPUBLISHED — TEST DATA");
        report.put("businessId", actor.tenantId().toString());
        report.put("draftRevision", completion.draftRevision());
        report.put("decisionSetHash", completion.decisionSetHash());
        report.put("completedAt", completion.completedAt().toString());
        report.put("completionAuditEventId", auditEventId.toString());
        report.put("totalItems", MenuReviewDraft.ITEM_COUNT);
        ObjectNode categories = report.putObject("categories");
        for (JsonNode item : source.path("items")) {
            MenuReviewDecision decision = byIndex.get(item.path("source_index").asInt());
            ArrayNode entries = categories.withArray(item.path("category").asText());
            ObjectNode entry = entries.addObject();
            entry.put("sourceIndex", decision.itemIndex());
            entry.put("name", item.path("name").asText());
            entry.put("listedPrice", item.path("listed_price").asText());
            entry.put("decision", decision.decision().name());
            if (decision.correction() != null) entry.put("correction", decision.correction());
            if (decision.rationale() != null) entry.put("rejectionRationale", decision.rationale());
        }
        return report;
    }

    public record DecisionInput(int itemIndex, MenuReviewDecision.Decision decision, String correction, String rationale, int expectedVersion) { }
    @PostMapping("/api/v1/restaurant/menu-review-draft/decisions")
    MenuReviewDecision decide(@AuthenticationPrincipal Actor actor, @RequestBody DecisionInput input) {
        if (actor == null || actor.role() != Actor.Role.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "owner review access required");
        }
        try {
            return decisions.decide(actor, input.itemIndex(), input.decision(), input.correction(), input.rationale(), input.expectedVersion());
        } catch (IllegalStateException conflict) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "review decision changed; reload before retrying", conflict);
        } catch (IllegalArgumentException invalid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, invalid.getMessage(), invalid);
        }
    }

    public record BulkDecisionInput(List<DecisionInput> decisions) { }

    @PostMapping("/api/v1/restaurant/menu-review-draft/decisions/bulk")
    List<MenuReviewDecision> decideAll(@AuthenticationPrincipal Actor actor, @RequestBody BulkDecisionInput input) {
        if (actor == null || actor.role() != Actor.Role.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "owner review access required");
        }
        try {
            List<JdbcMenuReviewRepository.DecisionWrite> writes = input == null || input.decisions() == null ? null
                    : input.decisions().stream().map(decision -> new JdbcMenuReviewRepository.DecisionWrite(
                            decision.itemIndex(), decision.decision(), decision.correction(), decision.rationale(), decision.expectedVersion())).toList();
            return decisions.decideAll(actor, writes);
        } catch (IllegalStateException conflict) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "review decision changed; reload before retrying", conflict);
        } catch (IllegalArgumentException invalid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, invalid.getMessage(), invalid);
        }
    }
}
