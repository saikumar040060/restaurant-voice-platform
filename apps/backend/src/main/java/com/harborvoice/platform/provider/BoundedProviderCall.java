package com.harborvoice.platform.provider;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Supplier;

/** Executes a provider operation with a deadline and circuit-breaker guard. */
public final class BoundedProviderCall<T> {
    private final ProviderCircuitBreaker breaker;
    private final Duration timeout;
    private final Executor executor;

    public BoundedProviderCall(ProviderCircuitBreaker breaker, Duration timeout, Executor executor) {
        this.breaker = Objects.requireNonNull(breaker); this.timeout = Objects.requireNonNull(timeout); this.executor = Objects.requireNonNull(executor);
        if (timeout.isZero() || timeout.isNegative()) throw new IllegalArgumentException("positive timeout required");
    }

    public T execute(Supplier<T> operation) {
        if (!breaker.allowRequest()) throw new IllegalStateException("provider circuit open");
        try {
            T result = CompletableFuture.supplyAsync(operation, executor).orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS).join();
            breaker.recordSuccess(); return result;
        } catch (RuntimeException ex) {
            breaker.recordFailure();
            if (ex instanceof CompletionException completion && completion.getCause() instanceof RuntimeException cause) throw cause;
            throw ex;
        }
    }
}
