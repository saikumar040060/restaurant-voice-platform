# Restaurant Voice Platform

A reusable conversational AI voice platform for service businesses, with restaurant pickup ordering as the first production module. Build the shared voice core first and specialize it through approved business knowledge, policies and workflows. The repository name remains unchanged.

## Current state

Milestone 1 is in progress: authenticated tenant-access APIs and PostgreSQL integration tests are implemented. No working voice agent, customer-facing ordering, POS integration, or production deployment is claimed. See [project status](docs/PROJECT_STATUS.md).

## Source of requirements

[Original handoff](PRODUCT_SPEC.md) and [owner-approved conversational requirements](docs/PRODUCT_ADDENDUM.md). The addendum expands restaurant knowledge without relaxing the handoff's policy controls.

The 2026-09-06 platform-first direction is documented in [the architecture review](docs/architecture/PLATFORM_FIRST_REVIEW.md), [ADR 0004](docs/architecture/0004-platform-core-and-business-modules.md), [contracts](docs/architecture/PLATFORM_CONTRACTS.md), and [the milestone plan](docs/MILESTONES.md). These are designs, not implemented voice capabilities.

## Local validation

Prerequisites: Python 3.11+, Java 21, Maven 3.9.12, Node 22.19.0, Docker Compose for database services. Java 21 must be selected in JAVA_HOME and PATH. On this machine it is available at `/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home`.

```sh
python3 scripts/validate_foundation.py
python3 scripts/test_backend.py
VOICE_DB_PASSWORD=config-validation-only docker compose -f infra/local/compose.yaml config --quiet
```

The foundation check is offline and dependency-free. The backend test script requires Docker, creates its own disposable PostgreSQL 17.6 container, runs Maven verification, and removes the container. Java 21 must be selected in JAVA_HOME/PATH. Initial downloads require network access. CI defines the same checks; hosted success is unverified. Tests cover the implemented access paths only, not full pilot readiness.

## Implemented API slice

Login/logout, current identity, tenant/location-scoped restaurant reads, owner-only restaurant creation, and assigned location reads. See `packages/contracts/openapi.yaml`. Sessions expire after 15 minutes. There is no public registration or default account. Initial owner provisioning uses the non-web command in [the onboarding runbook](docs/runbooks/INITIAL_OWNER.md). Only isolated fictional test accounts have been created so far. Owner APIs now support staff/location administration, session revocation, and recent audit viewing. See ADR 0002 for security decisions and limitations.

## Local database

Copy `infra/local/.env.example` to `infra/local/.env`, replace its development-only placeholder locally, then run:

```sh
docker compose --env-file infra/local/.env -f infra/local/compose.yaml up -d --wait
docker compose --env-file infra/local/.env -f infra/local/compose.yaml down
```

The database binds only to localhost. Its volume persists when stopped. Never reuse a production secret or put credentials in chat. Redis and external integrations are not needed yet.

## Structure

- `apps/backend`: Java 21 / Spring Boot / Maven modular monolith
- `apps/dashboard`: planned React / TypeScript operations UI
- `packages/contracts`: versioned API contracts
- `infra`: local services and future approved infrastructure
- `docs`: architecture, security, decisions, progress, and runbooks
- `tests`: conversation evaluation, future load and end-to-end suites

The private GitHub repository is [saikumar040060/restaurant-voice-platform](https://github.com/saikumar040060/restaurant-voice-platform). The baseline snapshot is published and connector access works. Local and remote histories need reconciliation; see project status. Current architecture edits are local, hosted CI success is unverified, and no continuing remote coding task is configured. Local work stops when the laptop sleeps.
