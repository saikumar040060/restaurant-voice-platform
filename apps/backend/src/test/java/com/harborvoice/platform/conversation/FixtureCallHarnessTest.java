package com.harborvoice.platform.conversation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.harborvoice.modules.restaurant.RestaurantBusinessModule;
import com.harborvoice.platform.module.ModuleRegistry;
import com.harborvoice.platform.module.ReferenceBusinessModule;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class FixtureCallHarnessTest {
    @Test void keepsTenMixedModuleCallsIsolatedUnderConcurrentSyntheticAudio() throws Exception {
        var harness = new FixtureCallHarness(new ModuleRegistry(java.util.List.of(
                new ReferenceBusinessModule(), new RestaurantBusinessModule())));
        var business = UUID.randomUUID();
        var calls = IntStream.range(0, 10).mapToObj(index -> new CallRef(
                harness.start(business, index < 5 ? "reference" : "restaurant"),
                index < 5 ? "reference" : "restaurant")).toList();

        try (var pool = Executors.newFixedThreadPool(10)) {
            pool.invokeAll(calls.stream().<java.util.concurrent.Callable<Boolean>>map(call -> () ->
                    harness.acceptAudio(call.id(), 0, 0, "fixture".getBytes(StandardCharsets.UTF_8))).toList());
        }

        for (var call : calls) {
            assertThat(harness.state(call.id(), business, call.moduleId()).phase()).isEqualTo("active");
        }
        assertThatThrownBy(() -> harness.state(calls.getFirst().id(), UUID.randomUUID(), calls.getFirst().moduleId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("fixture call scope denied");
    }

    @Test void rejectsUnknownModulesAndCalls() {
        var harness = new FixtureCallHarness(new ModuleRegistry(java.util.List.of(new ReferenceBusinessModule())));
        assertThatThrownBy(() -> harness.start(UUID.randomUUID(), "restaurant"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> harness.acceptAudio(UUID.randomUUID(), 0, 0, new byte[] {1}))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private record CallRef(UUID id, String moduleId) { }
}
