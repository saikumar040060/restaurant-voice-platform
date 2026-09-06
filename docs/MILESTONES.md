# Platform-first implementation plan

Revision: 2026-09-06. Authority: owner-approved direction, [architecture review](architecture/PLATFORM_FIRST_REVIEW.md), and [ADR 0004](architecture/0004-platform-core-and-business-modules.md). This replaces the original M2–M7 sequence; all original safety, quality and approval gates remain.

## Prerequisite: verify and finish the foundation

M0/M1 retain their existing work and outstanding gates. Verify hosted CI; a PR-only run query cannot establish push-run status. Reconcile local/remote histories without force-push or loss. Finish documented session lifecycle, runtime/migration privileges, appropriate static/secret/dependency checks and remaining role/isolation evidence. Do not mark M1 complete simply because this roadmap changes.

The next focused implementation task is bounded M1 verification/lifecycle work using the repository's Terra/Medium preference. Then begin M2. Do not begin with the restaurant menu UI or provider selection.

## M2 — Generic businesses, profiles and module contracts

Implement generic business/location ownership with additive compatibility migrations, preserving restaurant APIs and IDs. Introduce typed employee/call/service principals and an independent audit port. Define the compiled module registry, profile/schema versions, capability declarations, approval/publication and compatibility checks. Add a minimal fictional reference module declaring FAQ/callback intents without order or booking entities. Additional tenant provisioning must be an operator-authorized service separate from initial bootstrap; tenant owners cannot create/access another tenant.

Acceptance:

- Existing M1 tests pass unchanged in intent; legacy restaurant APIs never expose non-restaurant locations.
- A second fictional business and tenant can exist without dummy restaurants; FK, principal and staff isolation is enforced.
- Fresh install and V1 upgrade tests preserve UUIDs, assignments and immutable audit history, with documented recovery/compensating procedures.
- Unknown/incompatible modules, unapproved profiles, cross-tenant references and tenant-defined tools fail closed.
- Dependency checks reject core imports of restaurant types and module imports of sibling internals.

No external account is needed. This establishes scope and contracts, not a conversational model.

## M3 — Call state, policy gateway and durable actions

Implement call admission through verified local transport fixtures, isolated state/sequencing, caller-ID hints, consent, typed workflow execution and schema/permission/policy/confirmation gates. Implement stable action IDs, transactional outbox/audit, worker replay protection, bounded retries, reconciliation and dead-letter visibility. Reference transfer/callback tools use mocks. Caller capabilities must not inherit staff authority.

Acceptance:

- Ten deterministic concurrent calls do not share state; duplicated, out-of-order or partial inputs cannot authorize writes.
- Prohibited/uncertain cases escalate; callback success is acknowledged only after durable creation.
- Workers reject wrong-tenant/replayed jobs and recheck current permissions, module enablement and kill switches.
- Confirmation binds to one action snapshot. Crash/restart/concurrent tests yield at most one mock side effect when supported by its idempotency contract.
- Unknown outcomes stay uncertain; a DB outage cannot falsely confirm success.

Read tools also require authorization. No side effect leaves local mocks.

## M4 — Approved knowledge and configurable dialogue

Implement typed facts, provenance, approval/publication, effective/expiry/revocation rules, scoped retrieval, approved prompt assembly, and evidence-aware responses. Use deterministic dialogue fixtures through a provider port first. Support follow-ups, corrections, topic switching and compact state. Add a minimal authenticated text console and profile/knowledge review screen, not the full dashboard.

Acceptance:

- Two fictional tenants answer only from their own authorized evidence; draft/private/foreign facts are excluded.
- Missing, conflicting, expired or revoked facts produce clarification/escalation.
- Caller/document prompt injection cannot change scope, tools, policy or approvals.
- A mid-workflow question resumes the same workflow revision; summaries cannot authorize actions or become approved facts.
- Publication/rollback is versioned and audited; revocation invalidates in-flight use and caches.

No paid model is required for fixture tests. Scripted output does not establish natural-language quality; real dialogue quality belongs to M5's explicit evaluation gate.

## M5 — Reusable streaming voice proof

Implement telephony/STT/dialogue/TTS ports, local audio transport, interruption/cancellation epochs, bounded buffers, playback acknowledgement, deadlines/backpressure, silence handling, consent announcements and cost/duration budgets. Add a local voice test console for fictional inputs and the reference-module path. Document fixture origins.

- **M5a, no external account:** verify codec/event contracts, cancellation/reconnection, old-epoch rejection, synthetic or prerecorded fixtures, transfer/callback failure paths and concurrency. The reference module runs through shared call/gateway code without restaurant imports.
- **M5b, real conversational proof:** after explicit approval of a suitable local model installation or specific sandbox services, measure actual STT, reasoning and TTS against the agreed dataset and ADR 0004 targets. Save latency, correctness, interruption and failure evidence with environment/provider details.

M5a alone must not be called a fully functioning natural voice agent. Without approved access, M5b stays incomplete while independent restaurant-domain work may progress. This plan does not authorize a model download, credentials, account connections, microphone capture, real calls or recording. Those retain the original approval rules.

## M6 — Restaurant business module

Implement restaurant knowledge schemas and full Harbor Pizza Test Kitchen menu, variants/modifiers, quantities, hours, availability, taxes/rounding, special-instruction constraints, recommendations from verified facts and every original escalation category. Add the original order state machine, deterministic quoting/read-back and snapshot-bound confirmation. Implement restaurant-owned POS contracts with Mock adapter; mock checkout/SMS requests use the shared gateway. Add dish/menu management UI and order views to the shared shell.

Acceptance:

- Required modifiers, price/rounding, size/quantity changes and availability/price drift pass domain tests.
- Complete delivered read-back and explicit final confirmation are mandatory; interrupted, rejected, stale and duplicate confirmations cannot submit.
- Restaurant tools remain within module/tenant capabilities; reference-module calls cannot access them.
- Restaurant-specific entities do not enter the core interfaces. Any necessary core revision receives a focused ADR and regression coverage.
- Applicable original scenarios pass against explicitly declared mocks. No live Square acceptance is claimed.

The seed spec omits Build Your Own base prices, concrete dressing/flavor choices and some thresholds. Record proposed fictional defaults during implementation; do not invent facts for real businesses. Live onboarding requires approved values.

## M7 — Shared operations and restaurant voice integration

Complete the role-aware dashboard for calls, workflows/orders, knowledge/configuration, employee routing/hours, transfer/callback queues, redacted transcripts, health/audit and metrics. Add live-update reauthorization/reconnection. Integrate restaurant dialogue and voice with the core from M5 and mock POS until M8. Keep the reference module in the same regression suite.

Acceptance:

- All 30 original scenarios and dish-knowledge addendum cases pass at the appropriate layers, including relevant voice interruption variants.
- Measure at least 98% line-item/modifier accuracy on the approved restaurant evaluation set, with a defined denominator, separate transfer rate, and uncertainty escalated. Scripted tests cannot satisfy real-voice accuracy.
- Dashboard role/accessibility, tenant-scoped SSE reconnect, uncertainty warnings, masking and handoff tests pass.
- Demonstrate five concurrent calls per location and ten mixed reference/restaurant calls without leaks; report actual mocks/providers and cost evidence.
- M5b evidence is required for integrated voice readiness. Long soak, restoration, security hardening and availability evidence remain M9 gates.

No public deployment or real restaurant activity is authorized. Missing approved voice access leaves the corresponding gate incomplete.

## Original scope mapped forward

| Original milestone | Revised allocation |
| --- | --- |
| M2 menu and business configuration | Generic scope/configuration M2/M4; restaurant menu/math/UI M6 |
| M3 calls and simulator | Call/action core M3; dialogue M4; media M5 |
| M4 order state and gateway | Shared gateway/confirmation M3; restaurant reducer/read-back M6 |
| M5 exactly-once external-effect framework | Generic durable effects M3; provider-dependent restaurant proofs M6/M7 |
| M6 dashboard | Small consoles M4/M5; restaurant views M6; full operations M7 |
| M7 voice | M5a mocks/media, M5b approved real evaluation, M7 restaurant integration |

| Original scenario IDs | First implementation and verification |
| --- | --- |
| 1, 5–11, 28–29: construction/confirmation/stale facts | M6, with voice variants M7 |
| 2–4, 21–22: identity/privacy/tenant scope | M3/M4; all implemented channels rechecked M7 |
| 12–14, 26–27: duplicate/restart/unknown effects and DB/SMS failure | M3 generic; M6/M7 restaurant |
| 15–19: prohibited intents, transfer/callback | M3 reference; M6/M7 restaurant categories |
| 20, 30: injection and redaction | M3/M4; media/provider behavior M5/M7 |
| 23–25: concurrency, backpressure, AI failure | M3 deterministic; M5 media; M7 integrated; M9 soak |

M8 remains approved Square sandbox integration: OAuth, Catalog, Orders, hosted checkout, webhooks, revocation and reconciliation. M9 retains observability/load/soak/recovery/security scans, SBOM and restoration. M10 retains staging approval, resources and public-exposure gates. M11 retains onboarding/legal/consent review and explicit real-customer go/no-go. M12 retains Toast mock/contract readiness and future-vertical access requirements; generic extension interfaces now arrive at M2 rather than M12.

Hotel/salon workflow implementations, delivery, loyalty/gift cards, refunds, spoken-card processing and medical/allergy assurances remain outside the first pilot. The 99.9% owned-path availability target remains a target only; monitoring evidence and separately reported provider outages are required before claiming it.

## Implementation handoff

Verify CI/history and close the recorded M1 gaps. Then implement M2 in focused slices: generic business migration and compatibility, typed principals/audit port, registry/profile contracts, reference configuration and isolation tests. Follow M3–M7 with evidence reports and explicit mock-versus-real distinctions. Use Terra/Medium for substantive implementation and escalate only for a concrete unresolved architectural/security question. This plan does not switch models or start a new task.
