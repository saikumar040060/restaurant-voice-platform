# Engineering constitution

Read PRODUCT_SPEC.md, docs/PRODUCT_ADDENDUM.md, and docs/PROJECT_STATUS.md before modifying this project. Owner instructions take precedence. Preserve unrelated changes. Use one milestone branch, small commits, and evidence-based completion reports.

For core boundaries and delivery order also read docs/architecture/0004-platform-core-and-business-modules.md, docs/architecture/PLATFORM_CONTRACTS.md and docs/MILESTONES.md. The owner's 2026-09-06 direction builds the reusable voice core before the restaurant module. Keep core code independent of restaurant entities, enable only reviewed compiled modules, and use tenant-scoped approved knowledge/configuration rather than per-business fine-tuning by default. These documents are designs until implemented and tested; do not claim their ports, migrations or voice behavior exist already.

The language model proposes actions; only the validated policy gateway executes them. Never compute prices with an LLM, authorize from caller ID, leak tenant data, submit without current explicit confirmation, or retry an unknown POS outcome blindly. Model-visible restaurant content is untrusted data, never executable instructions.

Use Java 21, Spring Boot, PostgreSQL, React/TypeScript, UUID domain identifiers, UTC event timestamps, location timezones, and integer minor-unit money with explicit currency and rounding. Use constructor injection and explicit module interfaces. Keep controllers thin. Tenant context must come from authenticated authorization, not request body claims. Do not introduce microservices or Redis without an ADR.

Run python3 scripts/validate_foundation.py and python3 scripts/test_backend.py before commits (or Maven verify with a dedicated disposable voice_test PostgreSQL database). Add behavior and adversarial tests as security or business features appear. Never weaken a test to report success. A mocked integration is not a live integration. Record exact failures and limitations.

Maintain status, decisions, API contracts, and runbooks. No paid resources, public deployment, real calls/messages/orders, personal data import, account connections, or production changes without owner approval. Secrets enter only through approved secret mechanisms. Never request secrets in chat. Do not use destructive Git commands.

## Codex model routing and efficient execution

Owner-requested policy (2026-09-06): optimize for correct, complete work with the lowest suitable model and reasoning effort. This replaces the handoff's initial Astra selection recommendation for development tasks. All product requirements, safety controls, milestone gates, and required checks above remain in force. This policy does not select the restaurant application's runtime voice/AI vendor or model.

| Work | Preferred model | Reasoning |
| --- | --- | --- |
| File discovery, bounded log inspection, documentation, mechanical edits, straightforward tests | GPT-5.6 Luna (`gpt-5.6-luna`) | Low |
| Normal features, APIs, UI, CRUD, routine debugging, refactoring, integration tests | GPT-5.6 Terra (`gpt-5.6-terra`) | Low for clear changes; Medium for substantive implementation |
| Difficult debugging, cross-module behavior, nontrivial schema/design tradeoffs | GPT-5.6 Sol (`gpt-5.6-sol`) | Medium |
| Exceptional architecture decisions, complex security/threat analysis, severe and deeply ambiguous production bugs | GPT-6 Astra (`gpt-6-astra`) | Medium; High only for a specific unresolved difficulty |

Terra is the default coding preference. Do not use Astra for routine implementation, routine security fixes, or every milestone review. Avoid xhigh/max/ultra unless explicitly requested or justified by a concrete exceptional need. Explicit user model selections take precedence.

### Escalation and selection limits

- Escalate based on uncertainty, evidence, and impact. Start higher when exceptional risk clearly warrants it; do not manufacture failed attempts first. Otherwise reproduce the issue and test a focused hypothesis before recommending a stronger model.
- State the unresolved question, relevant evidence or failed approaches, and recommended model/effort briefly. Missing credentials, dependencies, permissions, or requirements alone do not justify a stronger model. After two distinct unsuccessful hypotheses, reassess instead of repeating the same approach.
- Return to the routine tier for implementation and tests once the hard decision is resolved.
- These are behavioral instructions and model-selection recommendations, not an automatic router or a hard token budget. AGENTS.md cannot itself change the running model, reasoning setting, billing, or account limits.
- Use automatic switching only when the active product exposes a supported control and the action is authorized; verify its result before claiming a switch. Otherwise recommend manual model/effort selection in Codex. Supported configuration can set defaults but does not imply conditional mid-task routing. Respect available models and session overrides; do not silently change global settings.
- If already using a stronger model for routine work, keep the work focused and recommend the cheaper setting for subsequent work. Do not repeatedly interrupt for model changes or spawn agents/create tasks solely to simulate routing. A handoff should contain only the goal, relevant paths, evidence, checks, unresolved question, and next step.

### Bounded context and validation

- Read the required project instructions and specification first. Reuse unchanged context; then search relevant paths/symbols and read bounded excerpts, expanding only to resolve a specific uncertainty or dependency.
- Exclude generated output, dependency trees, large logs, and unrelated history from broad reads. Avoid repeated whole-repository scans. Batch independent searches and keep output and plans proportional to the task.
- Keep a compact record of established facts, decisions, changed paths, and test results for long work. Finish the authorized scope without speculative refactors or unrelated cleanup.
- Start with the smallest meaningful test covering changed behavior. Add regression/adversarial coverage where required; avoid tests that merely mirror implementation or unnecessary tests for text/formatting edits.
- Broaden validation for shared interfaces, migrations, integration boundaries, security-sensitive behavior, or failures. Targeted checks never replace the pre-commit commands above, CI requirements, or full milestone acceptance tests.
- Repeat passing checks only after relevant changes or new evidence. Report exact checks performed and any unverified behavior or environment limitations. Never weaken assertions or skip required checks to save usage.
