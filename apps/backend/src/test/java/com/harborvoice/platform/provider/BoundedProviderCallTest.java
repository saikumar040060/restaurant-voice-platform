package com.harborvoice.platform.provider;

import static org.assertj.core.api.Assertions.*;
import java.time.Duration;
import java.util.concurrent.ForkJoinPool;
import org.junit.jupiter.api.Test;

class BoundedProviderCallTest {
    @Test void timeoutCountsAsFailureAndOpenCircuitBlocksLaterCalls() {
        var breaker = new ProviderCircuitBreaker(1, Duration.ofMinutes(1));
        var call = new BoundedProviderCall<String>(breaker, Duration.ofMillis(5), ForkJoinPool.commonPool());
        assertThatThrownBy(() -> call.execute(() -> { try { Thread.sleep(100); } catch (InterruptedException ignored) { } return "late"; }))
                .isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> call.execute(() -> "blocked")).isInstanceOf(IllegalStateException.class);
    }

    @Test void successfulSyntheticOperationResetsBreaker() {
        var breaker = new ProviderCircuitBreaker(2, Duration.ofMinutes(1));
        var call = new BoundedProviderCall<>(breaker, Duration.ofSeconds(1), ForkJoinPool.commonPool());
        assertThat(call.execute(() -> "synthetic-audio-response")).isEqualTo("synthetic-audio-response");
        assertThat(breaker.state()).isEqualTo(ProviderCircuitBreaker.State.CLOSED);
    }
}
