package com.harborvoice.modules.restaurant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OrderSubmissionTest {
    @Test void submissionRequiresConfirmedDraftAndCarriesHash() {
        var item = new MenuItem("tea", "Fictional Tea", 300, java.util.Map.of());
        var draft = OrderDraft.create(UUID.randomUUID(), List.of(new OrderPricing.Line(item, null, 1)), "USD");
        assertThatThrownBy(() -> OrderSubmission.from(draft)).isInstanceOf(IllegalArgumentException.class);
        var submission = OrderSubmission.from(draft.confirm(draft.quote()));
        assertThat(submission.quoteHash()).hasSize(64);
    }
}
