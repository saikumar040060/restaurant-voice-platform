package com.harborvoice.modules.restaurant;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;

/** Text fixtures representing synthetic transcripts. No model, audio provider, call, or order is used. */
class IndianMenuSyntheticConversationTest {
    private final SyntheticMenuAgent agent = SyntheticMenuAgent.load();

    @Test void answersAnExactUniquePriceOnlyFromTheDraft() {
        var result = agent.answer("How much is Plain Dosa?");
        assertThat(result.outcome()).isEqualTo(Outcome.GROUNDED_MENU_ANSWER);
        assertThat(result.response()).contains("Plain Dosa", "$10.99");
    }

    @Test void asksForClarificationWhenTheSourceContainsDuplicateNames() {
        var result = agent.answer("Tell me about Chicken Supreme");
        assertThat(result.outcome()).isEqualTo(Outcome.CLARIFICATION_REQUIRED);
        assertThat(result.response()).contains("more than once");
    }

    @Test void neverInventsAnUnknownDish() {
        var result = agent.answer("Do you have moonlight curry?");
        assertThat(result.outcome()).isEqualTo(Outcome.NOT_FOUND);
        assertThat(result.response()).contains("not find");
    }

    @Test void allergyAndDietaryQuestionsFailSafeBecauseFactsAreUnverified() {
        assertThat(agent.answer("Does Plain Dosa contain nuts?").outcome()).isEqualTo(Outcome.HUMAN_TRANSFER);
        assertThat(agent.answer("What dishes are safe for a dairy allergy?").outcome()).isEqualTo(Outcome.HUMAN_TRANSFER);
        assertThat(agent.answer("Which dishes are vegetarian?").outcome()).isEqualTo(Outcome.HUMAN_TRANSFER);
    }

    @Test void orderingAndCorrectionsRemainNonExecutable() {
        assertThat(agent.answer("Add two Plain Dosa and change that to three").outcome())
                .isEqualTo(Outcome.NON_EXECUTABLE_PREVIEW);
    }

    enum Outcome { GROUNDED_MENU_ANSWER, CLARIFICATION_REQUIRED, NOT_FOUND, HUMAN_TRANSFER, NON_EXECUTABLE_PREVIEW }
    record Answer(Outcome outcome, String response) { }

    private record DraftItem(String name, String listedPrice) { }

    private static final class SyntheticMenuAgent {
        private final List<DraftItem> items;

        private SyntheticMenuAgent(List<DraftItem> items) { this.items = List.copyOf(items); }

        static SyntheticMenuAgent load() {
            try (InputStream source = IndianMenuSyntheticConversationTest.class
                    .getResourceAsStream("/reviews/indian-restaurant-menu.review-draft.json")) {
                if (source == null) throw new IllegalStateException("review draft unavailable");
                JsonNode draft = new ObjectMapper().readTree(source);
                List<DraftItem> items = new ArrayList<>();
                draft.path("items").forEach(item -> items.add(new DraftItem(
                        item.path("name").asText(), item.path("listed_price").asText())));
                return new SyntheticMenuAgent(items);
            } catch (IOException failure) {
                throw new IllegalStateException("review draft unavailable", failure);
            }
        }

        Answer answer(String transcript) {
            String normalized = transcript.toLowerCase(Locale.ROOT);
            if (normalized.contains("allerg") || normalized.contains("contain nuts")
                    || normalized.contains("vegetarian") || normalized.contains("vegan")) {
                return new Answer(Outcome.HUMAN_TRANSFER,
                        "Those facts are unverified in this test menu, so I need an employee to help.");
            }
            if (normalized.contains("add ") || normalized.contains("order ") || normalized.contains("change that")) {
                return new Answer(Outcome.NON_EXECUTABLE_PREVIEW,
                        "This menu is an unpublished test preview and cannot create or change an order.");
            }
            List<DraftItem> matches = items.stream()
                    .filter(item -> normalized.contains(item.name().toLowerCase(Locale.ROOT))).toList();
            if (matches.isEmpty()) {
                return new Answer(Outcome.NOT_FOUND, "I could not find that dish in the reviewed test menu.");
            }
            if (matches.size() > 1) {
                return new Answer(Outcome.CLARIFICATION_REQUIRED,
                        "That name appears more than once in the source menu; an owner must clarify it.");
            }
            DraftItem item = matches.getFirst();
            return new Answer(Outcome.GROUNDED_MENU_ANSWER,
                    item.name() + " is listed at " + item.listedPrice() + " in the unpublished test menu.");
        }
    }
}
