package com.harborvoice.platform.conversation;

import java.util.List;
import java.util.Objects;

/** Bounded, immutable context passed to dialogue providers and business modules. */
public record ConversationContext(List<ConversationTurn> turns, int characterCount) {
    public ConversationContext {
        turns = List.copyOf(turns == null ? List.of() : turns);
        int actual = turns.stream().mapToInt(turn -> turn.text().length()).sum();
        if (characterCount < 0 || characterCount != actual || characterCount > 16_000) {
            throw new IllegalArgumentException("invalid conversation context size");
        }
    }

    public static ConversationContext bounded(List<ConversationTurn> source, int maxTurns, int maxCharacters) {
        Objects.requireNonNull(source, "source");
        if (maxTurns < 0 || maxCharacters < 0) {
            throw new IllegalArgumentException("context limits cannot be negative");
        }
        List<ConversationTurn> selected = new java.util.ArrayList<>();
        int chars = 0;
        for (int i = source.size() - 1; i >= 0 && selected.size() < maxTurns; i--) {
            ConversationTurn turn = source.get(i);
            if (chars + turn.text().length() > maxCharacters) break;
            selected.add(0, turn);
            chars += turn.text().length();
        }
        return new ConversationContext(selected, chars);
    }
}
