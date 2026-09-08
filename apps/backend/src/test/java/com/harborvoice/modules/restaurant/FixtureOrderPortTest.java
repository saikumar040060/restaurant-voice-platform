package com.harborvoice.modules.restaurant;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;

class FixtureOrderPortTest {
    @Test void acceptsOnlyConfirmedSubmission() {
        var item = new MenuItem("tea", "Fictional Tea", 300, java.util.Map.of());
        var draft = OrderDraft.create(UUID.randomUUID(), List.of(new OrderPricing.Line(item, null, 1)), "USD")
                .confirm(OrderPricing.quote(List.of(new OrderPricing.Line(item, null, 1)), "USD"));
        var result = new FixtureOrderPort().submit(OrderSubmission.from(draft));
        assertThat(result.state()).isEqualTo(OrderState.ACCEPTED);
    }

    @Test void makesDuplicateMockPosSubmissionsIdempotent() {
        var item = new MenuItem("tea", "Fictional Tea", 300, java.util.Map.of());
        var draft = OrderDraft.create(UUID.randomUUID(), List.of(new OrderPricing.Line(item, null, 1)), "USD")
                .confirm(OrderPricing.quote(List.of(new OrderPricing.Line(item, null, 1)), "USD"));
        var submission = OrderSubmission.from(draft);
        var port = new FixtureOrderPort();

        assertThat(port.submit(submission)).isSameAs(port.submit(submission));
    }

    @Test void makesConcurrentDuplicateMockPosSubmissionsIdempotent() throws Exception {
        var item = new MenuItem("tea", "Fictional Tea", 300, java.util.Map.of());
        var draft = OrderDraft.create(UUID.randomUUID(), List.of(new OrderPricing.Line(item, null, 1)), "USD")
                .confirm(OrderPricing.quote(List.of(new OrderPricing.Line(item, null, 1)), "USD"));
        var port = new FixtureOrderPort();
        try (var pool = Executors.newFixedThreadPool(10)) {
            var results = pool.invokeAll(java.util.stream.IntStream.range(0, 10)
                    .<Callable<OrderPort.Result>>mapToObj(ignored -> () -> port.submit(OrderSubmission.from(draft))).toList())
                    .stream().map(result -> {
                        try { return result.get(); } catch (Exception exception) { throw new AssertionError(exception); }
                    }).toList();
            assertThat(results).allSatisfy(result -> assertThat(result).isSameAs(results.getFirst()));
        }
    }
}
