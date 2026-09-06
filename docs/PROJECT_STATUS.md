# Project status

Updated: 2026-09-06. Current work: platform-first core, deterministic voice fixtures, knowledge/dialogue contracts, and restaurant domain foundations are implemented locally; integration and real-provider gates remain open.

## Product direction

Build the reusable voice core first, prove it with a fictional reference module, then implement restaurant ordering as the first production module. Later industries supply reviewed modules and tenant-scoped approved configuration/knowledge. Per-business fine-tuning is not the default. See [review](architecture/PLATFORM_FIRST_REVIEW.md), [ADR 0004](architecture/0004-platform-core-and-business-modules.md), [contracts](architecture/PLATFORM_CONTRACTS.md), and [milestones](MILESTONES.md).

The first live pilot remains US/English-first restaurant pickup ordering. Original safety, privacy, authorization, idempotency, audit, explicit confirmation, employee escalation and human-approval requirements remain in force.

## Implementation and evidence

M1 includes hashed staff sessions, BCrypt, expiry/revocation, roles, tenant predicates/composite FKs, staff/location administration, initial-owner bootstrap, login throttling, audit viewing/denial events, correlation IDs, and transactional audit with write rollback.

Historical evidence: 28 PostgreSQL integration tests passed, plus packaged bootstrap success, repeat rejection and secret-file checks. Provider-free regression tests now pass locally. Provider-neutral voice ports, knowledge retrieval, policy/action boundaries, durable outbox state, restaurant order workflow, and local fixtures are implemented; no live voice engine, POS integration, external provider, or dashboard is connected.

Retain staff authentication, UUIDs, tenant constraints, audit behavior and existing tests. Proposed adaptations are generic business/location relationships, module boundaries, typed call/service principals and audit-port extraction. Do not rewrite V1 SQL or silently change legacy APIs.

## Milestones

| Milestone | Status |
| --- | --- |
| 0 Engineering foundation | Local evidence recorded; hosted CI acceptance unverified |
| 1 Secure tenant foundation | In progress; lifecycle, DB privileges and validation gaps remain |
| 2 Generic businesses, profiles and module contracts | Core contracts and reference module implemented; isolation evidence remains |
| 3 Call state, policy gateway and durable actions | Contracts, repositories, outbox and fixtures implemented; DB/concurrency evidence remains |
| 4 Approved knowledge and configurable dialogue | Knowledge, profiles, prompts, dialogue and context implemented; console/publication evidence remains |
| 5 Reusable streaming voice proof | M5a local fixture path implemented; M5b requires approved provider/model |
| 6 Restaurant business module | Domain, workflow, persistence and escalation foundations implemented; full menu/UI/mock POS evidence remains |
| 7 Shared operations and restaurant voice integration | Planned |
| 8 Square sandbox | Pending approved access and prerequisites |
| 9 Observability, load, recovery and security | Not started |
| 10 Staging | Not started; original approvals required |
| 11 Pilot readiness | Not started; real customer activity requires explicit approval |
| 12 Toast and future vertical readiness | Not started; extension contracts now introduced at M2 |

No milestone is complete merely because the roadmap changed. MILESTONES.md contains acceptance tests and the original-scenario coverage map.

## Repository and CI facts

Private repository: https://github.com/saikumar040060/restaurant-voice-platform. GitHub connector access is working; no repository-access approval is currently pending. This review performed read-only GitHub verification.

Published baseline commit 3a9f202562a5c0f7966c4b2c379589fd8f812895 and local baseline 01129e9 have identical tree 92387fc6be79ab9c90b90f89ef72a75e0f07428d. The original local commits were not pushed as the same history; remote publication used separate API-created commits. Preserve both histories and reconcile before ordinary Git-based collaboration. Do not force-push to conceal the difference.

Hosted CI success is unverified. The previous run-list connector filtered PR events, so an empty result did not establish whether a push run occurred. This review claims neither CI success nor absence of all runs. The reviewed architecture and first M2 implementation slice are committed locally; they have not been published.

## Next action

Use the repository policy's Terra/Medium preference for substantive implementation. Continue closing M1 lifecycle/privilege/testing gates and add focused M4/M6 evidence: authenticated text console, publication/rollback tests, complete fictional menu fixtures, and restaurant module isolation tests. Keep M5b and M8–M12 gated on approved providers, credentials, external systems, and human approval.

The first bounded M1 lifecycle slice now includes scheduled deletion of expired `auth_sessions` rows. Local compilation is blocked by an environment JDK older than the Java 21 target; the project target and source were not changed.

The required backend harness was also attempted and could not start its isolated PostgreSQL container because Docker API access is denied in this environment. No test result is being reported as passed from that run.

The first M2 code slice adds the provider-neutral `BusinessModule`, `ModuleDescriptor`, and dependency-injected `ModuleRegistry` boundary. It contains no restaurant behavior and is not yet wired to tenant persistence.

The reference module now exercises the registry with only FAQ, callback, and human-transfer intents; it deliberately has no ordering, payment, or external tool capability.

The M2 schema slice adds additive `businesses`, versioned `business_modules`, and approval-aware `business_module_bindings` tables. Existing restaurant records are not rewritten; tenant backfill and API wiring remain the next slice.

The follow-up migration deterministically backfills existing tenants as `restaurant` businesses and binds the approved reference module. This is compatibility scaffolding only; tenant-scoped API enforcement and restaurant module binding remain subject to database-backed tests.

The next M2 code slice adds `ModuleBindingService`, which resolves a module only when the authenticated business has an enabled, approved, version-matching binding. It intentionally exposes no caller-controlled database or provider access.

The platform boundary now also defines provider-neutral conversation state, turn sequencing, interruption epochs, and tenant-scoped conversation operations. This is a contract only; no telephony, speech, model, or persistence adapter is connected.

The action layer now defines validated requests, confirmation evidence, short-lived policy permits, and an execution result boundary. Model output remains non-executable, and no external action adapter exists yet.

The M2 read-only module endpoint now resolves modules through the authenticated tenant identity; callers cannot supply a different business ID. Mutable module provisioning remains an operator-only follow-up.

Migration V4 adds tenant-scoped conversation, turn, and action-request state with interruption epochs, monotonic sequence uniqueness, bounded text, explicit action status, and idempotency keys. Runtime repositories and simulator behavior remain the next implementation slice.

`JdbcConversationRepository` now provides tenant-scoped start, state lookup, turn append, and interruption operations backed by V4. Database constraints remain authoritative for duplicate sequences and cross-business writes.

`JdbcActionRepository` now persists proposed action requests and safely reuses an identical business-scoped idempotency key while rejecting key reuse with a different request hash. It does not execute actions.

Migration V5 and `JdbcPermitRepository` now persist short-lived, business-scoped policy permits and validate request hash, tool, and expiry before any adapter can execute.

Action status transitions now reject changes from terminal states (`SUCCEEDED`, `FAILED`, `UNKNOWN`) and require the expected prior state, preserving uncertain outcomes for reconciliation instead of blind retries.

The workflow slice adds a deterministic reducer contract and a fictional reference reducer for FAQ, callback, transfer, and end events. Free-form model output is not a workflow transition and no restaurant order workflow exists yet.

Migration V6 and the `KnowledgePort` contract add tenant/location-scoped knowledge with provenance, version, approval state, and freshness metadata. Retrieval implementation must still enforce approval and scope before ranking.

`JdbcKnowledgeRepository` now performs bounded approved retrieval with business/location filtering and freshness checks before matching content. Returned records retain provenance and version for grounding and audit; no vector or model provider is used.

`KnowledgeRecord` now validates stable identity, nonblank content and provenance, positive version, and approval state before a record can enter retrieval or grounding.

`KnowledgePublication` now requires a strictly newer version and rejects revoked candidates before approval, keeping publication explicit and auditable.

`BusinessScope` now provides a shared immutable business/location scope predicate for platform operations, with optional location narrowing and fail-fast business identity validation.

`CallSession` now provides validated tenant-scoped call identity, channel, conversation state, and lifecycle timestamps for binding media, consent, audit, and workflow events.

`ConversationTurn` now restricts speaker roles to `CUSTOMER`, `AGENT`, or `SYSTEM`, matching the persistence schema and preventing untrusted role injection.

`ToolCapability` now gives modules an explicit, validated tool identifier plus mutation and confirmation requirements; modules default to no tools and cannot declare arbitrary paths or endpoints.

`GroundedKnowledgeService` now packages retrieved sources with stable IDs, versions, and provenance so a later dialogue provider can cite only the approved context it received.

The local `DialogueSimulator` consumes grounded context and emits deterministic reference workflow events for questions, callbacks, transfers, and endings. It is a test fixture, not an AI or voice runtime.

The first restaurant module slice adds pure deterministic menu-item validation and minor-unit pricing with modifier checks. It uses fictional domain data and has no ordering submission, POS, payment, or customer integration.

The restaurant module is now registered through the platform `BusinessModule` contract with menu, dish, recommendation, availability, pickup-order, and transfer intents. POS and payment capabilities remain absent until their approved integration milestone.

The restaurant module now declares read-only menu/availability tools and a confirmation-required quote capability through `ToolCapability`; no arbitrary or external tool is exposed.

The restaurant `OrderPort` and fixture adapter now establish the POS boundary: only confirmed submissions cross it, and the fixture returns a clearly synthetic reference without external calls.

`MenuAvailability` now defaults unknown menu items to unavailable and allows quotes to require an explicit availability snapshot. Live inventory remains deferred to the approved POS integration.

`OrderSubmission` now requires a confirmed draft and carries a deterministic SHA-256 quote hash for later external reconciliation. It creates no external order.

`MenuRecommendations` now ranks only explicitly available items with a stable name/SKU ordering and bounded result count. Recommendations use no model-generated or hidden inventory assumptions.

Migration V9 adds tenant/location-scoped restaurant order persistence with immutable quote hash, integer total, currency, and explicit lifecycle state. It has no POS submission path.

Migration V10 adds immutable order lines for confirmed restaurant orders, preserving SKU, modifier, quantity, and unit minor-unit price for audit and reconciliation.

`JdbcRestaurantOrderRepository` now stores validated quote lines idempotently with deterministic line numbers and calculated unit prices.

`JdbcRestaurantOrderRepository` now stores confirmed submissions idempotently in the scoped order table while keeping external POS submission behind `OrderPort`.

The order repository now supports expected-state transitions scoped to the business and rejects transitions from accepted, unknown, or cancelled orders, preserving reconciliation safety.

`DishProfile` now carries structured ingredients, allergens, and dietary tags for grounded dish explanations; absent facts remain empty rather than being inferred.

`RestaurantMenuKnowledge` now groups typed menu items and dish profiles and resolves dish facts by stable SKU, returning no invented result for unknown dishes.

`OpeningHours` now evaluates open/closed status in the configured IANA timezone from explicit weekly windows; missing days and invalid windows fail closed.

`PickupDetails` now validates the structured customer name, contact, and requested pickup timestamp needed by the restaurant workflow. It does not send or store customer contact data by itself.

Restaurant quote creation now requires an uppercase ISO-style three-letter currency code, preventing malformed money metadata from reaching confirmation or persistence.

`RestaurantOrderWorkflow` now requires the deterministic quote confirmation transition before submission can begin, keeping order state separate from external adapter execution.

The privacy slice adds deterministic redaction for email, phone, and card-like sequences before log/audit payloads are emitted. It is a utility only; production retention and provider controls remain gated.

`AuditText` now applies redaction at construction time and bounds audit text size before persistence callers receive it.

The evaluation slice adds a reusable scenario contract carrying module ID, utterances, and expected outcome so restaurant and future business modules can share offline tests.

`CallbackRequest` now requires explicit consent and tenant/conversation scope before a callback can be represented. It does not enqueue, schedule, or send a callback.

`UsageBudget` now provides a synchronized per-call guard for audio duration and input size, failing closed when either budget would be exceeded.

`OrderDraft` now binds explicit confirmation to the exact immutable quote, rejecting changed totals or lines. It remains local domain logic and cannot submit an order.

The restaurant order lifecycle now distinguishes draft, confirmed, submitting, accepted, unknown, and cancelled states. Unknown submission results are terminal until reconciliation and cannot be retried through a normal transition.

The escalation slice adds tenant-scoped transfer and callback case contracts with explicit reasons and states. It performs no telephony, SMS, or employee notification.

Migration V7 and consent contracts now separate service, recording, transcript, and callback purposes with immutable evidence hashes. No recording or outbound callback is enabled.

Platform audit contracts now provide a generic append-only event path with business scope, actor, target, outcome, and correlation metadata. The existing database trigger remains the mutation control.

`JdbcConsentRepository` now persists consent decisions append-only and evaluates the latest decision for the exact business, conversation, and purpose. No consent is inferred from caller identity or a different conversation.

The media slice adds bounded provider-neutral envelopes and a synchronized sequencer that rejects duplicate/stale frames and requires explicit epoch advancement for interruptions. No telephony or audio provider is connected.

`LocalConversationSimulator` now exercises the core media-to-fixture-STT-to-workflow path entirely in memory, including interruption epochs. It is a validation fixture and does not represent a live voice service.

Migration V8 and business profile contracts add versioned, approval-aware tenant configuration for locale, timezone, and module settings. Profiles are business-scoped and no shared mutable configuration is introduced.

`JdbcBusinessProfileRepository` now resolves the newest approved profile for the authenticated business and fails closed when none exists or stored JSON is invalid.

Business profile invariant tests now cover blank locale/timezone rejection; unapproved or missing profiles remain fail-closed at the repository boundary.

The dialogue slice adds a provider-neutral `DialoguePort` and deterministic fixture that answers only from supplied grounded sources and emits structured workflow events. No language model provider is connected.

`PromptTemplate` now provides bounded tenant prompt rendering with explicit fact substitution; tenant values remain data and no external model is invoked.

`VoiceTurnService` now connects dialogue output to provider-neutral TTS while preserving the interruption epoch. The implementation is fixture-only and does not stream audio to a caller.

Provider-free unit verification now covers the grounded knowledge service, dialogue simulator, media pipeline, module registry, restaurant pricing, order confirmation/lifecycle, and escalation contracts. Database-backed integration verification still requires the disposable PostgreSQL harness.

Latest provider-free regression run passes under Java 25 targeting Java 21. The database-guarded integration suite remains unrun because Docker API access is denied.

Migration V11 adds an idempotent action outbox with explicit pending, dispatched, unknown, and reconciled states for future external adapters; it performs no dispatch itself.

`JdbcOutboxRepository` now enqueues requests idempotently and applies business-scoped expected-state transitions while leaving dispatch and reconciliation to a future approved adapter.

The outbox repository now increments dispatch attempts atomically only for pending or dispatched entries; unknown and reconciled outcomes remain protected.
The platform now exposes a bounded immutable conversation context so dialogue providers and business modules receive ordered recent turns without direct storage coupling.
Provider health is represented by a provider-neutral contract with healthy, degraded, and unavailable states; routing can use it for failover or escalation without exposing providers to business workflows.
The M4 text console now retrieves approved, fresh knowledge through the tenant-scoped knowledge service before invoking dialogue; its no-arg fixture constructor remains available for provider-free tests, and it cannot answer from unapproved or caller-supplied facts.
The restaurant module now includes a deterministic Harbor Pizza Test Kitchen fixture with required cheese, pepperoni, and build-your-own base prices for offline evaluation; it contains no real-business data or provider integration.
The same fixture now includes explicit structured dish profiles and allergen facts for cheese and pepperoni sizes; missing dietary claims remain empty rather than inferred.

`FixtureActionGateway` now validates permit request, business, tool, and expiry before returning a fixture result. It performs no external side effect and exists only for policy tests.

Provider-neutral `SpeechToTextPort` and `TextToSpeechPort` contracts now support bounded streaming transcripts, epoch-aware synthesis, and deterministic local fixtures. No speech SDK, credential, network call, or recording was added.

`LocalStreamingPipeline` now proves media sequencing, stale-frame rejection, fixture STT delivery, and interruption epoch propagation in a focused local test.

No runtime voice vendor is chosen. M5a uses mocks/local fixtures; natural voice capability requires M5b measured evaluation with an approved implementation. This task requests no credentials and authorizes no real calls or product account connections.

## Environment and continuation

Java 21 is available through Homebrew; shell Java was 17, so select Java 21 per process. Maven 3.9.12, Node 22.19.0 and Docker are installed. Previous integration tests used disposable PostgreSQL 17.6 containers. Prior turn-scoped tool permissions are not current grants.

Local execution only. GitHub storage and CI do not keep this coding task running after laptop sleep. No continuing cloud task is configured. Publish reviewed changes and configure an approved reproducible remote task before claiming laptop-independent work.

The routing guidance is committed in local 01129e9 and the remote snapshot. It does not automatically switch models or select a runtime voice model.
