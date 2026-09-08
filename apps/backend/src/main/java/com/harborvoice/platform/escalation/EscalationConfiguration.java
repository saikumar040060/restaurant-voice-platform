package com.harborvoice.platform.escalation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Local/mock queue only; a real employee-contact adapter needs separate approval. */
@Configuration
public class EscalationConfiguration {
    @Bean FixtureEscalationPort fixtureEscalationPort() { return new FixtureEscalationPort(); }
}
