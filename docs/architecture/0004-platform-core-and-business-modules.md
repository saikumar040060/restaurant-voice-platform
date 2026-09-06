# ADR 0004: Reusable voice core and business modules

Date: 2026-09-06. Status: accepted design direction per owner request; implementation pending.

This supplements ADRs 0001–0003. It supersedes their restaurant-specific placement of generic tenancy and the original M2–M7 sequence. Existing security decisions, constraints, and history remain valid. The rationale and evidence are recorded first in [the architecture review](PLATFORM_FIRST_REVIEW.md).

## Ownership and dependencies

| Boundary | Owns | Must not own |
| --- | --- | --- |
| Platform identity and tenancy | Tenant/business/location scope, staff membership, service/call principals, role checks, provisioning | Dish, room, appointment, order, or refund logic |
| Platform configuration | Versioned business profiles, module enablement, language/tone limits, opening hours, routing, consent/retention policies | Executable tenant scripts or arbitrary credentials/endpoints |
| Platform conversation and media | Call lifecycle, sequencing, interruption, compact dialogue state, deadlines, media flow, playback state | Prices or vertical workflow transitions |
| Platform knowledge | Ingestion/approval/publication, provenance, scoped retrieval, evidence references, freshness/revocation | Invented domain facts or direct customer-history access |
| Platform action and policy | Schema/permission gates, confirmation evidence, stable action IDs, outbox, replay protection, result reconciliation | Menu math, room inventory, or an all-purpose execute-API tool |
| Platform operations | Human escalation/callback cases, consent, redaction, audit, costs, quotas, shared dashboard shell, metrics | Business-specific assurances or a successful-order claim without provider evidence |
| Restaurant module | Dishes, menus, quantities, variants, modifiers, tax/price calculations, quote/read-back, order state machine, POS mapping | Telephony sessions, staff login, or bypasses around platform policy |
| Future hotel/salon/etc. modules | Their own typed intents, facts, inventory/booking workflows, calculations, tools, providers and evaluations | Imports of restaurant domain types or lower safety ceilings |

Suggested Java packages are `com.harborvoice.platform.{identity,tenancy,configuration,conversation,knowledge,actions,operations}`, `com.harborvoice.spi`, `com.harborvoice.modules.restaurant`, and `com.harborvoice.adapters.{telephony,speech,dialogue,messaging}`. Restaurant POS adapters remain under the restaurant module. These are target packages, not new implemented modules or an instruction to mass-rename working code.

Dependencies point from the composition root to platform implementations, modules, and adapters; modules and adapters use the small SPI. Core services use SPI abstractions, never restaurant classes. Modules cannot import sibling module internals. Public HTTP/JSON contracts remain separate from internal Java ports. Introduce package/dependency checks when code is added. A module runs in the same trusted process; package rules are not a sandbox for arbitrary third-party code.

## Generic identity and migration

`tenant_id` remains the authorization boundary. A tenant owns businesses, a business owns locations, and an enabled module attaches to a business/location. A business profile selects one primary workflow module initially, with shared platform capabilities. Multiple independent workflows in one call are deferred until a concrete use case defines routing and conflict rules.

Add a `businesses` table and explicit restaurant-extension mapping. Backfill one business per existing restaurant while preserving all restaurant/location/staff UUIDs. Add tenant-scoped business FKs to locations; keep the legacy restaurant relationship while compatible code transitions. Generic locations must not require a dummy restaurant. Only after backfill validation and compatibility tests may a later migration make the old restaurant reference optional. Establish one canonical business name and explicit legacy projection to prevent two independently editable sources of truth.

Existing restaurant endpoints continue to require a restaurant extension; generic business/location APIs use new contracts. Test both schemas during transition, row counts, employee assignments, missing/cross-tenant references, old API results, and compensating/restore procedures. No migration is executed in this review.

Add a tenant-scoped principal registry with EMPLOYEE, CALL, and SERVICE kinds. Existing employee UUIDs can be preserved as employee-principal IDs. Distinguish the authenticated human actor, originating call, and executing service in new events. Backfill principal references and validate replacement FKs before transitioning audit writers; retain old immutable events and UPDATE/DELETE protection. An LLM output is not a principal. Keep staff `SYSTEM`/`SUPPORT` interactive-login restrictions until separately designed flows exist. Jobs use explicitly delegated capabilities and must revalidate tenant/module state at execution.

## Configuration and isolation

A published business profile references exact module, prompt-template, knowledge publication, policy, and tool-schema versions. A call snapshots that profile for reproducibility. New calls pick up new approved publications. In-flight calls recheck revocations and live inventory/pricing before answering or executing; a revoked fact/template or disabled module cannot remain usable merely because it was pinned.

Keep platform safety instructions and reviewed module templates immutable to tenants. Tenant fields cover approved facts, language, branding/tone bounds, hours and employee destinations. Tenant configuration may narrow capabilities and escalation thresholds. It cannot enable prohibited actions, claim another tenant, choose arbitrary tool URLs, or increase its safety ceiling. Conflicting configuration fails publication or call admission.

Retrieval filters tenant/business/location/module/publication before ranking. Recheck document authorization and current approval after retrieval. Cache keys include those scope/version fields and audience; never share customer-history caches with public FAQ caches. Private records are separate capability-protected reads. Draft, expired, revoked, conflicting, and unsupported facts produce a clarification or escalation outcome. Quoted model confidence does not grant permission.

Structured operational data is authoritative for availability, amounts, and workflow state. Business owners approve descriptive facts. The restaurant module validates its ingredient/preparation/dietary fact schema, but allergy/safety questions still escalate. Core policy preserves every original prohibited category, including refunds, disputes, emergencies, identity-sensitive loyalty and account changes; modules may add escalations. Hotel or salon business rules require later review and never inherit unsafe restaurant assumptions.

## Runtime and safe effects

The media adapter validates provider events, resolves the called destination against server-controlled routing, and obtains the tenant/business/location scope. Caller ID remains an untrusted E.164 lookup hint. The session coordinates final utterances, dialogue proposals, validated knowledge, and module workflow state. Partial transcripts may support responsiveness but cannot authorize a business write.

All model-proposed reads and writes pass through typed capabilities. Only the action gateway can issue an execution permit to a narrow adapter. Internal, approved context prefetch also uses scoped knowledge access. Modules return proposals and deterministic validation results; they cannot directly send SMS, access private customer data, submit to POS, or change administrative settings.

Policy combines platform rules, reviewed module rules, tenant restrictions, principal capability, current state, and current confirmation. Every required layer must allow; denial or uncertainty wins. The same gate is rechecked before queued effects execute. Use stable action/submission IDs, atomic state-plus-outbox-plus-audit, bounded retries for proven-safe operations, and provider reconciliation for unknown outcomes. A transport timeout is not evidence of failure or permission to resubmit. Generic action uncertainty maps to restaurant `SUBMISSION_UNCERTAIN` without replacing the restaurant order state machine.

Speech output requires semantic grounding as well as tool safety. Deterministic read-backs/amounts and verified tool results produce binding promises; generative explanations are limited to authorized evidence and evaluated for unsupported claims. This does not mathematically eliminate hallucinations. If safe grounding cannot be established, clarify or escalate.

Cancellation stops obsolete speech and uncommitted proposals. It cannot cancel a committed external order. A new customer change after submission becomes a separate module decision, usually staff escalation in the first pilot. A confirmation binds to a specific fully delivered read-back and immutable action snapshot; interruptions, changed quantities/prices, or stale results invalidate it.

## Voice adapters and delivery proof

Keep telephony independent from STT, dialogue, and TTS. A streaming STT → dialogue → TTS pipeline is the initial testable architecture. A future combined speech-to-speech provider may implement the same semantic events and gateway, but may not bypass confirmation or speak unchecked transaction outcomes. If it cannot suppress/cancel unsafe output or provide required delivery signals, restrict its capabilities rather than pretend it meets the contract.

Measure end-of-utterance-to-first-audio, interruption-to-output-stop, recognition errors, grounded answer correctness, completion/transfer rates, dropped audio, queue pressure, and per-call cost. Initial engineering targets for a later approved audio implementation: p95 response start ≤1.5 seconds for a simple local FAQ, p95 interruption stop ≤300 ms, and ten isolated concurrent synthetic calls. These are proposed evaluation targets, not measured performance, a vendor promise, or a replacement for the original pilot gates. Record device/network/provider conditions and revise targets transparently if evidence requires it.

Use deadlines, bounded queues, silence handling, and duration/spend budgets from the first voice slice. If the database cannot durably create a callback, do not claim one was booked. A future provider-level, preconfigured employee fallback can handle application outages independently; it requires approved integration and testing. Keep AI disclosure, consent distinctions, recording-off defaults, minimal retention, redaction, and legal review requirements.

## Readiness and scope

M5 proves the reusable core against the fictional reference module. M6–M7 prove the restaurant extension and cross-module isolation. A later hotel/appointment module should require module code/configuration/adapters/evaluations, not modifications to the core call state machine. Its actual implementation remains deferred.

No model fine-tuning, vector database, workflow language, microservices, cloud provider, or runtime vendor is selected here. Use structured queries and PostgreSQL retrieval first where sufficient; choose additional infrastructure only against measured requirements and the original approval rules.
