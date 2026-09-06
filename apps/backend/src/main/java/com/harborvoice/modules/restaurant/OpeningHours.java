package com.harborvoice.modules.restaurant;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

public record OpeningHours(Map<DayOfWeek, Window> windows, ZoneId timezone) {
    public OpeningHours {
        windows = Map.copyOf(windows == null ? Map.of() : windows);
        if (timezone == null) throw new IllegalArgumentException("timezone required");
    }
    public boolean openAt(ZonedDateTime instant) {
        ZonedDateTime local = instant.withZoneSameInstant(timezone);
        Window window = windows.get(local.getDayOfWeek());
        return window != null && !local.toLocalTime().isBefore(window.opens()) && local.toLocalTime().isBefore(window.closes());
    }
    public record Window(LocalTime opens, LocalTime closes) {
        public Window { if (opens == null || closes == null || !opens.isBefore(closes)) throw new IllegalArgumentException("invalid hours"); }
    }
}
