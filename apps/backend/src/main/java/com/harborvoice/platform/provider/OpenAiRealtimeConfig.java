package com.harborvoice.platform.provider;

/** Server-side Realtime configuration; the key is injected only at runtime. */
public record OpenAiRealtimeConfig(boolean enabled, String apiKey, String model, int maxOutputTokens) {
    public OpenAiRealtimeConfig {
        apiKey = apiKey == null ? "" : apiKey.trim();
        model = model == null ? "" : model.trim();
        if (maxOutputTokens < 1 || maxOutputTokens > 4096) throw new IllegalArgumentException("invalid realtime output limit");
        if (enabled && (apiKey.isBlank() || model.isBlank())) {
            throw new IllegalArgumentException("enabled realtime provider requires key and evaluated model");
        }
    }
}
