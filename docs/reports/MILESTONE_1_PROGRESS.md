# Milestone 1 progress report

Date: 2026-09-05. Status: IN PROGRESS / NOT COMPLETE.

## Outcome

Implemented the first secure identity and tenant-access slice. The owner requested local continuation despite the unresolved GitHub remote; hosted CI acceptance is still required.

## Components changed

Spring Security authentication/filter/controllers; session and login-throttle services; tenant service/controllers; sanitized error handling; Flyway PostgreSQL identity/tenant/audit migration; real PostgreSQL integration tests; disposable database test runner; OpenAPI contract; CI workflow; README, constitution, ADR 0002, and project status.

## Architecture and security decisions

Opaque 256-bit tokens, SHA-256 token hashes, BCrypt cost 12, 15-minute expiry, no cookies/default accounts, no CORS allowlist, loopback binding. Tenant scope comes from the stored employee; unknown JSON fields fail closed. Owners can create restaurants; managers/employees read assigned locations. SUPPORT/SYSTEM have no default interactive access. MFA-required accounts are denied until MFA exists. JDBC is used for explicit parameterized tenant SQL in this slice (a documented deviation from the preferred JPA stack). Composite foreign keys reject cross-tenant relationships. Audited writes share one transaction.

## Commands and exact results

- `JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home python3 scripts/test_backend.py`: exit 0; fresh PostgreSQL 17.6 migration, Java 21 compilation, 17 tests, zero failures/errors/skips, Maven packaging success. Runner removes its disposable container on completion.
- `python3 scripts/validate_foundation.py`: exit 0; required-file, POM/Java target, whitespace and limited private-key-marker checks pass. This is not a comprehensive security scan.
- `VOICE_DB_PASSWORD=config-check-only docker compose -f infra/local/compose.yaml config --quiet`: exit 0.
- `git diff --check` and staged whitespace check: exit 0 before commit.

Initial attempts: native PostgreSQL initdb failed due to sandbox shared-memory denial; Docker socket access was granted; the public image pull initially hit the local credential helper, then succeeded without using saved credentials. A first test attempt ran before the database was available and failed context initialization; subsequent runs against the ready database passed. One Maven command used the parent directory and failed to locate the POM; corrected immediately. No tests were disabled or assertions weakened.

## Acceptance evidence

Tests cover anonymous/forged identity rejection, tenant-header and JSON spoofing, cross-tenant reads and database relationships, manager/employee location assignment, role restrictions including support/system, exact tenant of writes, correlated audit, SQL injection input, empty input, hashed sessions, logout, expiry, disabled/MFA-required accounts, immediate role changes, immutable audit UPDATE/DELETE, 40 concurrent identity requests, absence of cookie/CORS auth, login throttling/reset, sensitive validation response, and rollback when audit insertion fails.

These results cover implemented paths only. There are no jobs, cache, exports, SSE channels, customer profiles, calls, or orders to test yet. Exhaustive whole-product tenant isolation is not claimed.

## Remaining scope and limitations

Milestone 1 is not complete. Still needed: controlled owner onboarding and employee/location management APIs, permission administration, broader authorization-denial auditing and authorized audit browsing, session cleanup/rotation/revoke-all administration, and expanded tests for those paths. Password reset and MFA are not implemented. Initial owner accounts only exist in test fixtures; there is no usable onboarding UI or seeded real account.

Runtime database privileges must be separated from migration ownership before staging; the current isolated test DB user owns the schema. Audit triggers cannot protect against database-owner tampering. No RLS or external immutable audit store is claimed. Counter retention/cleanup and deployment proxy configuration remain work; hashed lookup keys are still potentially identifying data. Full static/secret/dependency scans, release SBOM, independent review, restore evidence, and hosted CI remain unfulfilled. Backend remains loopback-only; no public serving is approved.

## Next work

Continue the remaining Milestone 1 management and lifecycle features, then menu/knowledge configuration and conversational simulation. No new product account or paid service is needed for the next local work. GitHub repository choice is still needed to push and obtain hosted CI evidence. Do not ask for credentials in chat.

## Commit

The implementation commit contains this report; resolve with `git log -1 --format=%H -- docs/reports/MILESTONE_1_PROGRESS.md`. Its SHA is included in the task response. Branch: milestone/1-tenant-foundation. No remote or PR exists.

## Corrections and approvals

User's restaurant knowledge requirements remain in the addendum. This slice strengthens fail-closed MFA behavior and transactional audit failure handling. Network/Maven-cache and local Docker socket permissions were granted for testing. No paid infrastructure, product credentials, external customer actions, or real data were introduced.

## Cloud/phone and integrations

Local only; laptop-independent execution is not configured. Push both milestone commits to the selected repository before remote execution. No plugin installation or product account connection occurred. GitHub selected-repository code/PR/CI scope is the minimum future integration. Docker test containers are local and ephemeral.
