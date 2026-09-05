# Codex/Astra Master Handoff: Secure AI Voice Operations Platform

Copy the section titled **MASTER EXECUTION PROMPT** into a new Codex task using Astra. Keep this file in the repository as the authoritative product specification.

---

# MASTER EXECUTION PROMPT

You are the lead architect, senior full-stack engineer, security engineer, QA engineer, DevOps engineer, and technical program manager for this project. Build the product milestone by milestone from an empty repository to a production-candidate pilot. You own ordinary technical decisions, implementation, tests, documentation, and repairs. The human owner approves external accounts, credentials, paid resources, real customer activity, production deployment, and other sensitive actions.

Do not attempt to finish the whole product in one uncontrolled change. Complete one milestone at a time, verify its acceptance criteria, commit it, and produce a concise evidence report before continuing. Continue automatically to the next milestone when all checks pass and no human approval is required. Stop only for a genuine blocker or an action listed under **Human approval required**.

Continuously review this specification, the architecture, implementation, tests, security controls, reliability, scalability, and operating cost. If you discover an error, missing requirement, safer design, simpler approach, or meaningful improvement, tell the owner clearly in the milestone report and record the reason. You may automatically implement low-risk, backward-compatible improvements that remain inside the approved scope and do not weaken a safety control. Ask the owner before implementing any improvement that materially changes product scope, user behavior, cost, external services, stored data, security boundaries, legal assumptions, or production infrastructure. Never follow the specification blindly when evidence shows that a requirement is incorrect or unsafe; stop, explain the evidence, and propose the safest correction.

## 1. Product mission

Build a secure, low-cost, highly available, multi-tenant AI voice platform for service businesses. The first production module is restaurant phone ordering. The same platform must later support hotels, salons, auto-repair shops, and other appointment or service businesses through configuration and replaceable workflow modules.

The restaurant agent must:

- Answer multiple calls concurrently.
- Identify a possible returning customer from caller ID without treating caller ID as proof of identity.
- Answer approved questions using current structured restaurant data.
- Create pickup orders from a synchronized menu.
- Support quantities, sizes, modifiers, special instructions, and customers changing their minds.
- Calculate prices using deterministic code or the POS provider, never an LLM.
- Read back the complete order and obtain explicit confirmation before submission.
- Send an SMS confirmation or provider-hosted payment link.
- Transfer refunds, payment disputes, complaints, allergy/safety matters, emergencies, manager requests, and uncertain cases to an employee.
- Continue safely through temporary provider or POS failures without creating duplicate orders.
- Keep every restaurant's data strictly isolated.

The product is an assistant to restaurant employees, not an autonomous authority for refunds, medical/allergy advice, payment disputes, or emergencies.

## 2. Non-negotiable safety model

The language model may interpret speech, ask questions, and propose structured actions. It must never directly call a POS, payment, SMS, customer-data, or administrative API.

Every proposed action must flow through:

1. Schema validation.
2. Tenant and permission validation.
3. Current menu/business-state validation.
4. Deterministic policy evaluation.
5. Idempotency and replay protection.
6. Explicit customer confirmation when required.
7. Audited execution by a narrow tool adapter.

Default to deny. Missing, malformed, ambiguous, stale, or contradictory data must never authorize an action.

### Always transfer or create a callback

- Refunds, voids, chargebacks, and payment disputes
- Complaints, food poisoning, injury, threats, harassment, or emergencies
- Allergy questions or assurances about cross-contamination
- Requests for a manager
- Catering or unusually large orders above a configurable threshold
- Gift-card redemption or account ownership changes
- Identity-sensitive loyalty actions
- Any order whose item, modifier, price, fulfillment time, or intent is uncertain
- Any situation in which a dependency fails and safe completion cannot be proven

### Payment rule

Never request, transcribe, log, or store spoken card numbers, CVV, or full payment credentials. Send a checkout link hosted by Square, Toast, Stripe, or another approved payment provider. Keep the initial system outside direct card-data handling as far as reasonably possible.

### Caller identity rule

Store an internal UUID as `customer_id`. Normalize phone numbers to E.164 and use them only as lookup hints. Generate a unique `call_id` for every call and an `order_id` for every order. Require stronger verification for sensitive customer or loyalty actions. Support shared, blocked, missing, and spoofed caller IDs.

## 3. Scope

### Initial production scope

- United States
- English first; architecture ready for Spanish
- Independent takeout-focused restaurants
- Pickup orders only
- Square integration first
- Toast adapter and contract tests prepared; live Toast access later
- Human transfer and callback workflow
- Restaurant management dashboard

### Explicitly out of scope for the first pilot

- Delivery dispatch
- Cash collection by the AI
- Spoken card processing
- Refund execution
- Allergy or medical advice
- Drive-through hardware
- Hotel workflows
- Loyalty redemption
- Gift cards
- Autonomous menu or price changes
- Production Toast integration without approved access

## 4. Architecture principles

Start with a modular monolith, not microservices. Use clean module boundaries and asynchronous interfaces so high-load components can be extracted later. Do not add Kafka, Kubernetes, service discovery, or distributed infrastructure until measurable traffic justifies it.

Preferred implementation:

- Backend: Java 21, Spring Boot, Gradle or Maven, Spring Security, JPA/Hibernate
- Frontend: React and TypeScript
- Database: PostgreSQL
- Optional ephemeral coordination: Redis, introduced only when required
- Local environment: Docker Compose
- API: versioned REST plus WebSocket or server-sent events for live dashboard updates
- Database migrations: Flyway or Liquibase
- API documentation: OpenAPI
- Telephony: provider-neutral interface with a Twilio-compatible adapter
- AI/voice: provider-neutral interfaces for speech-to-text, reasoning/dialogue, and text-to-speech
- POS: `PosProvider` interface with Mock, Square, and Toast implementations
- Messaging: `MessageProvider` interface with Mock and SMS implementations
- Cloud: containers and managed PostgreSQL; deployment provider chosen later with owner approval
- CI: GitHub Actions or equivalent

Use a monorepo with clear directories such as:

```text
apps/backend
apps/dashboard
packages/contracts
infra/local
infra/cloud
docs/architecture
docs/security
docs/runbooks
tests/load
tests/e2e
```

If repository conditions require a different layout, document the reason in an architecture decision record.

## 5. Core modules

- Identity and access
- Tenant/restaurant/location management
- Employee and transfer routing
- Customer profiles and consent
- Menu, availability, price, and modifier management
- Call session and conversation state
- Order state machine and confirmation
- Deterministic policy engine
- Tool/action gateway
- POS adapters
- Telephony and voice adapters
- SMS/payment-link adapters
- Audit ledger
- Operations dashboard
- Metrics, alerts, and incident diagnostics

## 6. Required domain identifiers and states

Use opaque UUIDs internally. Never use phone number, email, restaurant name, or sequential database ID as an authorization boundary.

Minimum identifiers:

- `tenant_id`
- `restaurant_id`
- `location_id`
- `employee_id`
- `customer_id`
- `call_id`
- `conversation_turn_id`
- `order_id`
- `order_submission_id`
- `tool_request_id`
- `audit_event_id`
- `provider_event_id`

Minimum call states:

`RINGING -> ACTIVE -> TRANSFERRING | CALLBACK_REQUIRED | COMPLETED | FAILED`

Minimum order states:

`DRAFT -> VALIDATING -> QUOTED -> AWAITING_CONFIRMATION -> CONFIRMED -> SUBMITTING -> SUBMITTED`

Terminal or exception states:

`CANCELLED`, `TRANSFERRED`, `SUBMISSION_UNCERTAIN`, `FAILED`

Do not allow illegal state transitions. Persist transitions atomically and audit them.

## 7. Reliability and concurrency requirements

- Each call has isolated memory and state.
- No state may leak between callers or tenants.
- All provider webhooks require signature verification, timestamp/nonce checks when supported, and replay protection.
- Order submission uses a stable idempotency key derived from persisted identifiers, not conversation text.
- Duplicate webhooks, client retries, process restarts, and network timeouts must not create duplicate POS orders.
- Use transactional outbox or an equivalently proven pattern for durable external side effects.
- Retry only operations known to be safe and idempotent; use bounded exponential backoff with jitter.
- When final submission outcome is unknown, use `SUBMISSION_UNCERTAIN`, alert an employee, and reconcile before retrying.
- Use timeouts, circuit breakers, connection limits, and backpressure.
- Graceful degradation: if AI fails, offer human transfer/callback; if POS fails, do not claim the order was accepted.
- Begin pilot capacity at five concurrent calls per location and prove higher levels through load testing.

Pilot availability target is 99.9% for the owned application path, excluding separately reported third-party outages. Do not claim this target until monitoring evidence exists.

## 8. Security requirements

- Threat model before external integration work
- Tenant isolation enforced server-side on every request and background job
- RBAC with least privilege for owner, manager, employee, support, and system roles
- Strong password hashing or managed identity provider
- MFA-ready administrative accounts
- Short-lived sessions/tokens and secure rotation/revocation
- TLS for all external traffic and encryption at rest
- Central secrets management; no secrets in source, logs, images, fixtures, or frontend bundles
- Input validation and output encoding
- Parameterized database access
- CSRF protection where cookies are used
- Strict CORS allowlist
- SSRF defenses for outbound integrations
- Rate limiting by tenant, source, endpoint, and phone number where appropriate
- Upload restrictions if menu files are supported
- Redacted structured logs; never log payment credentials or authentication secrets
- Configurable transcript/recording retention and deletion
- Recording-consent announcement configurable by jurisdiction
- Append-only audit trail with actor, tenant, action, target, outcome, correlation ID, and timestamp
- Dependency, secret, SAST, container, and infrastructure scanning in CI
- SBOM generation for releases
- Backup and restore tests
- Security headers and secure defaults
- No production debug endpoints
- No model prompt or caller text may override policy or tool authorization

Create `SECURITY.md`, a data-flow diagram, trust-boundary diagram, threat model, incident-response runbook, backup/restore runbook, and access-control matrix.

## 9. Privacy requirements

- Collect the minimum data necessary.
- Separate consent for call recording, marketing, and saved preferences.
- Support calls without creating a persistent customer profile.
- Define retention periods by data class.
- Support export and deletion workflows without breaking required financial/audit records.
- Never expose one customer's order history merely because the caller ID matches.
- Mask phone numbers and personal information in routine dashboards based on role.
- Clearly label AI interaction where required and never impersonate a specific real employee.

Legal compliance must be reviewed by qualified counsel before production; do not represent engineering controls as legal certification.

## 10. Cost-efficiency rules

- Use deterministic code for business rules, calculations, state changes, and validation.
- Use a smaller/cheaper runtime model for routine calls; reserve stronger models for genuinely ambiguous interpretation.
- Cache approved FAQs and structured menu data with safe invalidation.
- Do not repeatedly send the full transcript when compact state is sufficient.
- Use voice activity detection and stop processing silence.
- Summarize completed calls asynchronously.
- Allow transition to SMS/web ordering when it is cheaper or clearer.
- Track per-call cost for telephony, transcription, generation, speech, SMS, and infrastructure.
- Enforce configurable cost and duration limits without abruptly abandoning a caller.
- Keep vendors behind interfaces and record provider-specific limitations.
- Do not purchase or provision paid infrastructure without owner approval.

## 11. Dashboard requirements

- Secure login and role-aware navigation
- Current calls and status
- Draft, awaiting-confirmation, submitted, uncertain, failed, and transferred orders
- Call summary and redacted transcript when authorized
- Human transfer/callback queue
- Menu and item-availability controls
- Employee routing configuration and business hours
- Integration health
- Audit-event viewer for authorized managers
- Metrics: calls answered, containment, transfer rate, order completion, failure rate, latency, order corrections, estimated recovered revenue, and cost per completed order
- Clear visual warnings for uncertain submission or degraded providers

## 12. Initial seed restaurant

Create fictional seed data named `Harbor Pizza Test Kitchen`; never use a real business.

Hours: 11:00 AM–10:00 PM local time.

Menu must include:

- Cheese Pizza: small $9.99, medium $12.99, large $15.99
- Pepperoni Pizza: small $11.49, medium $14.49, large $17.49
- Build Your Own Pizza with required size and optional toppings
- Toppings: extra cheese $1.50, mushrooms $1.00, onions $0.75, jalapeños $0.75
- Garlic Bread $5.49
- House Salad: small $5.99, large $8.99; required dressing choice
- Fountain Drink: small $1.99, medium $2.49, large $2.99; required flavor

Include sold-out toggles, tax configuration, maximum quantity, special-instruction limits, and configurable large-order transfer threshold. Store money as integer minor units or fixed decimal with explicit rounding; never binary floating point.

## 13. Required simulated conversation scenarios

At minimum automate these end-to-end scenarios:

1. New customer completes a simple pizza order.
2. Returning phone number is recognized but identity is confirmed safely.
3. Shared family phone number does not reveal private history.
4. Blocked caller completes an order without persistent profiling.
5. Customer changes size and removes a modifier.
6. Customer orders multiple quantities and mixed sizes.
7. Required modifier is missing and the agent asks for it.
8. Item becomes sold out during the call.
9. Customer rejects the read-back; nothing is submitted.
10. Customer confirms; exactly one submission occurs.
11. Duplicate confirmation message does not create a second order.
12. Duplicate telephony webhook is ignored safely.
13. Process restarts during submission and recovers exactly once.
14. POS times out before response and order becomes uncertain instead of blindly retrying.
15. Refund request transfers without any refund action.
16. Payment dispute transfers without exposing payment data.
17. Allergy question transfers without giving assurance.
18. Complaint produces a redacted summary and transfers.
19. Employee unavailable creates a callback request.
20. Prompt-injection attempt cannot change menu, price, permissions, or policy.
21. Caller tries to obtain another customer's history and is denied.
22. Tenant A cannot read or mutate Tenant B data through API, job, cache, websocket, export, or identifier guessing.
23. Ten calls run concurrently without state mixing.
24. Excess traffic triggers controlled rate limiting/backpressure.
25. AI provider failure degrades to transfer/callback.
26. Database outage does not falsely confirm an order.
27. SMS failure does not duplicate the POS order.
28. Invalid or stale menu identifier fails closed.
29. Price changes before confirmation cause re-quote and renewed confirmation.
30. Transcript/log redaction removes configured sensitive data.

## 14. Quality gates

No milestone is complete unless:

- Compilation, linting, formatting, unit tests, integration tests, and relevant end-to-end tests pass.
- Database migrations upgrade and downgrade safely where the chosen migration system supports it; destructive migrations require explicit review.
- No credentials or secrets appear in the repository.
- New external side effects have idempotency and failure tests.
- New permissions have positive and negative authorization tests.
- API contracts and operational documentation are updated.
- Security-critical code has adversarial tests.
- Known limitations and residual risks are documented honestly.

Pilot release additionally requires:

- Zero duplicate orders in the full failure-injection suite.
- Zero cross-tenant access in the isolation suite.
- Zero execution of prohibited actions.
- At least 98% line-item/modifier accuracy on the approved pilot evaluation set, with all uncertainty transferred rather than guessed.
- Successful backup restoration demonstration.
- Successful concurrent-load and soak tests at the declared capacity.
- Human review of representative call recordings/transcripts where legally authorized.

## 15. Milestone plan

### Milestone 0 — Repository and engineering constitution

Create the monorepo, README, contribution rules, coding conventions, architecture decision record template, threat-model template, local environment, CI skeleton, test strategy, and `AGENTS.md`. Record assumptions and open external dependencies. No paid services.

Acceptance: clean checkout can run documented validation commands; CI runs; no application feature is falsely claimed.

### Milestone 1 — Secure multi-tenant foundation

Implement tenants, restaurants, locations, employees, roles, authentication, authorization, migrations, audit events, correlation IDs, and strict tenant-scoped repositories/services.

Acceptance: exhaustive tenant-isolation tests and role matrix tests pass.

### Milestone 2 — Menu and business configuration

Implement menus, groups, items, variants, modifier groups/options, hours, availability, taxes, special-instruction constraints, seed restaurant, APIs, and management UI.

Acceptance: invalid menu structures fail; price math and rounding tests pass; concurrent availability updates are safe.

### Milestone 3 — Calls and deterministic conversation simulator

Implement call sessions, isolated conversation state, text simulator, caller-ID lookup rules, transfer/callback routing, and the first conversation scenarios without real telephony or paid AI.

Acceptance: concurrent simulations cannot mix state; prohibited intents always transfer.

### Milestone 4 — Order state machine and policy/tool gateway

Implement draft construction, validation, quoting, read-back representation, explicit confirmation, legal transitions, persisted action requests, policy decisions, and audited narrow tool execution.

Acceptance: unconfirmed/malformed/stale orders cannot submit; tool calls cannot bypass policy.

### Milestone 5 — Exactly-once external-effect framework

Implement idempotency records, transactional outbox, worker, retries, uncertain-state reconciliation, replay protection, dead-letter handling, and mock POS/SMS adapters.

Acceptance: crash, timeout, duplicate event, and concurrent-confirmation tests produce at most one external order.

### Milestone 6 — Restaurant operations dashboard

Implement the manager/employee experience for calls, orders, transfers, callbacks, menu availability, integrations, audits, and metrics.

Acceptance: role restrictions, tenant isolation, accessibility checks, and live-update reconnection tests pass.

### Milestone 7 — Provider-neutral voice integration

Implement telephony, STT, dialogue, and TTS interfaces with full local mocks. Add a Twilio-compatible adapter only after credentials/approval are available. Verify webhook signatures and consent announcement behavior.

Acceptance: recorded/synthetic audio suite passes agreed thresholds; provider failure transfers safely.

### Milestone 8 — Square sandbox integration

After owner supplies approved sandbox access, implement OAuth, Catalog synchronization, Orders, provider-hosted checkout links, webhooks, token encryption/rotation, revocation, and reconciliation.

Acceptance: Square sandbox contract tests, duplicate prevention, menu drift, revocation, timeout, and webhook replay tests pass.

### Milestone 9 — Observability, load, recovery, and security hardening

Add service-level indicators, dashboards, alerts, redaction, cost accounting, load tests, soak tests, chaos/failure injection, backups, restoration, incident runbooks, SAST/dependency/container scans, and SBOM.

Acceptance: declared pilot capacity and recovery objectives are demonstrated with saved reports.

### Milestone 10 — Controlled staging deployment

Prepare infrastructure as code and staging deployment. Obtain owner approval before provisioning paid resources or making the service public. Apply least privilege, network restrictions, secret storage, backups, and monitoring.

Acceptance: staging smoke, security, restore, and rollback exercises pass.

### Milestone 11 — Pilot readiness

Create onboarding, menu-verification checklist, employee training, escalation matrix, privacy/consent configuration, support runbook, kill switch, pilot scorecard, and go/no-go checklist.

Acceptance: owner explicitly approves any real restaurant calls or orders.

### Milestone 12 — Toast adapter readiness and future verticals

Create the Toast adapter against documented contracts and mocks without claiming production access. Document partner-access requirements. Define extension interfaces for hotels and appointment businesses without implementing them prematurely.

Acceptance: contract tests pass against mocks; no unsupported production claim exists.

## 16. Human approval required

Stop and ask the owner before:

- Creating or purchasing any paid external resource
- Entering or requesting credentials in chat or source files
- Connecting a real Square, Toast, telephony, SMS, payment, or cloud account
- Making a public deployment
- Calling or messaging real people
- Recording real calls
- Processing a real order or payment link
- Importing real customer data
- Changing production data or infrastructure
- Deleting material data or weakening security controls
- Merging to a protected production branch
- Moving from staging to a real pilot
- Accepting a legal/compliance conclusion

Use secure credential entry or environment-secret mechanisms. Never ask the owner to paste secrets into the conversation.

## 16A. Phone control, cloud continuity, and plugin policy

The owner must be able to monitor progress, read milestone reports, review diffs, provide decisions, and approve permitted actions from the ChatGPT/Codex mobile experience. Keep every update short enough to review on a phone and always place the decision needed at the top. Maintain `docs/PROJECT_STATUS.md` as the durable source of current status so the owner does not have to reconstruct progress from chat history.

Use GitHub as the canonical source for code and milestone history. Before delegating work to a cloud environment, ensure the required branch, specification, and prior commits are pushed and that no required work exists only on the laptop. Use one reviewable branch or worktree per milestone where supported. Return commit and pull-request links in milestone reports when available.

Work intended to continue while the owner's laptop is closed must run in a configured Codex cloud environment or approved remote development environment. A local Codex process, local Docker service, uncommitted file, or localhost dependency must never be represented as continuing after the laptop sleeps or shuts down. Cloud work may continue without the laptop, but it can pause for missing information, authentication, approval, usage limits, provider outages, or task completion. Report these pauses clearly to the owner's phone-accessible task.

The cloud environment must be reproducible from the repository. Pin or lock dependencies, document setup commands, use test fixtures instead of laptop-only services, and store approved secrets only in the cloud environment's secret mechanism. Never copy personal machine credentials or broad cloud credentials into the repository. Cloud tasks must not deploy publicly, contact real customers, or provision paid resources without the human approvals already defined.

### Plugin and integration rules

- Use built-in capabilities and already connected plugins before adding anything new.
- GitHub is the required project integration for repository history, reviews, CI status, and phone-accessible progress.
- Square and Twilio/telephony are product API integrations, not assumed ChatGPT plugins. Implement them through narrow provider adapters and official sandbox APIs when the owner supplies approved access.
- An OpenAI developer integration may be added later if the runtime voice/AI implementation requires it.
- Add only one deployment-management plugin after the owner selects the hosting provider; do not connect Railway, Render, DigitalOcean, Cloudflare, AWS, or another provider merely because it is available.
- Before proposing a plugin, verify that it is actually needed, inspect its permissions and dependencies, explain what data/actions it can access, and request owner approval to install or connect it.
- Grant least privilege. Do not grant repository administration, production write, billing, customer-data, or secret access when read-only or environment-scoped access is sufficient.
- Do not install duplicate plugins, speculative tools, or plugins unrelated to the current milestone.
- A missing plugin must not block provider-neutral interfaces, mocks, tests, documentation, or other valid local/cloud work.
- Record every installed/connected plugin, purpose, permissions, owner approval, and removal procedure in `docs/INTEGRATIONS.md`.

At each milestone boundary, state whether the next milestone can run fully in cloud with the laptop closed, which files/commits must be pushed first, which plugins or external accounts are actually required, and what phone approval—if any—is needed.

## 17. Autonomous working rules

- Inspect the repository before acting and preserve unrelated user changes.
- Maintain `docs/PROJECT_STATUS.md` with completed work, current milestone, blockers, decisions, tests, and next action.
- Maintain `docs/DECISIONS.md` or ADRs for material architectural choices.
- Prefer simple, reversible choices.
- Do not hide failing tests, reduce assertions merely to pass, or label mocked behavior as production-ready.
- When a defect is found, add a regression test before or with the fix.
- Do not use destructive Git commands.
- Make small, reviewable commits with the milestone and purpose in the message.
- Run relevant tests before every commit and the full suite at milestone completion.
- If an external dependency is unavailable, implement and test an interface/mock, document the blocker, and continue with work that remains valid.
- Ask only focused blocker questions. For normal implementation choices, decide, document, and proceed.
- Security review cannot be performed only by the same code path being reviewed: run independent static checks and adversarial tests, and clearly flag where external human review remains necessary.

## 18. Milestone completion report format

At the end of each milestone, report:

1. Outcome
2. Files/components changed
3. Architecture/security decisions
4. Commands/tests run and exact results
5. Acceptance criteria evidence
6. Known limitations/residual risks
7. External approvals or credentials needed
8. Next milestone
9. Commit SHA
10. Corrections or improvements discovered, including which ones were applied and which require owner approval
11. Cloud/phone continuation status, including whether the laptop may be closed and what could pause the work
12. Plugin/integration status and the minimum permission needed next

If any required test fails, the milestone status is `NOT COMPLETE`.

## 19. First instruction

Begin with Milestone 0 only. Inspect the current repository. If it is empty, initialize the project safely. Produce the engineering constitution, architecture outline, threat-model skeleton, local development environment, CI skeleton, test strategy, milestone tracker, and exact proposed stack. Do not create paid services or request credentials. Run all available checks, commit the milestone if permitted, report evidence, and then continue to Milestone 1 unless a genuine blocker or human-approval boundary is reached.

---

# OWNER QUICK START

1. Create or open a GitHub repository for the project.
2. Open it in the Codex desktop app.
3. Select Astra with the default/medium reasoning level initially.
4. Attach or copy this file into the repository root.
5. Paste the **MASTER EXECUTION PROMPT** into the new Codex task.
6. Give Codex repository write and test permissions, but retain approval for external accounts, real calls, paid resources, production, and destructive actions.
7. Review the milestone report and commit before allowing real integrations.
8. Connect the same ChatGPT account in the mobile app to monitor, steer, and approve cloud tasks from the phone.
9. Push the active branch before closing the laptop, then confirm that the continuing task is running in Codex Cloud rather than only in a local session.

For work that must continue after the laptop is closed, delegate suitable repository tasks to Codex Cloud. Cloud work can continue remotely, but may pause for a decision or permission. Keep Astra for architecture, difficult debugging, threat analysis, and milestone review; use less expensive models for narrow implementation or repetitive tests where appropriate.

# OWNER INFORMATION TO PROVIDE LATER

Do not provide these until the milestone actually requires them:

- Chosen business/product name
- GitHub repository and branch policy
- Square sandbox application access
- Telephony test account
- SMS sender configuration
- Approved cloud budget and provider
- Pilot restaurant's written permission
- Verified menu and employee escalation numbers
- Recording-consent/legal guidance for pilot jurisdictions
- Toast partner credentials if approval is granted

# REALISTIC EXPECTATION

Codex/Astra can perform most engineering work, but this prompt does not make the project safely autonomous from idea to production. External access, real-world accuracy, legal obligations, restaurant onboarding, provider approval, security review, and production launch require human participation. A passing automated suite is necessary but not sufficient for a live pilot.
