package com.harborvoice.platform.provider;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

/** Issues a one-use stream token only after the signed webhook has admitted the named call. */
public final class TwilioCallAdmissionService {
    private final TwilioMediaStreamAdmission streams; private final UUID businessId; private final String mediaUrl;
    public TwilioCallAdmissionService(TwilioMediaStreamAdmission streams, UUID businessId, String mediaUrl) {
        if (streams == null || businessId == null || mediaUrl == null || !mediaUrl.startsWith("wss://")) throw new IllegalArgumentException("media admission configuration required");
        this.streams = streams; this.businessId = businessId; this.mediaUrl = mediaUrl;
    }
    public String admit(String caller, String callSid, long worstCaseMinor, Instant now) {
        if (callSid == null || !callSid.matches("[A-Za-z0-9]{6,64}")) throw new IllegalArgumentException("invalid call id");
        UUID conversation = UUID.nameUUIDFromBytes((businessId + ":" + callSid).getBytes(StandardCharsets.UTF_8));
        UUID token = streams.issue(businessId, conversation, caller, worstCaseMinor, now);
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response><Connect><Stream url=\"" + mediaUrl
                + "?token=" + token + "\"/></Connect></Response>";
    }
}
