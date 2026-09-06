package com.harborvoice.platform.conversation;

import java.util.List;

/** Bounded, immutable context passed to dialogue providers and business modules. */
public record ConversationContext(List<ConversationTurn> turns, int characterCount) {
    public ConversationContext {
        turns = List.copyOf(turns == null ? List.of() : turns);
        if (characterCount < 0) {
            throw new IllegalArgumentException("character count cannot be negative");
        }
    }

    public static ConversationContext bounded(List<ConversationTurn> source, int maxTurns, int maxCharacters) {
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
