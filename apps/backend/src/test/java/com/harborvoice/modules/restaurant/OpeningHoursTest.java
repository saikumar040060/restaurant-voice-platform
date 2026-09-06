package com.harborvoice.modules.restaurant;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.*;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OpeningHoursTest {
    @Test void evaluatesHoursInConfiguredTimezone() {
        var hours = new OpeningHours(Map.of(DayOfWeek.MONDAY, new OpeningHours.Window(LocalTime.of(9, 0), LocalTime.of(17, 0))), ZoneId.of("America/Detroit"));
        assertThat(hours.openAt(ZonedDateTime.of(LocalDate.of(2026, 9, 7), LocalTime.of(17, 0), ZoneId.of("UTC")))).isTrue();
    }
}
