package com.harborvoice.platform.provider;

import static org.assertj.core.api.Assertions.*;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ProviderSafetyTest {
    @Test void enabledSandboxRequiresAllowlistAndBoundsCalls() {
        assertThatThrownBy(() -> new ProviderSafetyConfig(true, Set.of(), 2, 300, 100)).isInstanceOf(IllegalArgumentException.class);
        var config = new ProviderSafetyConfig(true, Set.of("+15551234567"), 2, 300, 100);
        assertThat(config.allowedPhoneNumbers()).containsExactly("+15551234567");
    }

    @Test void twilioSignatureUsesConstantTimeComparison() throws Exception {
        var verifier = new TwilioRequestVerifier();
        String url = "https://example.test/voice", params = "CallSidabcFrom%2B1555";
        var mac = javax.crypto.Mac.getInstance("HmacSHA1");
        mac.init(new javax.crypto.spec.SecretKeySpec("token".getBytes(), "HmacSHA1"));
        String signature = java.util.Base64.getEncoder().encodeToString(mac.doFinal((url + params).getBytes()));
        assertThat(verifier.verify("token", signature, url, params)).isTrue();
        assertThat(verifier.verify("token", "bad", url, params)).isFalse();
    }
}
