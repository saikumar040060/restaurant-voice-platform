# ADR 0003: Owner-managed staff and non-web initial provisioning

Status: accepted for local implementation. Date: 2026-09-05.

Keep initial owner provisioning outside the HTTP API. A non-web bootstrap profile reads an owner-only password file, takes a PostgreSQL transaction advisory lock, and creates exactly one initial tenant/owner only on an empty installation. Provisioning and audit commit atomically. Startup fails closed for insecure file permissions or repeated provisioning. Only fictional test accounts were created.

Owners can create tenant-scoped locations and staff, replace location assignments, change staff roles/enabled status, revoke employee sessions, and view recent tenant audit events. Managers/employees cannot administer these controls yet; narrower delegated permissions may be introduced with explicit requirements and tests. Staff APIs cannot create OWNER/SUPPORT/SYSTEM or modify existing owner access, avoiding ownership escalation and last-owner lockout. Ownership recovery/transfer is a separate future workflow.

Password material is never returned in employee DTOs, audit events, or record string representations. BCrypt rules are centralized. Username collisions return a generic 409; account handles are globally unique. Employee listing currently returns at most 200 rows without pagination; pilot onboarding must remain within this limit until pagination is added. Audit listing is bounded to 200.

Access changes lock the affected employee, revoke every existing session in the same transaction, and audit the action. Login takes the same employee row lock to avoid issuing a session concurrently with revocation from an already-validated stale account. Location assignment replacement validates all location IDs in the current tenant before deleting existing assignments; failure preserves existing access. Fresh location lookups enforce changed assignments immediately.

Authenticated application 403/404 outcomes are audited after the business transaction rolls back, using the actor as target and a generated correlation ID. Do not log guessed foreign IDs, URLs, bodies, passwords, or tokens. An audit-storage failure returns 503. Unknown unauthenticated access still has no tenant-attributed audit event; perimeter/operational security telemetry remains future work.

First-owner bootstrap is single-use per installation; additional tenant onboarding, invitation/reset, MFA, session rotation/cleanup, runtime DB privilege separation, and full security scans remain gates. The management APIs are not a completed dashboard or voice product.
