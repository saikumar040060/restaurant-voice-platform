# Owner-approved conversational product direction

Confirmed in conversation on 2026-09-05. The core experience is a natural spoken conversation with a knowledgeable restaurant assistant, not a fixed ordering script.

The assistant must answer restaurant and dish questions, explain verified ingredients and preparation, discuss flavors and spice levels, describe sizes and permitted customization, and recommend dishes using available facts and customer preferences. It must handle follow-up questions, interruptions, corrections, and switching between questions and ordering without losing the draft order.

Add a tenant/location-scoped restaurant knowledge module alongside the synchronized menu. Facts need provenance, owner approval, version, effective time, and freshness controls. Structured POS data is authoritative for current prices and availability; curated approved information covers dish descriptions, preparation, FAQs, parking, and policies. Conflicting, expired, or missing facts trigger clarification or staff help. Serving estimates and dietary claims require explicit supporting data. Never infer ingredients from a dish name.

Recommendations respect stated preferences, budget, verified serving information, and availability. Price totals remain deterministic. Allergy or cross-contamination assurance always escalates; the assistant may not infer safety from an ingredient list. Restaurant-specific policy must not enable refunds or other prohibited actions.

Knowledge retrieval cannot cross tenants or reveal private customer history. Treat retrieved text and caller speech as untrusted input. The model proposes actions through the same policy gateway as ordering. Reading an answer never grants permission to act.

Evaluation additions: dish follow-ups; unsupported preparation question; preference-based recommendations; budget-grounded recommendations; mid-order FAQ then resume; interruption while speaking; changed menu after recommendation; conflicting knowledge; knowledge prompt injection; allergy escalation; cross-tenant retrieval denial.

The original 2026-09-05 sequence placed knowledge in M2, conversation scenarios in M3, and voice grounding in M7. The following amendment supersedes that sequence. Model and voice vendor selection remain open pending evaluation and approved costs.

## Platform-first amendment — 2026-09-06

Build the reusable AI voice core first, then restaurant ordering as the first production business module. Specialize it through approved tenant knowledge, configuration, prompts and typed workflows, not automatic per-tenant model training. Hotels, salons and other industries will supply their own modules and evaluations later; they are not implemented by this amendment.

Generic tenancy/configuration, voice/conversation state, knowledge governance, policy/tool gateway, durable effects, escalation, audit/privacy, cost controls and shared operations belong to the platform. Menus, dish facts, prices/modifiers, order state and POS adapters belong to the restaurant module. Preserve all original safety, identity, confirmation, escalation and approval rules.

Follow [ADR 0004](architecture/0004-platform-core-and-business-modules.md), [contracts](architecture/PLATFORM_CONTRACTS.md), and [MILESTONES.md](MILESTONES.md). M2 establishes generic businesses/contracts; M3 call state and durable actions; M4 approved knowledge/dialogue; M5 streaming voice proof; M6 restaurant specialization; M7 integrated operations/evaluation. A fictional reference module tests reuse without building hotel workflows prematurely.
