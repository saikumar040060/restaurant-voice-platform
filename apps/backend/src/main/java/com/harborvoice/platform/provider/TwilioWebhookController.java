package com.harborvoice.platform.provider;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sandbox ingress only. It accepts no call until a later provider adapter is explicitly enabled.
 */
@RestController
public class TwilioWebhookController {
    private final TwilioWebhookConfig config;
    private final TwilioRequestVerifier verifier;
    private final TwilioWebhookReplayGuard replayGuard;

    @Autowired
    public TwilioWebhookController(TwilioWebhookConfig config, TwilioRequestVerifier verifier) {
        this(config, verifier, new TwilioWebhookReplayGuard(config.replayWindowSeconds()));
    }

    TwilioWebhookController(TwilioWebhookConfig config, TwilioRequestVerifier verifier,
                            TwilioWebhookReplayGuard replayGuard) {
        this.config = config;
        this.verifier = verifier;
        this.replayGuard = replayGuard;
    }

    @PostMapping("/webhooks/twilio/voice")
    ResponseEntity<Void> voice(HttpServletRequest request) {
        if (!config.enabled()) return ResponseEntity.notFound().build();
        String parameters = canonicalParameters(request.getParameterMap());
        String signature = request.getHeader("X-Twilio-Signature");
        if (!verifier.verify(config.authToken(), signature, config.callbackUrl(), parameters)) {
            return ResponseEntity.status(403).build();
        }
        if (!replayGuard.firstSeen(signature + config.callbackUrl() + parameters)) {
            return ResponseEntity.status(409).build();
        }
        String caller = request.getParameter("From");
        if (caller == null || !config.allowedPhoneNumbers().contains(caller)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.status(503).header("Retry-After", "60").build();
    }

    static String canonicalParameters(Map<String, String[]> rawParameters) {
        var sorted = new TreeMap<>(rawParameters);
        StringBuilder canonical = new StringBuilder();
        sorted.forEach((name, values) -> {
            for (String value : values) canonical.append(name).append(value);
        });
        return canonical.toString();
    }
}
