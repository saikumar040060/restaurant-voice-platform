package com.harborvoice.platform.privacy;

import java.util.regex.Pattern;

public final class Redactor {
    private static final Pattern EMAIL = Pattern.compile("\\b[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}\\b");
    private static final Pattern PHONE = Pattern.compile("(?<!\\d)(?:\\+?\\d[\\d .()/-]{7,}\\d)(?!\\d)");
    private static final Pattern CARD = Pattern.compile("(?<!\\d)(?:\\d[ -]?){13,19}(?!\\d)");

    private Redactor() { }

    public static String redact(String input) {
        if (input == null) return null;
        return CARD.matcher(PHONE.matcher(EMAIL.matcher(input).replaceAll("[EMAIL]"))
                .replaceAll("[PHONE]")).replaceAll("[CARD]");
    }
}
