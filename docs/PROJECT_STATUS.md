# Project status

Updated: 2026-09-08. Current work: platform-first core, deterministic voice fixtures, knowledge/dialogue contracts, restaurant domain foundations, and a sandbox Railway deployment are implemented; the approved sandbox secrets are stored only in Railway, while live-provider and real-call gates remain disabled.

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
| 1 Secure tenant foundation | Local integration and packaged bootstrap verified; hosted CI/history reconciliation remains |
| 2 Generic businesses, profiles and module contracts | Core contracts, reference module, and boundary validation implemented |
| 3 Call state, policy gateway and durable actions | Contracts, repositories, outbox, replay safety, and 112-test integration evidence implemented |
| 4 Approved knowledge and configurable dialogue | Scoped retrieval, prompts, dialogue, console, publication controls, and conflict handling implemented |
| 5 Reusable streaming voice proof | M5a local fixture path implemented; M5b sandbox safeguards and deployment are ready. Sandbox credentials are securely stored, while provider connection and measured evaluation remain gated |
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

Continue provider-free M6/M7 evidence with authenticated tenant-scoped restaurant views and synthetic integration tests. The OpenAI sandbox adapter evaluation may proceed only through the approved Railway environment; Twilio ingress and all real calls remain disabled. Full database-harness verification still needs Docker socket access, and hosted publication awaits GitHub DNS recovery.

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

Latest provider-free regression run passes under Java 25 targeting Java 21. The database-guarded integration suite has now also passed locally against disposable PostgreSQL 17.6.
Earlier completion audit: `scripts/validate_foundation.py` and the provider-free Maven suite passed, while the first `scripts/test_backend.py` attempt was blocked by temporary Docker API access denial.
Integration verification later completed successfully: `scripts/test_backend.py` ran 100 tests with zero failures or errors, packaged bootstrap/repeat/insecure-secret checks passed, and Maven build/repackage succeeded against disposable PostgreSQL 17.6.
The backend harness now selects the installed Java 25 runtime when the shell is still on Java 17, preventing Surefire class-version mismatches during verification.
The harness now always uses the installed Java 25 runtime when available, regardless of the caller's `JAVA_HOME` path naming, so Java 17 cannot launch Java 25-compiled tests.
Integration audit found and fixed a V4 PostgreSQL defect: conversation child tables referenced `(conversation_id, business_id)` without a matching unique key on `conversations`; V4 now declares `UNIQUE (id, business_id)` and includes a migration schema regression test.
The next integration audit found the corresponding V5 action-permit defect; V4 now also declares `UNIQUE (id, business_id)` on `action_requests`, satisfying the composite foreign key used by `action_permits`.
The provider-neutral health registry now records and replaces health signals by provider in a thread-safe in-memory fixture, without making provider calls.

Migration V11 adds an idempotent action outbox with explicit pending, dispatched, unknown, and reconciled states for future external adapters; it performs no dispatch itself.

`JdbcOutboxRepository` now enqueues requests idempotently and applies business-scoped expected-state transitions while leaving dispatch and reconciliation to a future approved adapter.

The outbox repository now increments dispatch attempts atomically only for pending or dispatched entries; unknown and reconciled outcomes remain protected.
Outbox attempt increments now stop at five tries, enforcing a bounded retry ceiling before dead-letter/reconciliation handling is added.
Migration V12 and the outbox repository now expose a `DEAD_LETTER` terminal state once five attempts are exhausted; unknown external outcomes remain separate and reconcilable.
`OutboxStatus.terminal()` now encodes that reconciled and dead-letter entries cannot be retried, while unknown entries remain eligible for explicit reconciliation.
`OutboxStatus.retryable()` now identifies only pending and dispatched entries for worker retry decisions; unknown outcomes remain outside automatic retry.
Restaurant availability now has an immutable named snapshot contract, allowing deterministic flows to retain the exact revision used for a quote while unknown SKUs fail closed.
The availability helper accepts immutable snapshots directly alongside the legacy map form, preserving compatibility while enabling revision-bound callers.
Availability snapshot revisions now use bounded identifier syntax, preventing malformed metadata from entering quote or audit state.
Recommendation ranking now accepts the same immutable availability snapshot used by ordering, keeping suggestions bound to a specific inventory revision.
Availability snapshots now cap entries at 10,000, bounding tenant-controlled in-memory state before recommendation or quote evaluation.
Provider health reasons are now trimmed and capped at 500 characters before routing or audit consumers receive them.
Provider health now exposes a staleness check with caller-supplied age bounds, allowing routing to fail closed on old signals.
Grounded knowledge sources now validate stable identity, bounded content, provenance, and positive version before dialogue consumption.
Action requests now cap arguments at 100 validated keys, bounding model-proposed payloads before policy authorization.
Confirmation evidence now requires a canonical 64-character hexadecimal utterance hash, preventing malformed evidence from authorizing actions.
Special-instruction filtering now matches sensitive terms as whole words, avoiding false positives on harmless words such as “cardboard.”
Prompt rendering now enforces a 16,000-character post-substitution ceiling, preventing tenant facts from expanding model-facing context without bound.
Prompt fact maps now enforce bounded count and identifier syntax before rendering, keeping tenant placeholders deterministic.
M2 now includes a validated business-scoped `ServicePrincipal` contract for internal workers, distinct from employee actors and unsuitable for staff-authorized requests.
Callback request destinations are now trimmed and capped at 320 characters before any future queueing, while explicit consent and exact business/conversation scope remain required.
Escalation cases now validate identity, tenant/conversation scope, reason, state, and creation time before transfer or callback handling.
Escalation cases now expose forward-only transition rules; resolved or declined cases cannot reopen or move backward.
Restaurant orders now have a bounded special-instructions value object that trims ordinary notes and rejects payment or credential-like content before workflow use.
The platform now exposes a bounded immutable conversation context so dialogue providers and business modules receive ordered recent turns without direct storage coupling.
Conversation contexts now verify their declared character count and enforce the 16,000-character ceiling even when constructed directly.
Conversation context sizing now uses a long accumulator before validation, preventing integer wraparound from bypassing the limit.
Provider health is represented by a provider-neutral contract with healthy, degraded, and unavailable states; routing can use it for failover or escalation without exposing providers to business workflows.
The M4 text console now retrieves approved, fresh knowledge through the tenant-scoped knowledge service before invoking dialogue; its no-arg fixture constructor remains available for provider-free tests, and it cannot answer from unapproved or caller-supplied facts.
The restaurant module now includes a deterministic Harbor Pizza Test Kitchen fixture with required cheese, pepperoni, and build-your-own base prices for offline evaluation; it contains no real-business data or provider integration.
The same fixture now includes explicit structured dish profiles and allergen facts for cheese and pepperoni sizes; missing dietary claims remain empty rather than inferred.
Restaurant menu knowledge now rejects duplicate item or dish SKUs at construction time, preventing ambiguous identity from entering availability, pricing, or confirmation flows.
`VoiceTurnService` now rejects blank transcripts, missing grounded context, and negative interruption epochs before invoking dialogue or TTS.
The compiled module registry now rejects null module lookups explicitly, keeping unknown or malformed module identities fail-closed before resolution.
Module descriptors now require numeric semantic versions and cap display names at 160 characters before registry publication.
The module registry now validates declared intent syntax and non-null tool sets at construction, rejecting malformed contracts before tenant routing.
Added `scripts/validate_module_boundaries.py` to enforce that platform core does not import restaurant types and restaurant code does not import sibling business modules.
M3 now includes a bounded tenant-scoped `CallerPrincipal` contract; caller hints carry no employee role or staff authority.
Policy permits now require a nonblank tool, canonical 64-character hexadecimal request hash, and non-null expiry before action execution.
M3 now includes a replay-safe provider-free action executor that validates request/business scope and atomically invokes a gateway at most once per request ID.
The replay-safe executor unit/concurrency test passes; a subsequent full harness attempt is currently blocked by Docker API permission denial at `unix:///Users/saikumar/.docker/run/docker.sock`.
Latest full harness evidence: 105 tests passed with zero failures/errors, including the application-context suite and migration schema test; packaged bootstrap checks and Spring Boot repackage also succeeded. The Docker permission issue was transient and is no longer present for this run.
Replay verification now explicitly preserves an `UNKNOWN` gateway outcome across repeated execution attempts without invoking the gateway again.
M3 now includes an action-dispatch worker guard that rejects terminal outbox states and wrong-business jobs before invoking the replay-safe executor.
M3 now centralizes tool authorization for read and mutating capabilities; tool identity must match the request and confirmation-required tools need explicit evidence.
M3 now includes a fixture escalation port that stores a case before returning success and enforces business-scoped forward-only transitions; it performs no transfer, SMS, or employee notification.
The post-escalation full harness attempt stopped before startup because Docker again denied access to `unix:///Users/saikumar/.docker/run/docker.sock`; no integration result is inferred from that attempt.
M3 acceptance rerun after the worker and tool-authorization slices was attempted twice; both attempts stopped before container startup on the same Docker socket permission error, while all provider-free M3 tests remain green.
Resumed-goal harness evidence 2026-09-06: 110 tests passed with zero failures/errors, packaged bootstrap checks passed, and Maven package/repackage succeeded against disposable PostgreSQL.
Restaurant recommendations now collapse duplicate SKUs before deterministic sorting and limiting, avoiding repeated or ambiguous suggestions.
Restaurant quote pricing now bounds an order to 100 lines before arithmetic, preventing unbounded input from reaching confirmation or persistence.

`FixtureActionGateway` now validates permit request, business, tool, and expiry before returning a fixture result. It performs no external side effect and exists only for policy tests.

Provider-neutral `SpeechToTextPort` and `TextToSpeechPort` contracts now support bounded streaming transcripts, epoch-aware synthesis, and deterministic local fixtures. No speech SDK, credential, network call, or recording was added.

`LocalStreamingPipeline` now proves media sequencing, stale-frame rejection, fixture STT delivery, and interruption epoch propagation in a focused local test.

No runtime voice vendor is chosen. M5a uses mocks/local fixtures; natural voice capability requires M5b measured evaluation with an approved implementation. This task requests no credentials and authorizes no real calls or product account connections.

## Environment and continuation

Java 21 is available through Homebrew; shell Java was 17, so select Java 21 per process. Maven 3.9.12, Node 22.19.0 and Docker are installed. Previous integration tests used disposable PostgreSQL 17.6 containers. Prior turn-scoped tool permissions are not current grants.

The sandbox backend is deployed to Railway with managed PostgreSQL and a public liveness endpoint. Railway verifies `/healthz` before completing deployments; the endpoint returns only `{"status":"ok"}` and exposes no tenant, order, provider, or credential data. This establishes an HTTPS callback base for later sandbox integration, not a live voice service. Sandbox Twilio and OpenAI credentials are stored only as masked Railway variables; no provider connection, calls, or customer activity is enabled.

The routing guidance is committed in local 01129e9 and the remote snapshot. It does not automatically switch models or select a runtime voice model.

M4 publication controls now include explicit revocation and same-tenant rollback contracts. Rollback only restores an older non-revoked record for the same canonical key and rejects cross-tenant or forward-version substitutions; focused tests and module-boundary/foundation validation pass.

Credential-free M5b preparation now includes bounded sandbox provider configuration, Twilio HMAC request verification, and synthetic evaluation/account-owner checklist documentation. No provider credentials, SDK, network call, or real call path has been added.

Grounded dialogue now detects conflicting approved sources sharing a canonical key and returns a clarification response instead of selecting one arbitrarily; focused provider-free tests pass.

A post-M4 full harness retry on 2026-09-06 stopped before PostgreSQL startup because Docker denied access to unix:///Users/saikumar/.docker/run/docker.sock. Unit tests, foundation validation, and module-boundary validation remain green; no database result is inferred from this retry.

User-provided full harness evidence 2026-09-06 19:23:18: Maven verify completed with 112 tests, zero failures/errors/skips, packaged jar/repackage succeeded, and bootstrap initialization/repeat-secret/insecure-secret checks passed.

M5b preparation now includes a provider-neutral call admission controller enforcing test-number allowlisting, concurrent-call limits, and owner-approved spend ceilings before adapter execution; focused tests and repository validators pass.

Spend reservation now releases a previously admitted sandbox call slot when the requested worst-case amount exceeds the fixed budget. Tests also prove that an unlisted caller or a negative reservation neither consumes a slot nor charges the budget.

M5b preparation now includes a provider-neutral circuit breaker that opens after bounded failures and resets only after an explicit successful probe; focused tests and validators pass.

Credential-free M5b provider invocation now has a bounded timeout wrapper coupled to the circuit breaker; synthetic tests prove successful calls, timeout failure accounting, and open-circuit suppression.

The public Twilio voice-webhook boundary is now disabled by default. Enabling it requires an HTTPS callback base URL, injected token, bounded replay window, and test-number allowlist; invalid signatures, replayed deliveries, and unlisted callers are rejected. A valid sandbox request still returns `503` because no provider adapter or live call path is enabled.

Owner-provided OpenAI and Twilio sandbox secrets, together with a single test-caller allowlist, are stored only as masked Railway service variables. They are not committed, logged, or read by this task. Realtime and Twilio ingress remain disabled pending a measured adapter evaluation and an explicit pre-connection decision.

Restaurant order submission hashes now use a versioned canonical serialization of SKU, modifier, quantity, unit price, currency, and total rather than Java object text; a regression test proves identical quotes retain a stable hash and changed lines invalidate it.

The Harbor Pizza Test Kitchen fixture now covers the complete fictional seed menu: pizza sizes, build-your-own pizza with a required size and optional named toppings, garlic bread, house salads with required dressing, and fountain drinks with required flavor. Modifier selections are bounded, grouped, priced in integer minor units, and canonicalized so equivalent selection ordering cannot alter a confirmation hash. The fictional dressing and drink choices are ranch, Italian, vinaigrette, cola, lemon-lime, and root beer; they are offline defaults only and do not assert facts about any real business.

The restaurant-owned mock POS now deduplicates a repeated submission by its order ID and canonical quote hash, returning the original fixture result without a second external-effect claim. It remains a local test adapter and does not contact Square or any other provider.

Restaurant ordering policies now provide tenant-approved currency, tax basis points, line-quantity caps, large-order escalation threshold, and special-instruction length. Deterministic totals round tax half-up in integer minor units; the fictional Harbor fixture uses 6% tax, a 20-item line cap, and a $100 transfer threshold. These are fixture defaults, not production restaurant settings.

Restaurant drafts can now bind to an immutable availability revision. Final confirmation validates the same revision and each line's availability, so a sold-out item or any availability revision change requires a new quote and read-back before submission.

The local realtime evaluation gate now selects the lowest estimated-cost measured candidate only when it passes every approved accuracy, latency, interruption, reliability, and zero-safety-violation threshold. It is a pure local decision component with synthetic tests; it neither selects an OpenAI model nor connects a provider.

Latest local verification on 2026-09-08: the focused provider, restaurant, and evaluation suites plus foundation and module-boundary validators passed. The disposable PostgreSQL harness was retried but the current process was denied access to Docker's socket, so no new database-harness result is claimed. All resulting commits through `914fc5c` are pushed to the private GitHub branch.

The restaurant evaluation catalog now preserves all 30 required fictional safety and ordering scenarios as named, immutable evaluation inputs. It supplies the denominator for later model/provider evidence and cannot be reduced by selecting only easy prompts.

The local mock-POS concurrency regression now submits the same confirmed order ten times in parallel and verifies every caller receives the one idempotent fixture result. It covers the duplicate-confirmation/exactly-once path without introducing a real POS effect.

The provider-neutral realtime session boundary now has a bounded in-memory fixture that emits output only for completed synthetic frames, clears queued audio on interruption, rejects old epochs, and closes without network activity. It is used only for local contract testing; the disabled runtime implementation remains the production default.

The shared operations layer now exposes a stable, credential-free snapshot of provider health and remaining audio/input budgets. It is a read model for future authorized dashboard work and deliberately excludes provider configuration, keys, caller data, transcripts, and order content.

The owner authorized a controlled OpenAI sandbox connection on 2026-09-08. The Railway sandbox now has `gpt-realtime-2.1-mini` configured as the initial low-cost evaluation candidate; its key remains masked and unread. A credential-safe Railway shell model-access probe was initiated without printing the key. Its result is not yet claimed because the console does not expose command output through the automation surface. Twilio ingress and real calls remain disabled.

Restaurant confirmation now has explicit delivered read-back evidence bound to a quote hash and playback epoch. An undelivered response, interrupted epoch, or changed quote cannot confirm the order. Focused read-back, hash, and workflow tests plus repository validators pass; the pending local commit awaits temporary GitHub DNS recovery.

The restaurant workflow now fails closed if a caller uses the legacy confirmation shortcut. A confirmation requires delivered read-back evidence bound to the exact quote and playback epoch; focused workflow regression tests and repository validators pass.

Restaurant escalation classification now covers refunds/voids, payment disputes, complaints, food-safety/injury, threats/harassment, emergencies, allergy/cross-contamination, manager requests, uncertain orders, provider failure, and employee unavailability. Every category has an explicit transfer or callback route; the policy creates no contact and requires the platform's tenant-scoped durable escalation case for execution. Focused tests and repository validators pass.


Latest focused restaurant-module verification on 2026-09-08: all 36 restaurant domain, policy, fixture, workflow, idempotency, evaluation-catalog, and escalation tests passed with zero failures, errors, or skips. An initial package-filter command matched no tests and was immediately corrected; it was not a product test failure.

A generic module-capability resolver now rejects a tool that the active compiled module did not declare. Regression coverage proves the reference module cannot obtain `restaurant.quote`, while dependency validation confirms the platform core does not import restaurant types. Focused capability, restaurant, and action-authorization tests pass.

The first restaurant dashboard API slice is now available at authenticated `GET /api/v1/restaurant/menu`. It resolves the `restaurant` module through the caller's approved tenant binding before returning the fictional menu fixture. The new generic `ApprovedModuleResolver` keeps the restaurant module independent of JDBC implementation details; focused service, capability, and boundary checks pass.

The authenticated restaurant surface now also exposes `GET /api/v1/restaurant/orders` with a bounded tenant-scoped order-summary projection. It returns only order ID, location, amount, currency, state, and creation time; it does not return customer details, payment information, transcripts, or provider data. The JDBC query is business-filtered, and focused controller/service tests plus module-boundary validation pass.

The fictional Harbor Pizza Test Kitchen fixture now publishes the seed specification hours (11:00–22:00 America/Detroit every day) through the authenticated menu view. Focused menu/hour tests and repository validators pass.

Provider-free M7 concurrency evidence now includes a mixed-module fixture harness. Ten simultaneous synthetic calls (five reference and five restaurant) process isolated media/workflow state, while cross-business or wrong-module state reads fail closed. This is local fixture evidence only; it does not claim real-provider concurrency or voice quality.

The M5b adapter boundary now includes a synthetic `OpenAiRealtimeSession` implementation behind the provider-neutral realtime port. It serializes input-audio/commit/response events, bounds output, clears output on interruption, and drops malformed provider frames before playback. Its transport is injected and no concrete network transport, credential read, socket, or OpenAI request has been added; focused synthetic tests and repository validators pass.

The OpenAI sandbox preparation now includes an explicit WebSocket transport that builds the documented Realtime endpoint and authenticates only inside its opt-in `connect` method. Construction and test execution do not create a socket. Fragmented provider text events are reassembled before they reach the session adapter, and focused transport/session tests pass. A real connection remains unattempted until the current commits can be deployed and the authorized sandbox evaluation path is deliberately invoked.

Sandbox provider admission now issues an opaque, expiring call lease. A lease cannot remain active at or beyond the configured duration and releases its concurrency slot exactly once on expiry or cleanup; spend reservation still occurs before session start. Focused allowlist, concurrency, spend, duration, and idempotent-release tests plus repository validators pass.

The realtime port now has a provider-neutral admitted-session wrapper. Every audio input/output operation rechecks the sandbox lease; expiration closes the delegate, rejects further audio, and idempotently releases the concurrency slot. Focused lease/session/fixture tests and repository validators pass.

A fail-closed realtime-session factory now reserves the approved caller's spend, concurrency, and duration lease before it attempts transport setup. It releases the lease if setup fails and returns only an admitted, lease-enforced session. All coverage uses synthetic transports; no OpenAI socket or secret read occurred.

Realtime transport setup now runs through the existing bounded timeout and circuit-breaker wrapper. The shared wrapper now unwraps runtime provider failures correctly while still recording them for breaker state. Synthetic connection/admission, timeout, and breaker regression tests pass; no real transport was opened.

The shared operations layer now has authenticated `GET /api/v1/operations/snapshot` access for owner and manager roles only. It exposes the existing redaction-safe health/budget projection and denies employee or unauthenticated access; focused operations tests and repository validators pass.

Operations snapshots now remove provider-health diagnostic reasons before serialization, preventing internal or customer-derived diagnostic text from reaching the dashboard API. Focused operations tests and repository validators pass.

A dependency-free same-origin dashboard shell now lives at `/dashboard/index.html`. It supports authenticated menu, order-summary, and authorized operations views; it stores the opaque session token only in browser session storage, uses no third-party assets, and avoids unsafe HTML insertion. The restrictive CSP now permits only same-origin scripts, styles, and API connections. Focused dashboard, restaurant, and operations tests plus repository validators pass.

The dashboard now reauthorizes the current session on focus and every minute. A `401` clears the session-scoped token, hides tenant content, and returns the user to sign-in rather than displaying stale data. Focused dashboard and console tests plus repository validators pass.

A one-shot OpenAI connectivity probe is now implemented but disabled by default. It requires both realtime enablement and a separate probe flag, opens and closes an admitted sandbox socket without sending audio, prompts, tools, orders, or customer data, and is bounded by the caller allowlist, $1 fixture cap, 60-second duration cap, timeout, and circuit breaker. Local package and repository validation pass; the probe has not been enabled or run.

OpenAI Realtime sandbox connectivity was verified on 2026-09-08 through the approved Railway environment: the application started successfully while the one-shot probe opened and closed an authenticated Realtime WebSocket. The probe sends no audio, text prompt, tool call, order, or customer data. After the pass, `VOICE_OPENAI_CONNECTIVITY_PROBE_ENABLED` and `VOICE_OPENAI_REALTIME_ENABLED` were both restored to `false`, and the restored deployment passed Railway health checks. Twilio remains disabled; no telephone call, message, recording, order, payment, or real customer activity occurred.

M6 menu/dashboard slice: Harbor Pizza now exposes fictional optional pizza toppings with deterministic minor-unit prices, and the authenticated dashboard shows available modifier choices alongside required choices and its fictional sandbox-hours label. Focused restaurant/dashboard tests (13 tests) plus foundation and module-boundary validation passed on 2026-09-08. The required disposable-PostgreSQL harness was attempted but could not start because this execution environment was denied access to `/Users/saikumar/.docker/run/docker.sock`; no full-harness pass is claimed for this slice.

M6 mock-POS workflow slice: `RestaurantOrderWorkflow.submit` now crosses the module-owned `OrderPort` only after deterministic read-back-backed confirmation. It accepts only `ACCEPTED` or `UNKNOWN`; a null/unprovable adapter response remains `UNKNOWN` for reconciliation and cannot be retried blindly. Focused workflow/mock-POS tests (6 tests) and module-boundary validation passed.

M6 verification update: the owner ran `python3 scripts/test_backend.py` with Docker available on 2026-09-08. Maven verification and Spring Boot packaging succeeded, and the packaged bootstrap initialized once, rejected repeat initialization and an insecure secret file, then exited without a web server. This confirms the full disposable-PostgreSQL harness passed for the current M6 work.

M7 synthetic voice safety slice: `VoiceTurnService` now has a provider-neutral safe call boundary. Any dialogue or synthesis failure creates a scoped fixture human-transfer case with `SYSTEM_FAILURE` and returns no invented audio. Focused voice, escalation, and ten-mixed-call isolation tests (6 tests) passed with the module-boundary validator. Twilio remains disabled; this uses no provider session, recording, or call.

M7 operations slice: added an authenticated owner/manager-only, tenant-scoped transfer queue at `/api/v1/operations/escalations` and a dashboard Transfers view. It returns only case ID, reason, state, and time; it exposes no transcript, caller, destination, or contact data. The queue is a local fixture only and performs no employee contact. Focused dashboard/voice/queue tests (6 tests), foundation validation, and module-boundary validation passed.

M7 synthetic load/evaluation evidence: five concurrent restaurant fixture calls at one business and ten mixed reference/restaurant fixture calls pass isolation tests. The M7 synthetic evaluation report records this as mock-only evidence and explicitly does not claim the 98% real-voice accuracy target or provider cost/latency evidence.

M7 provider-free acceptance review: dashboard static assets were checked for same-origin/session-scoped behavior; the Transfers view is tenant-scoped and redaction-safe. The synthetic evaluation report states its mock-only evidence and does not claim real-voice accuracy, latency, transfer rate, or cost. `docs/runbooks/twilio-sandbox-activation.md` now gives the exact owner-side, one-person sandbox activation and immediate-rollback checklist. Twilio remains disabled.

M7 final provider-free verification: the owner ran `python3 scripts/test_backend.py` with Docker available on 2026-09-08. All 165 tests passed with zero failures/errors. Maven packaged the backend successfully, and the packaged bootstrap initialized once, rejected repeat initialization and insecure secret-file permissions, then exited without a web server.

M7 Media Streams preparation: added the disabled `/webhooks/twilio/media` WebSocket route and a one-use, short-lived, business/conversation-scoped admission contract backed by sandbox allowlist, concurrency, duration, and spend leases. Frames are deliberately discarded; no recording or ordering path exists. The targeted admission test passed. A broad application-context run in this environment was blocked before startup because `VOICE_DB_URL` is unavailable outside the disposable-Docker harness. Twilio remains disabled. Token issuance through a reviewed TwiML admission response remains required before activation.

M7 reviewed TwiML admission slice: the signed/allowlisted webhook can now issue a fresh one-use WSS token bound to the configured sandbox business and exact Twilio `CallSid`, through `TwilioCallAdmissionService`. Adversarial admission coverage passes for reused, expired, wrong-caller, and over-limit tokens; the business/conversation binding prevents cross-tenant or wrong-call reuse. Twilio remains disabled and no console change or call occurred. Full Docker-backed verification remains owner-terminal-only in this session.

M7 Media Streams circular-dependency repair: the owner reran `python3 scripts/test_backend.py` with Docker available on 2026-09-08 after the WebSocket configuration was refactored to avoid self-construction. The complete harness passed: 168 tests, zero failures/errors, successful package/repackage, and secure bootstrap checks. Twilio remains disabled and no console or Railway change occurred.

Owner-only Indian menu review dashboard: the review draft is served only to OWNER actors, displays flagged entries first by category, and keeps approve/correct/reject/bulk decisions in browser session storage only. It has no import, publish, order, provider, or database-write endpoint. Focused owner-access/dashboard tests (3 tests) and module-boundary validation passed.

Durable owner menu-review decisions: migration V13 adds tenant/business-scoped `menu_review_decisions` with a database-enforced `UNPUBLISHED` state, version number, correction consistency, and owner actor reference. The review API now lists and writes only the authenticated owner's tenant rows; it uses compare-and-set optimistic versions and appends an audit event in the same transaction. The dashboard now persists approve/correct/reject and bulk review decisions through that API, while import, publication, ordering, Railway, Twilio, and provider behavior remain disabled. PostgreSQL integration coverage verifies owner-only enforcement, tenant isolation, stale-version rejection without a second audit event, unpublished-only database enforcement, and successful audit persistence. Owner-run `python3 scripts/test_backend.py` passed on 2026-09-08: 171 tests, zero failures/errors, package/repackage, and secure bootstrap checks.

Menu-review API contract follow-up: stale optimistic-version writes now return HTTP 409 and malformed review decisions return HTTP 400, so the owner dashboard can safely request a reload instead of treating a concurrency conflict as a generic error. The focused controller test passes without external providers.

Atomic menu-review bulk decisions: owner bulk approval now validates the complete request first and applies all decisions plus their audit events in one transaction. If a stale version is encountered, every prior decision/audit in that bulk operation rolls back. The dashboard calls the atomic endpoint and tells the owner that nothing was saved on a conflict. The owner ran the disposable PostgreSQL harness on 2026-09-08: 172 tests passed with zero failures/errors. Import and publication remain disabled.

Draft-revision binding: V14 stores the immutable SHA-256 revision of the non-executable Indian-menu review source on every decision. The repository returns and updates only decisions for the currently bundled draft revision; an older revision is excluded and cannot be reused through the decision API. PostgreSQL integration coverage simulates a stale revision and verifies it cannot be read or updated. The owner ran `python3 scripts/test_backend.py` on 2026-09-08: 172 tests passed with zero failures/errors. Menu import and publication remain disabled.

Owner review summary: the dashboard now displays authenticated-tenant counts for approved, corrected, rejected, and pending entries from the current draft revision only. The summary is read-only and reports `UNPUBLISHED`; it cannot import or publish data. PostgreSQL integration coverage verifies count isolation between two tenant owners. The owner reran `python3 scripts/test_backend.py` on 2026-09-08: 172 tests passed with zero failures/errors.
