# Platform-first architecture review

Date: 2026-09-06. Scope: architecture and implementation planning only.

## Recommendation

Build a reusable voice conversation core, prove it with a minimal fictional business configuration, and then implement restaurant ordering as the first production business module. Keep the Java modular monolith and PostgreSQL foundation. Do not build a universal workflow designer or implement hotel/salon booking yet.

The original mission already anticipates multiple industries; its implementation sequence and location schema are restaurant-specific. The owner's new direction changes those boundaries and the delivery sequence. It does not relax the safety model or expand the first live pilot beyond US, English-first, pickup restaurant ordering.

“Core complete” means its contracts and failure behavior have been demonstrated against an explicit evaluation set. Mock audio and scripted dialogue cannot establish natural conversation quality. A later approved voice implementation must separately demonstrate recognition, grounded responses, latency, and interruption handling. A permanent claim of being complete for every future industry is not realistic.

## Evidence and conflicts

Reviewed the eight requested documents, V1 migration, identity/session/bootstrap implementation, tenant and management services, API contract, current tests, and CI definition.

| Current evidence | Architectural consequence | Decision |
| --- | --- | --- |
| `V1__identity_and_tenants.sql`: every location requires a restaurant FK | A hotel cannot own a location without masquerading as a restaurant | Introduce generic businesses and business locations additively; retain restaurant compatibility |
| `TenantService` mixes generic location reads and restaurant creation | Generic tenancy depends on the first vertical | Move restaurant behavior behind its module when generic tenancy is introduced |
| `Actor` contains an employee ID; audit actor FK references employees | A caller or background worker has no proper principal; an LLM must not become an employee | Add typed execution principals and a dedicated audit port without enabling SYSTEM interactive login |
| `SessionService` also writes general audit events | All future workflows would depend on login implementation | Extract audit responsibility while preserving transaction semantics and existing events |
| `BootstrapService` refuses a second tenant in an installation | This is initial provisioning, not full multi-tenant onboarding | Preserve it; add a separately authorized provisioning service and tests before onboarding additional businesses |
| Calls, knowledge, policy gateway, outbox, and voice adapters are not implemented | Boundaries can be established before a substantial rewrite becomes necessary | Introduce contracts and reference fixtures before business-specific tools |
| Original M2/M4 precede voice work at M7 | Restaurant structure would drive the core before a voice prototype tests it | Move shared voice proof to M5; restaurant specialization to M6 |
| Current authentication checks are staff-oriented | A caller must not inherit owner or employee authority | A call receives narrow, tenant-bound capabilities independently of staff sessions |
| Current tests exercise identity and administration | 28 tests do not establish call isolation, conversational accuracy, or reliable POS effects | Preserve these tests and add separate core/module/provider evaluations |

No existing voice system or hotel integration was found. This review does not certify the entire application secure or complete Milestone 1.

## Decisions documented before roadmap changes

1. Keep one modular backend deployment and database; use module-owned tables and ports, not new microservices.
2. Make tenant, business, location, business profile, call, knowledge publication, and approved action concepts part of the shared platform.
3. Make restaurant menus, dishes, prices, modifiers, order state, and POS mappings owned by the restaurant module.
4. Keep core safety ceilings immutable through business configuration. Effective permissions are the intersection of platform, installed module, tenant configuration, principal scope, current state, and confirmation evidence.
5. Use a reviewed, compiled module registry. Businesses configure installed modules; they cannot upload code, invent API tools, or supply unrestricted system prompts.
6. Use approved, scoped knowledge and typed workflow configuration by default. Do not fine-tune per business; evaluate a specific model behavior gap before proposing fine-tuning, with separate data/cost approval.
7. Separate dialogue from authoritative state. Speech proposes changes; typed workflow reducers, policies, and adapters decide and execute them.
8. Pin published configuration and module versions per call, but immediately enforce revocations, kill switches, consent withdrawal, and current operational facts.
9. Add real-time media epochs, cancellation, playback acknowledgements, and final-turn sequencing to the core contracts before order confirmation is implemented.
10. Build a small reference module for fictional FAQ and callback simulation to test reuse; do not make that reference module an unrestricted general assistant.

The detailed decision is [ADR 0004](0004-platform-core-and-business-modules.md). Boundary contracts are in [PLATFORM_CONTRACTS.md](PLATFORM_CONTRACTS.md). The implementation sequence is [MILESTONES.md](../MILESTONES.md).

## Keep from Milestone 1

Retain UUID identities, tenant predicates and composite foreign keys, PostgreSQL/Flyway, BCrypt passwords, opaque hashed sessions, expiry/revocation, login rate limiting, owner-only administration, explicit location assignments, safe bootstrap, generated correlation IDs, append-only audit behavior, and transactional write-plus-audit tests. Preserve the no-public-signup behavior, existing deny-by-default role restrictions, and all existing migrations and API behavior during the transition.

Keep the historical restaurant APIs for restaurant callers of those APIs. Adapt their implementations behind a compatibility boundary; do not silently expose a hotel location through an endpoint that returns a restaurant identifier. Keep names and existing UUIDs stable while adding generic business mappings. Do not remove tenant constraints or rewrite deployed V1 SQL.

Milestone 1 still needs the documented session lifecycle, database privilege, security-validation, and hosted-CI work. Business-generalization and system/call principals are additional M2 work, not proof those earlier gaps have disappeared.

## Alternatives rejected for now

| Alternative | Why it is not the chosen next step |
| --- | --- |
| Build all restaurant ordering first, extract later | Reinforces the location and workflow coupling the owner wants to avoid |
| Build a universal workflow language or customer plugin marketplace | Expands scope and introduces executable configuration before two real workflows establish requirements |
| Train one model for every tenant | Makes frequently changing business facts harder to govern; training is not an authorization boundary |
| Let modules call their providers directly | Bypasses shared confirmation, audit, revocation, and idempotency controls |
| Require one particular voice vendor's session format everywhere | Couples business state to transport/model implementation and obstructs failure testing |
| Replace working authentication or introduce microservices now | Adds migration and operating work without solving the current boundary problem |

## Repository and verification correction

Local commit `01129e9` and published GitHub commit `3a9f202562a5c0f7966c4b2c379589fd8f812895` have the same Git tree, `92387fc6be79ab9c90b90f89ef72a75e0f07428d`. Thus the published snapshot contains the inspected source. Their commit histories are different: the remote commit's parent is `da67c7e`, while the local milestone history remains local. Earlier wording that the complete local history was uploaded was inaccurate. Do not force-push or discard either history; reconcile deliberately before normal Git-based collaboration.

The old status saying the repository is empty or access is pending is obsolete. GitHub connector read access is verified. No hosted CI success was established by this review. The prior run-list tool filters to pull-request events; an empty result cannot establish that no push-triggered run exists. Hosted CI verification and history reconciliation remain tracked work, not part of this documentation-only architecture change.

## Handoff for implementation

Use the project policy's Terra/Medium preference for the subsequent substantive implementation, and Terra/Low for clear mechanical subtasks. No automatic model switch is claimed. Start with remaining M1 gates, then M2 generic tenancy and module contracts. Keep the proposed interfaces as design until the focused implementation task compiles them and adds its own tests.

No voice vendor, product account, cloud resource, recording, real call, credential, or customer data was introduced. The architecture documents and first M2 implementation slice are committed locally but not published. The existing 28-test result is historical evidence; this review does not claim a fresh backend integration run.

## Review validation

- `python3 scripts/validate_foundation.py`: exit 0; 18 required files and 61 text files checked, valid POM/Java target and foundation formatting.
- `git diff --check`: exit 0.
- Read-only document validation: 16 Markdown-only changed/new files, 28 relative links resolve, fenced blocks are balanced, and the historical PRODUCT_SPEC content is unchanged beneath its new amendment notice.
- Verified published commit tree equals the local baseline tree; this does not equate their commit histories or prove CI success.
- No application sources, executable contracts, SQL migrations, tests, OpenAPI endpoints or dependency files were changed. Backend tests were not rerun for this documentation-only review. No commit or external write was made in this architecture task, so the repository's mandatory pre-commit backend gate has not been bypassed.

Requirement coverage: current-state review/conflicts and retained M1 work are above; core/module decisions are in ADR 0004; domain/extension and isolation contracts are in PLATFORM_CONTRACTS; M2–M7 acceptance and original-scenario mapping are in MILESTONES. Original safeguards and human-approval boundaries remain binding across all four documents.
