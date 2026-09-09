package com.harborvoice.platform.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.util.Set;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;

class TwilioWebhookControllerTest {
    private static final String TOKEN = "sandbox-token";
    private static final String CALLER = "+15551234567";
    private static final String URL = "https://voice.example.test/webhooks/twilio/voice";

    @Test void spring_injects_call_admission_and_returns_stream_twiml() throws Exception {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.registerBean(TwilioWebhookConfig.class, this::enabledConfig);
            context.registerBean(TwilioRequestVerifier.class);
            context.registerBean(TwilioCallAdmissionService.class, this::admissionService);
            context.registerBean(TwilioWebhookController.class);
            context.refresh();

            var controller = context.getBean(TwilioWebhookController.class);
            var response = controller.voice(signedRequest());
            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody().toString()).contains("<Connect><Stream", "name=\"admission\"");
            assertThat(controller.voice(signedRequest()).getStatusCode().value()).isEqualTo(409);
        }
    }

    @Test void rejects_bad_signature_and_unknown_phone_number() throws Exception {
        var controller = enabledController();
        var badSignature = requestFor(CALLER, "not-valid");
        assertThat(controller.voice(badSignature).getStatusCode().value()).isEqualTo(403);

        String unknown = "+15557654321";
        var unknownCaller = requestFor(unknown, signature("CallSidCAfixtureFrom" + unknown));
        assertThat(controller.voice(unknownCaller).getStatusCode().value()).isEqualTo(403);
    }

    @Test void disabled_webhook_is_not_discoverable_and_enabled_config_requires_safety_values() {
        var controller = new TwilioWebhookController(TwilioWebhookConfig.disabled(), new TwilioRequestVerifier());
        assertThat(controller.voice(new MockHttpServletRequest()).getStatusCode().value()).isEqualTo(404);
        assertThatThrownBy(() -> new TwilioWebhookConfig(true, "", URI.create("https://voice.example.test"), Set.of(CALLER), 300))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private TwilioWebhookController enabledController() {
        return new TwilioWebhookController(enabledConfig(),
                new TwilioRequestVerifier());
    }

    private TwilioWebhookConfig enabledConfig() {
        return new TwilioWebhookConfig(true, TOKEN, URI.create("https://voice.example.test"), Set.of(CALLER), 300);
    }

    private TwilioCallAdmissionService admissionService() {
        var safety = new ProviderSafetyConfig(true, Set.of(CALLER), 1, 60, 100);
        var streams = new TwilioMediaStreamAdmission(new SandboxSpendGate(new CallAdmissionController(safety)));
        return new TwilioCallAdmissionService(streams, UUID.fromString("c4f3a781-5601-3694-9255-b6a1351dcbb6"),
                "wss://voice.example.test/webhooks/twilio/media");
    }

    private MockHttpServletRequest signedRequest() throws Exception {
        var request = requestFor(CALLER, "");
        request.addHeader("X-Twilio-Signature", signature(TwilioWebhookController.canonicalParameters(request.getParameterMap())));
        return request;
    }

    private MockHttpServletRequest requestFor(String caller, String signature) {
        var request = new MockHttpServletRequest("POST", "/webhooks/twilio/voice");
        request.setParameter("CallSid", "CAfixture");
        request.setParameter("From", caller);
        if (!signature.isEmpty()) request.addHeader("X-Twilio-Signature", signature);
        return request;
    }

    private String signature(String canonicalParameters) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(TOKEN.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA1"));
        return java.util.Base64.getEncoder().encodeToString(mac.doFinal((URL + canonicalParameters)
                .getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }
}
