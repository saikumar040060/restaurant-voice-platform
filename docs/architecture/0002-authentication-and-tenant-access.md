# ADR 0002: Revocable opaque sessions and explicit SQL tenant scope

Status: accepted for the first Milestone 1 implementation slice. Date: 2026-09-05.

Amendment, 2026-09-06: ADR 0003 subsequently implemented bootstrap, denial auditing and audit viewing described below as pending. [ADR 0004](0004-platform-core-and-business-modules.md) preserves staff authentication and proposes generic business scope, call/service principals and an independent audit port. Those new principal/migration changes remain unimplemented.

User requested continuing local work while GitHub selection remains pending. This does not waive hosted CI or other milestone gates.

Use Spring Security with opaque 256-bit bearer tokens. Persist only SHA-256 token hashes; sessions expire after 15 minutes and logout revokes the current token. Read enabled state, current role, and MFA-required state on every authenticated request. Password hashes use BCrypt cost 12. Validate the BCrypt 72-byte limit before password processing. Never create default credentials or expose self-registration. Owner provisioning remains an unimplemented controlled onboarding operation; current accounts are fictional test fixtures only.

MFA is not implemented. Accounts marked mfa_required are denied instead of silently bypassing MFA. SUPPORT and SYSTEM do not have interactive login or default business access. Bearer tokens belong in the Authorization header, not cookies or URLs; CSRF is disabled only for this non-cookie flow. A browser UI must not persist tokens in localStorage; its session design needs a separate review. No CORS origin is authorized. The server binds to loopback and must not be exposed before TLS, hardening, and approval.

Use JdbcTemplate for this initial identity/access module rather than JPA: the exact tenant predicates and composite foreign keys are directly visible and exercised against PostgreSQL. This is a bounded stack adjustment; all values are parameterized and domain services still own transactions. Future modules may use JPA if isolation remains explicit. Database RLS is not implemented or claimed. Provisioning/migration ownership and least-privilege runtime grants must be separated before staging.

Tenant context comes solely from the authenticated employee record. No endpoint accepts a tenant ID. Reject unknown JSON fields, including attempted tenant overrides. Owners access their tenant; managers/employees only read assigned locations and the corresponding restaurants. Restaurant creation is owner-only. Foreign and unassigned locations return identical 404 responses. Call, cache, job, export, and SSE paths do not yet exist and have no coverage claim.

Restaurant creation and its audit entry share a transaction; an audit insert failure rolls back creation. Login outcomes for known accounts and logout are audited. Audit UPDATE/DELETE is rejected by a PostgreSQL trigger, but database owners can bypass/remove controls; immutable external audit storage and privileges remain deployment work. General authorization-denial auditing and audit browsing are still pending.

Login attempts use database-atomic five-minute counters: 10 per username and 50 per source address. Ignore forwarded headers until trusted proxy configuration exists. Hash counter keys to avoid retaining plaintext usernames/IPs. A hash does not anonymize guessable identifiers. Cleanup/retention, distributed perimeter rate limiting, and broader API limits remain hardening work. Reject malformed request bodies without logging sensitive validation values.

Flyway V1 is a forward migration tested on fresh PostgreSQL 17.6. No down migration is supported; use a reviewed compensating migration or restoration. Never point integration tests at production: the suite truncates its disposable voice_test database.
