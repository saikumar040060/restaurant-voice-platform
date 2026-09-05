# Threat model skeleton

Status: preliminary; must be expanded and reviewed before external integration.

Assets: tenant data, customer identity, draft/orders, provider credentials, employee accounts, recorded content, audit evidence.

| Threat | Required control | Required evidence |
|---|---|---|
| Cross-tenant access through IDs, jobs, cache, exports, SSE | Authenticated tenant scope on all access paths | Negative isolation suite |
| Caller ID spoofing / shared phone | Lookup hint only; stronger verification for sensitive actions | Shared/blocked/spoofed call scenarios |
| Prompt injection in speech or knowledge | Narrow schemas and policy gateway; deny by default | Prohibited tool/action tests |
| Duplicate or forged webhook | Provider verification, replay ledger, stable IDs | Duplicate/replay/signature tests |
| Lost response after external commit | Outbox plus provider idempotency and reconciliation | Crash and ambiguous-outcome suite |
| Stale menu or knowledge | Versioned facts and renewed confirmation after price change | Drift and stale-fact tests |
| Sensitive data in voice/logging | Minimal retention, consent, redaction, provider controls | Redaction and retention tests |
| Stolen staff session | Least privilege, secure sessions, revocation, MFA-ready admin | Role/session tests |
| SSRF through integration configuration | Allowlisted endpoints and redirect/address controls | Adversarial outbound tests |
| Resource exhaustion | Bounded calls, timeouts, tenant limits, backpressure | Load and failure tests |

Open: authentication provider, jurisdiction consent guidance, provider retention capabilities, recovery objectives, deployment topology, external security review. None of these controls is claimed implemented in Milestone 0.
