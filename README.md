# Restaurant Voice Platform

A conversational AI phone assistant for restaurants: explain dishes, answer grounded questions, recommend food, and take confirmed pickup orders. The broader platform will support configurable service-business workflows.

## Current state

Milestone 0 engineering foundation. No working voice agent, customer-facing ordering, POS integration, or production deployment is claimed. See [project status](docs/PROJECT_STATUS.md).

## Source of requirements

[Original handoff](PRODUCT_SPEC.md) and [owner-approved conversational requirements](docs/PRODUCT_ADDENDUM.md). The addendum expands restaurant knowledge without relaxing the handoff's policy controls.

## Local validation

Prerequisites: Python 3.11+, Java 21, Maven 3.9.12, Node 22.19.0, Docker Compose for database services. Java 21 must be selected in JAVA_HOME and PATH. On this machine it is available at `/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home`.

```sh
python3 scripts/validate_foundation.py
mvn -B -f apps/backend/pom.xml verify
VOICE_DB_PASSWORD=config-validation-only docker compose -f infra/local/compose.yaml config --quiet
```

The foundation check is offline and dependency-free. Maven's first build needs dependency access. CI runs the same checks. It does not prove application behavior, security isolation, or pilot readiness.

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

No GitHub remote or remote execution environment has been configured. Local work stops when the laptop sleeps.
