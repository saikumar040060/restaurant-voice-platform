package com.harborvoice.modules.restaurant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.harborvoice.identity.Actor;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Read-only transformation preview. It has no import, publication, POS, or ordering capability. */
@RestController
public final class MenuImportPreviewController {
    private static final Pattern FIXED_PRICE = Pattern.compile("^\\$(\\d+(?:\\.\\d{2})?)$");
    private final ObjectMapper json;
    private final JdbcMenuReviewRepository reviews;

    public MenuImportPreviewController(ObjectMapper json, JdbcMenuReviewRepository reviews) {
        this.json = json;
        this.reviews = reviews;
    }

    @GetMapping("/api/v1/restaurant/menu-review-draft/import-preview")
    ObjectNode preview(@AuthenticationPrincipal Actor actor) {
        if (actor == null || actor.role() != Actor.Role.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "owner review access required");
        }
        MenuReviewCompletion completion = reviews.completion(actor);
        List<MenuReviewDecision> decisions = reviews.decisions(actor);
        if (completion == null || decisions.size() != MenuReviewDraft.ITEM_COUNT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "completed menu review required");
        }
        Map<Integer, MenuReviewDecision> byIndex = new LinkedHashMap<>();
        decisions.forEach(decision -> byIndex.put(decision.itemIndex(), decision));
        JsonNode draft = readDraft();
        ObjectNode preview = json.createObjectNode();
        preview.put("label", "UNPUBLISHED — TEST-ONLY IMPORT PREVIEW");
        preview.put("executable", false);
        preview.put("publicationState", "UNPUBLISHED");
        preview.put("businessId", actor.tenantId().toString());
        preview.put("draftRevision", completion.draftRevision());
        preview.put("decisionSetHash", completion.decisionSetHash());
        ArrayNode items = preview.putArray("items");
        ArrayNode excluded = preview.putArray("excludedRejectedItems");
        int ready = 0;
        int needsResolution = 0;
        for (JsonNode source : draft.path("items")) {
            int sourceIndex = source.path("source_index").asInt();
            MenuReviewDecision decision = byIndex.get(sourceIndex);
            if (decision.decision() == MenuReviewDecision.Decision.REJECTED) {
                ObjectNode rejected = excluded.addObject();
                rejected.put("sourceIndex", sourceIndex);
                rejected.put("name", source.path("name").asText());
                rejected.put("rationale", decision.rationale());
                continue;
            }
            ObjectNode item = items.addObject();
            item.put("previewSku", "review-" + String.format("%03d", sourceIndex));
            item.put("sourceIndex", sourceIndex);
            item.put("category", source.path("category").asText());
            item.put("name", source.path("name").asText());
            item.put("listedPrice", source.path("listed_price").asText());
            item.put("decision", decision.decision().name());
            item.put("executable", false);
            if (decision.correction() != null) item.put("ownerCorrection", decision.correction());
            Matcher price = FIXED_PRICE.matcher(source.path("listed_price").asText());
            boolean fixedPrice = price.matches();
            if (fixedPrice) {
                item.put("priceMinor", new BigDecimal(price.group(1)).movePointRight(2)
                        .setScale(0, RoundingMode.UNNECESSARY).intValueExact());
            }
            ArrayNode blockers = item.putArray("importBlockers");
            if (!fixedPrice) blockers.add("PRICE_REQUIRES_STRUCTURED_OWNER_CORRECTION");
            if (decision.decision() == MenuReviewDecision.Decision.CORRECTED) {
                blockers.add("OWNER_CORRECTION_REQUIRES_STRUCTURED_MAPPING");
            }
            if (!source.path("review_flags").isEmpty()) {
                blockers.add("SOURCE_REVIEW_FLAGS_REQUIRE_RESOLUTION");
            }
            if (blockers.isEmpty()) ready++; else needsResolution++;
        }
        preview.put("includedCount", items.size());
        preview.put("excludedRejectedCount", excluded.size());
        preview.put("readyCount", ready);
        preview.put("needsResolutionCount", needsResolution);
        return preview;
    }

    private JsonNode readDraft() {
        try (InputStream source = getClass().getResourceAsStream("/reviews/indian-restaurant-menu.review-draft.json")) {
            if (source == null) throw new IllegalStateException("review draft unavailable");
            return json.readTree(source);
        } catch (IOException failure) {
            throw new IllegalStateException("review draft unavailable", failure);
        }
    }
}
