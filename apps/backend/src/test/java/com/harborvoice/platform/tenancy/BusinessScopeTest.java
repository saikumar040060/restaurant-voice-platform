package com.harborvoice.platform.tenancy;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class BusinessScopeTest {
    @Test void enforcesBusinessAndOptionalLocationScope() {
        UUID business = UUID.randomUUID(), location = UUID.randomUUID();
        assertThat(new BusinessScope(business, location).includes(business, location)).isTrue();
        assertThat(new BusinessScope(business, location).includes(UUID.randomUUID(), location)).isFalse();
        assertThat(new BusinessScope(business, null).includes(business, UUID.randomUUID())).isTrue();
    }
}
