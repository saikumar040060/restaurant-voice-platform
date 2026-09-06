# Project status

Updated: 2026-09-06. Current work: platform-first architecture review completed as local documentation; implementation not started.

## Product direction

Build the reusable voice core first, prove it with a fictional reference module, then implement restaurant ordering as the first production module. Later industries supply reviewed modules and tenant-scoped approved configuration/knowledge. Per-business fine-tuning is not the default. See [review](architecture/PLATFORM_FIRST_REVIEW.md), [ADR 0004](architecture/0004-platform-core-and-business-modules.md), [contracts](architecture/PLATFORM_CONTRACTS.md), and [milestones](MILESTONES.md).

The first live pilot remains US/English-first restaurant pickup ordering. Original safety, privacy, authorization, idempotency, audit, explicit confirmation, employee escalation and human-approval requirements remain in force.

## Implementation and evidence

M1 includes hashed staff sessions, BCrypt, expiry/revocation, roles, tenant predicates/composite FKs, staff/location administration, initial-owner bootstrap, login throttling, audit viewing/denial events, correlation IDs, and transactional audit with write rollback.

Historical evidence: 28 PostgreSQL integration tests passed, plus packaged bootstrap success, repeat rejection and secret-file checks. This architecture review inspected source/reports but did not rerun the backend suite or establish security/pilot readiness. Documentation checks are recorded in the review report. No voice engine, knowledge service, policy gateway, outbox, order workflow, POS integration or dashboard is implemented yet.

Retain staff authentication, UUIDs, tenant constraints, audit behavior and existing tests. Proposed adaptations are generic business/location relationships, module boundaries, typed call/service principals and audit-port extraction. Do not rewrite V1 SQL or silently change legacy APIs.

## Milestones

| Milestone | Status |
| --- | --- |
| 0 Engineering foundation | Local evidence recorded; hosted CI acceptance unverified |
| 1 Secure tenant foundation | In progress; lifecycle, DB privileges and validation gaps remain |
| 2 Generic businesses, profiles and module contracts | Planned |
| 3 Call state, policy gateway and durable actions | Planned |
| 4 Approved knowledge and configurable dialogue | Planned |
| 5 Reusable streaming voice proof | Planned; mock media and real conversation are separate gates |
| 6 Restaurant business module | Planned |
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

Use the repository policy's Terra/Medium preference for substantive implementation. Verify hosted CI/reconcile histories and close recorded M1 lifecycle/privilege/testing gates first. Then implement M2 in focused slices: business/location compatibility migration; typed principals/audit port; compiled registry/profile contracts; reference module and isolation tests.

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

No runtime voice vendor is chosen. M5a uses mocks/local fixtures; natural voice capability requires M5b measured evaluation with an approved implementation. This task requests no credentials and authorizes no real calls or product account connections.

## Environment and continuation

Java 21 is available through Homebrew; shell Java was 17, so select Java 21 per process. Maven 3.9.12, Node 22.19.0 and Docker are installed. Previous integration tests used disposable PostgreSQL 17.6 containers. Prior turn-scoped tool permissions are not current grants.

Local execution only. GitHub storage and CI do not keep this coding task running after laptop sleep. No continuing cloud task is configured. Publish reviewed changes and configure an approved reproducible remote task before claiming laptop-independent work.

The routing guidance is committed in local 01129e9 and the remote snapshot. It does not automatically switch models or select a runtime voice model.
