# Owner-approved conversational product direction

Confirmed in conversation on 2026-09-05. The core experience is a natural spoken conversation with a knowledgeable restaurant assistant, not a fixed ordering script.

The assistant must answer restaurant and dish questions, explain verified ingredients and preparation, discuss flavors and spice levels, describe sizes and permitted customization, and recommend dishes using available facts and customer preferences. It must handle follow-up questions, interruptions, corrections, and switching between questions and ordering without losing the draft order.

Add a tenant/location-scoped restaurant knowledge module alongside the synchronized menu. Facts need provenance, owner approval, version, effective time, and freshness controls. Structured POS data is authoritative for current prices and availability; curated approved information covers dish descriptions, preparation, FAQs, parking, and policies. Conflicting, expired, or missing facts trigger clarification or staff help. Serving estimates and dietary claims require explicit supporting data. Never infer ingredients from a dish name.

Recommendations respect stated preferences, budget, verified serving information, and availability. Price totals remain deterministic. Allergy or cross-contamination assurance always escalates; the assistant may not infer safety from an ingredient list. Restaurant-specific policy must not enable refunds or other prohibited actions.

Knowledge retrieval cannot cross tenants or reveal private customer history. Treat retrieved text and caller speech as untrusted input. The model proposes actions through the same policy gateway as ordering. Reading an answer never grants permission to act.

Evaluation additions: dish follow-ups; unsupported preparation question; preference-based recommendations; budget-grounded recommendations; mid-order FAQ then resume; interruption while speaking; changed menu after recommendation; conflicting knowledge; knowledge prompt injection; allergy escalation; cross-tenant retrieval denial.

Knowledge administration and provenance join Milestone 2; deterministic conversational scenarios join Milestone 3; real voice grounding and interruption handling join Milestone 7. Model and voice vendor selection remain open pending evaluation and approved costs.
