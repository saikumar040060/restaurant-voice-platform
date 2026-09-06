package com.harborvoice.platform.provider;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** Verifies Twilio webhook signatures without contacting Twilio. */
public final class TwilioRequestVerifier {
    public boolean verify(String authToken, String signature, String requestUrl, String canonicalParameters) {
        if (authToken == null || authToken.isBlank() || signature == null || requestUrl == null || canonicalParameters == null) return false;
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(authToken.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            byte[] digest = mac.doFinal((requestUrl + canonicalParameters).getBytes(StandardCharsets.UTF_8));
            return MessageDigest.isEqual(Base64.getEncoder().encode(digest), signature.getBytes(StandardCharsets.US_ASCII));
        } catch (Exception ex) { return false; }
    }
}
