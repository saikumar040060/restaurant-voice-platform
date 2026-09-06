# ADR 0001: Modular monolith and controlled voice actions

Status: accepted for foundation. Date: 2026-09-05.

Amendment, 2026-09-06: [ADR 0004](0004-platform-core-and-business-modules.md) supersedes restaurant-specific core placement and the original sequence. The monolith, provider boundaries and reliability principles remain valid. The non-web statement below describes M0; authenticated HTTP APIs were later added in M1.

Use Java 21, Spring Boot 3.5.14, Maven 3.9.12, Spring Security, JPA/Hibernate, Flyway, PostgreSQL 17.6, React/TypeScript, versioned REST and SSE. Spring dependency versions inherit the pinned Boot BOM. Frontend package versions and lockfile will be committed with its first implementation. The selected Spring version is present in the local Maven cache; this is not a claim that it is the latest or has passed vulnerability review.

Start with one backend process and one PostgreSQL database, clear module APIs, and a durable outbox for side effects. Redis is deferred. Telephony, STT, dialogue, TTS, POS, and messages use provider adapters. Square is first live POS; Toast remains mocked until approved access. No runtime model is selected yet.

Modules: identity/access, tenants/locations, employees/routing, customer/consent, menu, restaurant knowledge, call/conversation, orders, policy/gateway, providers, audit, operations, observability. Domain services own transactions. Model output never reaches provider APIs directly.

Foundation bootstraps a non-web application with no endpoints, business actions, or external connections. Security and tenant isolation must precede exposing business APIs. A boot smoke test is infrastructure evidence only.

Exactly-once external effects are a goal contingent on provider guarantees. The application guarantees durable intent and prevents unsafe resubmission; an ambiguous provider outcome becomes SUBMISSION_UNCERTAIN and requires reconciliation. An outbox alone does not prove exactly-once effects.

Image versions are fixed tags for local development; release image digests, vulnerability scans, and patch review are required before staging. Cloud provider and recovery objectives remain open.
