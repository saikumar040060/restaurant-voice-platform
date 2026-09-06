package com.harborvoice.modules.restaurant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CurrencyValidationTest {
    @Test void rejectsMalformedCurrencyCodes() {
        var item = new MenuItem("tea", "Tea", 300, Map.of());
        assertThatThrownBy(() -> OrderPricing.quote(List.of(new OrderPricing.Line(item, null, 1)), "usd"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
