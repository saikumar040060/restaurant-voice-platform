# Milestone 1: Management and onboarding progress

Date: 2026-09-05. Status: IN PROGRESS / NOT COMPLETE.

## Outcome and changes

Added owner-only location creation, employee creation/listing, staff role/enable changes, assignment replacement, revoke-all-sessions, and bounded tenant audit viewing. Added initial non-web owner provisioning from an owner-only secret file. Preserved separate identities and existing local commits. Added centralized password rules, generic username-conflict handling, redacted employee request representation, and authenticated denial audit handling. Updated OpenAPI, test runner, README, ADR 0003, onboarding runbook, and status/integration records.

## Decisions and security

Only MANAGER/EMPLOYEE can be created through staff APIs. Owners cannot be demoted/disabled by those APIs, and foreign tenant targets return 404. Location IDs are all checked before assignment replacement. Access updates lock the employee and revoke sessions atomically with audit. Login uses the same lock to prevent stale concurrent issuance. The audit viewer is owner-only and capped at 200 records; employee listing is capped at 200 without pagination.

Bootstrap has no HTTP endpoint, requires an empty installation, serializes with an advisory transaction lock, hashes the owner password, and writes its audit event in the same transaction. The CLI requires web mode none, reads a POSIX owner-only secret file, and closes the application. Only disposable fictional accounts were provisioned during tests. See ADR 0003 and INITIAL_OWNER runbook for limits.

## Validation

- Java 21 `python3 scripts/test_backend.py`: exit 0. PostgreSQL 17.6, 28 integration test cases, zero failures/errors/skips; Maven compilation and packaging succeed.
- The same runner then invokes the packaged bootstrap JAR: initial setup succeeds; repeated setup fails; broadly readable password file fails; tenant count remains one. The runner removes the disposable database container and temporary secret directory.
- `python3 scripts/validate_foundation.py`: passes before commit; checks foundation structure, POM Java target, whitespace, and a limited private-key marker scan.
- OpenAPI YAML parses with Ruby's standard YAML parser: 11 paths. Python PyYAML was unavailable, so the parser check used Ruby rather than adding a dependency. This is a syntax check, not full OpenAPI semantic validation.
- Git whitespace checks pass before commit. No application tests were skipped or weakened.

New behavioral coverage includes staff password hashing/non-disclosure, forbidden privileged roles, foreign restaurant/location rejection, invalid timezone, preservation of assignments after invalid replacement, immediate removal of location access, revocation after role changes, owner/foreign-account protection, non-owner management denial, bounded tenant audit views, and bootstrap single-use behavior. Existing concurrency, session, SQL injection, audit rollback, and tenant-isolation tests continue to pass.

## GitHub result

Owner confirmed account saikumar040060 and authorized creation of private restaurant-voice-platform. Repository created and verified Private at https://github.com/saikumar040060/restaurant-voice-platform. Origin configured locally. The repository remains empty: SSH authentication failed, HTTPS has no configured credentials, and the connector cannot access the new repository (404). No code was uploaded and no CI run occurred. A question for repository-only connector access is pending. No credentials were requested in chat, copied from the browser, or committed.

## Remaining work

Session rotation and retention cleanup, least-privilege runtime/migration database identities, full security scans and independent review, comprehensive remaining permission checks, and hosted CI remain gates. Additional tenant self-service onboarding, owner transfer/recovery, invitation/password reset, MFA enrollment, and onboarding/dashboard UI are not implemented. These APIs are local-only and are not a live restaurant voice agent.

## Next step and required approval

Resolve repository publishing access without exposing credentials or granting unrelated repositories. Publish local source and run CI. Then complete remaining Milestone 1 lifecycle/security checks before moving to menu/knowledge configuration. No product API account or paid resource is needed for that work.

## Commit, cloud, and integrations

Branch: milestone/1-tenant-foundation. Resolve this report's implementation commit with `git log -1 --format=%H -- docs/reports/MILESTONE_1_MANAGEMENT.md`; task response includes SHA. No PR exists. Both prior commits remain local, and the new commit is local until publishing succeeds.

Local execution only; the laptop cannot be closed with an expectation of continued work. Repository-only GitHub access is pending; no new plugin, collaborator, product account, cloud resource, real call, or customer data was introduced.
